from typing import List, Optional
from datetime import datetime
from pydantic import BaseModel, Field
from app.schemas.product import ProductOut


class CartItemBase(BaseModel):
    product_id: int
    quantity: int = Field(default=1, ge=1, le=99)


class CartItemCreate(CartItemBase):
    pass


class CartItemUpdate(BaseModel):
    quantity: int = Field(..., ge=0, le=99)  # 0 indicates removal


class CartItemOut(BaseModel):
    id: int
    product_id: int
    quantity: int
    unit_price: float
    total_price: float
    product: Optional[ProductOut] = None
    created_at: datetime

    class Config:
        from_attributes = True


class CartSummary(BaseModel):
    subtotal: float
    tax_estimated: float
    shipping_estimated: float
    total: float
    item_count: int


class CartOut(BaseModel):
    id: int
    user_id: Optional[int] = None
    items: List[CartItemOut] = []
    summary: CartSummary

    class Config:
        from_attributes = True
