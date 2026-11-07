import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from app.agents.discovery_agent import discovery_agent
from app.agents.recommendation_agent import recommendation_agent
from app.agents.cart_agent import cart_agent
from app.agents.order_agent import order_agent
from app.agents.orchestrator import app_agent_graph, AgentGraphState, agent_orchestrator


def test_product_discovery_agent_execution(db_session: Session):
    res = discovery_agent.execute(
        db=db_session,
        query="Find the best wireless headphones under $100"
    )
    assert res["agent_name"] == "Product Discovery Agent"
    assert "thought" in res
    assert len(res["products"]) > 0
    top_p = res["products"][0]
    assert top_p["price"] <= 100.0


def test_recommendation_agent_execution(db_session: Session):
    res = recommendation_agent.execute(
        db=db_session,
        query="Recommend an ergonomic office chair for working from home"
    )
    assert res["agent_name"] == "Recommendation Agent"
    assert "relevance_explanation" in res["products"][0]
    assert len(res["suggested_actions"]) > 0


def test_recommendation_agent_comparison(db_session: Session):
    # Compare 2 products
    first_two = [1, 2]
    res = recommendation_agent.execute(
        db=db_session,
        query="Compare product 1 and product 2",
        context={"candidate_ids": first_two}
    )
    assert res["agent_name"] == "Recommendation Agent"
    assert "Comparison" in res["response"]
    assert "winner_id" in res


def test_cart_agent_view(db_session: Session, customer_user):
    res = cart_agent.execute(
        db=db_session,
        query="View my cart",
        user=customer_user
    )
    assert res["agent_name"] == "Cart Management Agent"
    assert "cart_summary" in res


def test_order_agent_requires_confirmation(db_session: Session, customer_user):
    res = order_agent.execute(
        db=db_session,
        query="Checkout my cart",
        user=customer_user
    )
    assert res["agent_name"] == "Order Management Agent"
    assert "thought" in res


def test_langgraph_routing(db_session: Session):
    initial_state: AgentGraphState = {
        "query": "Find lightweight running shoes for men under $80",
        "user_id": None,
        "session_token": "test_graph_token",
        "confirmation_payload": None,
        "context": {},
        "target_agent": "",
        "agent_thoughts": [],
        "response_payload": {},
        "error": None
    }
    result = app_agent_graph.invoke(initial_state)
    assert result["target_agent"] == "discovery"
    assert result["response_payload"]["agent_name"] == "Product Discovery Agent"
    assert len(result["agent_thoughts"]) >= 2  # Router + Discovery Agent


def test_multiagent_full_chat_endpoint(db_session: Session, customer_user):
    res = agent_orchestrator.process_chat_message(
        db=db_session,
        message="I need a laptop for programming under $900.",
        session_token="test_agentic_flow_token",
        user=customer_user
    )
    assert res.session_token == "test_agentic_flow_token"
    assert "Discovery" in res.active_agent
    assert len(res.agent_thoughts) >= 2
    assert len(res.suggested_actions) > 0
    assert res.products is not None and len(res.products) > 0
