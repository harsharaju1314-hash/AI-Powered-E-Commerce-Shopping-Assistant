from typing import Optional
from fastapi import APIRouter, Depends, Header, HTTPException, status
from sqlalchemy.orm import Session
from app.database.session import get_db
from app.models.user import User
from app.auth.deps import get_current_user_optional
from app.schemas.cart import CartOut, CartItemCreate, CartItemUpdate
from app.services.cart_service import cart_service

router = APIRouter()


def _resolve_cart(
    db: Session,
    current_user: Optional[User],
    x_session_token: Optional[str]
):
    user_id = current_user.id if current_user else None
    token = x_session_token if not user_id else None
    return cart_service.get_or_create_cart(db, user_id=user_id, session_token=token)


@router.get("", response_model=CartOut)
def get_cart(
    x_session_token: Optional[str] = Header(None, alias="X-Session-Token"),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db)
):
    """Retrieve current shopping cart and price breakdown."""
    cart = _resolve_cart(db, current_user, x_session_token)
    return cart_service.build_cart_response(cart)


@router.post("/items", response_model=CartOut)
def add_item_to_cart(
    item_in: CartItemCreate,
    x_session_token: Optional[str] = Header(None, alias="X-Session-Token"),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db)
):
    """Add a product with validated quantity and inventory check."""
    cart = _resolve_cart(db, current_user, x_session_token)
    cart_service.add_to_cart(db, cart, item_in.product_id, item_in.quantity)
    return cart_service.build_cart_response(cart)


@router.put("/items/{product_id}", response_model=CartOut)
def update_cart_item(
    product_id: int,
    item_update: CartItemUpdate,
    x_session_token: Optional[str] = Header(None, alias="X-Session-Token"),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db)
):
    """Update item quantity in cart or remove if quantity is zero."""
    cart = _resolve_cart(db, current_user, x_session_token)
    cart_service.update_quantity(db, cart, product_id, item_update.quantity)
    return cart_service.build_cart_response(cart)


@router.delete("/items/{product_id}", response_model=CartOut)
def remove_cart_item(
    product_id: int,
    x_session_token: Optional[str] = Header(None, alias="X-Session-Token"),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db)
):
    """Remove a product from the shopping cart."""
    cart = _resolve_cart(db, current_user, x_session_token)
    cart_service.remove_item(db, cart, product_id)
    return cart_service.build_cart_response(cart)


@router.delete("/clear", response_model=CartOut)
def clear_cart(
    x_session_token: Optional[str] = Header(None, alias="X-Session-Token"),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db)
):
    """Empty all items from the current cart."""
    cart = _resolve_cart(db, current_user, x_session_token)
    cart_service.clear_cart(db, cart)
    return cart_service.build_cart_response(cart)
