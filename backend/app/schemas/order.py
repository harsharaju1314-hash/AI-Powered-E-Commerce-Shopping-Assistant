from typing import List, Optional
from datetime import datetime
from pydantic import BaseModel, Field


class OrderItemOut(BaseModel):
    id: int
    product_id: Optional[int]
    product_name: str
    product_sku: Optional[str] = None
    quantity: int
    unit_price: float
    total_price: float

    class Config:
        from_attributes = True


class CheckoutPrepareRequest(BaseModel):
    shipping_address: str = Field(..., min_length=5)
    notes: Optional[str] = None


class CheckoutPrepareResponse(BaseModel):
    confirmation_token: str
    subtotal: float
    tax_amount: float
    shipping_amount: float
    total_amount: float
    item_count: int
    items_summary: List[dict]
    shipping_address: str
    requires_explicit_confirmation: bool = True
    prompt_message: str


class CheckoutConfirmRequest(BaseModel):
    confirmation_token: str
    confirmed: bool = Field(..., description="Must be true to authorize purchase")
    shipping_address: Optional[str] = None


class OrderOut(BaseModel):
    id: int
    order_number: str
    user_id: int
    status: str
    subtotal: float
    tax_amount: float
    shipping_amount: float
    total_amount: float
    shipping_address: str
    payment_status: str
    confirmation_code: Optional[str] = None
    notes: Optional[str] = None
    items: List[OrderItemOut] = []
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class OrderTrackingStep(BaseModel):
    status: str
    title: str
    description: str
    timestamp: datetime
    is_completed: bool
    is_current: bool


class OrderTrackingOut(BaseModel):
    order: OrderOut
    timeline: List[OrderTrackingStep]
    estimated_delivery: datetime
