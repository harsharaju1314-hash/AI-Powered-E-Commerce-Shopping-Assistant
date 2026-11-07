from typing import List, Optional, Dict, Any
from datetime import datetime
from pydantic import BaseModel, Field


class AgentThought(BaseModel):
    agent_name: str
    action: str
    details: Optional[str] = None


class ChatMessageCreate(BaseModel):
    message: str = Field(..., min_length=1)
    session_token: Optional[str] = None


class ChatMessageOut(BaseModel):
    id: int
    sender: str
    agent_name: Optional[str] = None
    content: str
    metadata_json: Optional[Dict[str, Any]] = None
    created_at: datetime

    class Config:
        from_attributes = True


class ChatSessionOut(BaseModel):
    id: int
    session_token: str
    title: str
    created_at: datetime
    updated_at: datetime
    messages: List[ChatMessageOut] = []

    class Config:
        from_attributes = True


class ChatWorkflowRequest(BaseModel):
    message: str
    session_token: Optional[str] = None
    confirmation_payload: Optional[Dict[str, Any]] = None


class ChatWorkflowResponse(BaseModel):
    session_token: str
    response_message: str
    active_agent: str
    agent_thoughts: List[AgentThought] = []
    products: Optional[List[Dict[str, Any]]] = None
    cart_summary: Optional[Dict[str, Any]] = None
    order_details: Optional[Dict[str, Any]] = None
    confirmation_required: bool = False
    confirmation_token: Optional[str] = None
    suggested_actions: List[str] = []
