import re
import json
import logging
from typing import Dict, Any, List, Optional, TypedDict
from sqlalchemy.orm import Session
from langgraph.graph import StateGraph, END
from app.database.session import SessionLocal
from app.agents.discovery_agent import discovery_agent
from app.agents.recommendation_agent import recommendation_agent
from app.agents.cart_agent import cart_agent
from app.agents.order_agent import order_agent
from app.services.chat_service import chat_service
from app.schemas.chat import ChatWorkflowResponse, AgentThought
from app.models.user import User

logger = logging.getLogger(__name__)

# Cross-thread session registry so LangGraph worker threads share the exact active DB session
_ACTIVE_DB_SESSIONS: Dict[str, Session] = {}


def get_db_for_session(token: str) -> Session:
    return _ACTIVE_DB_SESSIONS.get(token) or SessionLocal()


class AgentGraphState(TypedDict):
    query: str
    user_id: Optional[int]
    session_token: str
    confirmation_payload: Optional[Dict[str, Any]]
    context: Dict[str, Any]
    target_agent: str
    agent_thoughts: List[Dict[str, str]]
    response_payload: Dict[str, Any]
    error: Optional[str]


def router_node(state: AgentGraphState) -> AgentGraphState:
    query = state["query"].lower().strip()
    thoughts = list(state.get("agent_thoughts", []))
    confirmation = state.get("confirmation_payload")

    if confirmation or any(w in query for w in [
        "checkout", "place order", "buy now", "confirm purchase",
        "track order", "tracking", "order status", "order history", "past orders"
    ]) or re.search(r"\bORD-\d{8}-[A-Z0-9]+\b", query, re.IGNORECASE):
        target = "order"
        thoughts.append({
            "agent_name": "Supervisor Router",
            "action": "Route to Order Management Agent",
            "details": "Detected checkout, payment authorization, or order tracking request."
        })

    elif any(w in query for w in [
        "add to cart", "put in cart", "remove from cart", "view cart",
        "show cart", "clear cart", "delete from cart", "empty cart", "my cart", "cart"
    ]):
        target = "cart"
        thoughts.append({
            "agent_name": "Supervisor Router",
            "action": "Route to Cart Management Agent",
            "details": "Detected cart mutation or inspection intent."
        })

    elif any(w in query for w in [
        "recommend", "compare", "difference between", "which is better",
        "suggest", "pros and cons", "vs", "versus", "personalized"
    ]):
        target = "recommendation"
        thoughts.append({
            "agent_name": "Supervisor Router",
            "action": "Route to Recommendation Agent",
            "details": "Detected recommendation, comparative evaluation, or RAG factual synthesis request."
        })

    else:
        target = "discovery"
        thoughts.append({
            "agent_name": "Supervisor Router",
            "action": "Route to Product Discovery Agent",
            "details": "Detected product discovery, catalog query, or attribute filtering."
        })

    return {
        **state,
        "target_agent": target,
        "agent_thoughts": thoughts
    }


def route_decision(state: AgentGraphState) -> str:
    return state.get("target_agent", "discovery")


def discovery_node(state: AgentGraphState) -> AgentGraphState:
    query = state["query"]
    context = state.get("context", {})
    thoughts = list(state.get("agent_thoughts", []))
    db = get_db_for_session(state.get("session_token", ""))

    try:
        res = discovery_agent.execute(db=db, query=query, context=context)
        thoughts.append({
            "agent_name": discovery_agent.name,
            "action": "Execute Catalog Search",
            "details": res.get("thought", "")
        })
        return {**state, "response_payload": res, "agent_thoughts": thoughts}
    except Exception as e:
        logger.error(f"Error in discovery_node: {e}", exc_info=True)
        return {**state, "error": str(e)}


def recommendation_node(state: AgentGraphState) -> AgentGraphState:
    query = state["query"]
    user_id = state.get("user_id")
    context = state.get("context", {})
    thoughts = list(state.get("agent_thoughts", []))
    db = get_db_for_session(state.get("session_token", ""))

    try:
        user = db.query(User).filter(User.id == user_id).first() if user_id else None
        res = recommendation_agent.execute(db=db, query=query, user=user, context=context)
        thoughts.append({
            "agent_name": recommendation_agent.name,
            "action": "Synthesize Recommendations & Compare",
            "details": res.get("thought", "")
        })
        return {**state, "response_payload": res, "agent_thoughts": thoughts}
    except Exception as e:
        logger.error(f"Error in recommendation_node: {e}", exc_info=True)
        return {**state, "error": str(e)}


def cart_node(state: AgentGraphState) -> AgentGraphState:
    query = state["query"]
    user_id = state.get("user_id")
    session_token = state.get("session_token", "")
    context = state.get("context", {})
    thoughts = list(state.get("agent_thoughts", []))
    db = get_db_for_session(session_token)

    try:
        user = db.query(User).filter(User.id == user_id).first() if user_id else None
        res = cart_agent.execute(
            db=db,
            query=query,
            user=user,
            session_token=session_token,
            context=context
        )
        thoughts.append({
            "agent_name": cart_agent.name,
            "action": "Cart State Mutation",
            "details": res.get("thought", "")
        })
        return {**state, "response_payload": res, "agent_thoughts": thoughts}
    except Exception as e:
        logger.error(f"Error in cart_node: {e}", exc_info=True)
        return {**state, "error": str(e)}


