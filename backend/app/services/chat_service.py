import uuid
import json
from typing import Optional, List, Dict, Any
from sqlalchemy.orm import Session
from app.models.chat import ChatSession, ChatMessage
from app.models.user import User


class ChatService:
    @staticmethod
    def get_or_create_session(db: Session, session_token: Optional[str] = None, user_id: Optional[int] = None) -> ChatSession:
        session = None
        if session_token:
            session = db.query(ChatSession).filter(ChatSession.session_token == session_token).first()

        if not session:
            new_token = session_token or f"sess_{uuid.uuid4().hex}"
            session = ChatSession(
                session_token=new_token,
                user_id=user_id,
                title="AI Shopping Session"
            )
            db.add(session)
            db.commit()
            db.refresh(session)
        elif user_id and not session.user_id:
            session.user_id = user_id
            db.commit()
            db.refresh(session)

        return session

    @staticmethod
    def add_message(
        db: Session,
        session_id: int,
        sender: str,
        content: str,
        agent_name: Optional[str] = None,
        metadata_json: Optional[Dict[str, Any]] = None
    ) -> ChatMessage:
        meta_str = json.dumps(metadata_json) if metadata_json else None
        msg = ChatMessage(
            session_id=session_id,
            sender=sender,
            agent_name=agent_name,
            content=content,
            metadata_json=meta_str
        )
        db.add(msg)
        db.commit()
        db.refresh(msg)
        return msg

    @staticmethod
    def get_session_history(db: Session, session_id: int) -> List[ChatMessage]:
        return db.query(ChatMessage).filter(ChatMessage.session_id == session_id).order_by(ChatMessage.created_at.asc()).all()

    @staticmethod
    def list_user_sessions(db: Session, user_id: int) -> List[ChatSession]:
        return db.query(ChatSession).filter(ChatSession.user_id == user_id).order_by(ChatSession.updated_at.desc()).all()


chat_service = ChatService()
