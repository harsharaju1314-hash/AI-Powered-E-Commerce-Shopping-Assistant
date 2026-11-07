import json
from typing import List, Optional
from fastapi import APIRouter, Depends, Header, HTTPException, status
from sqlalchemy.orm import Session
from app.database.session import get_db
from app.models.user import User
from app.models.chat import ChatSession
from app.auth.deps import get_current_user_optional, get_current_user
from app.schemas.chat import (
    ChatWorkflowRequest, ChatWorkflowResponse,
    ChatSessionOut, ChatMessageOut
)
from app.agents.orchestrator import agent_orchestrator
from app.services.chat_service import chat_service

router = APIRouter()


@router.post("", response_model=ChatWorkflowResponse)
def execute_chat_workflow(
    request: ChatWorkflowRequest,
    x_session_token: Optional[str] = Header(None, alias="X-Session-Token"),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db)
):
    """
    Core Agentic Commerce endpoint:
    Orchestrates the 4 specialized AI agents (Product Discovery, Recommendation,
    Cart Management, and Order Management) via LangGraph StateGraph.
    """
    token = request.session_token or x_session_token
    return agent_orchestrator.process_chat_message(
        db=db,
        message=request.message,
        session_token=token,
        user=current_user,
        confirmation_payload=request.confirmation_payload
    )


@router.get("/sessions", response_model=List[ChatSessionOut])
def get_user_chat_sessions(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """List all previous conversation sessions for authenticated user."""
    sessions = chat_service.list_user_sessions(db, current_user.id)
    return [ChatSessionOut.from_orm(s) for s in sessions]


@router.get("/history/{token}", response_model=List[ChatMessageOut])
def get_session_history(
    token: str,
    db: Session = Depends(get_db)
):
    """Retrieve full chronological conversation transcript for a session."""
    session = db.query(ChatSession).filter(ChatSession.session_token == token).first()
    if not session:
        return []

    messages = chat_service.get_session_history(db, session.id)
    formatted = []
    for m in messages:
        meta_dict = None
        if m.metadata_json:
            try:
                meta_dict = json.loads(m.metadata_json)
            except Exception:
                meta_dict = None

        formatted.append(
            ChatMessageOut(
                id=m.id,
                sender=m.sender,
                agent_name=m.agent_name,
                content=m.content,
                metadata_json=meta_dict,
                created_at=m.created_at
            )
        )
    return formatted