def order_node(state: AgentGraphState) -> AgentGraphState:
    query = state["query"]
    user_id = state.get("user_id")
    session_token = state.get("session_token", "")
    confirmation_payload = state.get("confirmation_payload")
    context = state.get("context", {})
    thoughts = list(state.get("agent_thoughts", []))
    db = get_db_for_session(session_token)

    try:
        user = db.query(User).filter(User.id == user_id).first() if user_id else None
        res = order_agent.execute(
            db=db,
            query=query,
            user=user,
            session_token=session_token,
            confirmation_payload=confirmation_payload,
            context=context
        )
        thoughts.append({
            "agent_name": order_agent.name,
            "action": "Order Transaction & Tracking",
            "details": res.get("thought", "")
        })
        return {**state, "response_payload": res, "agent_thoughts": thoughts}
    except Exception as e:
        logger.error(f"Error in order_node: {e}", exc_info=True)
        return {**state, "error": str(e)}


def build_agent_graph():
    """Constructs LangGraph StateGraph orchestration pipeline."""
    workflow = StateGraph(AgentGraphState)

    workflow.add_node("router", router_node)
    workflow.add_node("discovery", discovery_node)
    workflow.add_node("recommendation", recommendation_node)
    workflow.add_node("cart", cart_node)
    workflow.add_node("order", order_node)

    workflow.set_entry_point("router")

    workflow.add_conditional_edges(
        "router",
        route_decision,
        {
            "discovery": "discovery",
            "recommendation": "recommendation",
            "cart": "cart",
            "order": "order",
        }
    )

    workflow.add_edge("discovery", END)
    workflow.add_edge("recommendation", END)
    workflow.add_edge("cart", END)
    workflow.add_edge("order", END)

    return workflow.compile()


app_agent_graph = build_agent_graph()


class AgentOrchestrator:
    @staticmethod
    def process_chat_message(
        db: Session,
        message: str,
        session_token: Optional[str] = None,
        user: Optional[User] = None,
        confirmation_payload: Optional[Dict[str, Any]] = None
    ) -> ChatWorkflowResponse:
        session = chat_service.get_or_create_session(
            db=db,
            session_token=session_token,
            user_id=user.id if user else None
        )

        _ACTIVE_DB_SESSIONS[session.session_token] = db

        try:
            chat_service.add_message(
                db=db,
                session_id=session.id,
                sender="user",
                content=message
            )

            history = chat_service.get_session_history(db, session.id)
            candidate_ids = []
            for msg in reversed(history[-6:]):
                if msg.metadata_json:
                    try:
                        meta = json.loads(msg.metadata_json)
                        if "products" in meta and isinstance(meta["products"], list):
                            for p in meta["products"]:
                                if isinstance(p, dict) and "id" in p:
                                    candidate_ids.append(p["id"])
                    except Exception:
                        pass

            initial_state: AgentGraphState = {
                "query": message,
                "user_id": user.id if user else None,
                "session_token": session.session_token,
                "confirmation_payload": confirmation_payload,
                "context": {"candidate_ids": list(dict.fromkeys(candidate_ids))},
                "target_agent": "",
                "agent_thoughts": [],
                "response_payload": {},
                "error": None
            }

            # Execute Multi-Agent Graph transitions cleanly and synchronously
            routed_state = router_node(initial_state)
            target = routed_state.get("target_agent", "discovery")
            node_map = {
                "discovery": discovery_node,
                "recommendation": recommendation_node,
                "cart": cart_node,
                "order": order_node,
            }
            node_fn = node_map.get(target, discovery_node)
            final_state = node_fn(routed_state)

            res = final_state.get("response_payload", {})
            active_agent = res.get("agent_name", final_state.get("target_agent", "Supervisor"))
            response_text = res.get("response", "I have processed your request.")
            thoughts_data = final_state.get("agent_thoughts", [])

            meta_to_save = {
                "products": res.get("products"),
                "cart_summary": res.get("cart_summary"),
                "order_details": res.get("order_details"),
                "confirmation_token": res.get("confirmation_token"),
                "suggested_actions": res.get("suggested_actions", [])
            }

            chat_service.add_message(
                db=db,
                session_id=session.id,
                sender="assistant",
                agent_name=active_agent,
                content=response_text,
                metadata_json=meta_to_save
            )

            thoughts_out = [
                AgentThought(
                    agent_name=t["agent_name"],
                    action=t["action"],
                    details=t.get("details")
                )
                for t in thoughts_data
            ]

            return ChatWorkflowResponse(
                session_token=session.session_token,
                response_message=response_text,
                active_agent=active_agent,
                agent_thoughts=thoughts_out,
                products=res.get("products"),
                cart_summary=res.get("cart_summary"),
                order_details=res.get("order_details"),
                confirmation_required=bool(res.get("confirmation_required", False)),
                confirmation_token=res.get("confirmation_token"),
                suggested_actions=res.get("suggested_actions", [
                    "Find wireless headphones",
                    "Compare laptops",
                    "View my shopping cart"
                ])
            )
        finally:
            _ACTIVE_DB_SESSIONS.pop(session.session_token, None)


agent_orchestrator = AgentOrchestrator()
