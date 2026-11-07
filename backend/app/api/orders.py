from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database.session import get_db
from app.models.user import User
from app.auth.deps import get_current_user
from app.schemas.order import (
    CheckoutPrepareRequest, CheckoutPrepareResponse,
    CheckoutConfirmRequest, OrderOut, OrderTrackingOut
)
from app.services.order_service import order_service

router = APIRouter()


@router.post("/prepare", response_model=CheckoutPrepareResponse)
def prepare_order_checkout(
    req: CheckoutPrepareRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Step 1 of Agentic Checkout:
    Validates cart inventory and generates a cryptographic confirmation token.
    Explicit user confirmation is required prior to order execution.
    """
    return order_service.prepare_checkout(
        db=db,
        user=current_user,
        shipping_address=req.shipping_address,
        notes=req.notes
    )


@router.post("/confirm", response_model=OrderOut)
def confirm_order_checkout(
    req: CheckoutConfirmRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Step 2 of Agentic Checkout:
    Executes simulated checkout upon explicit authorization,
    decrements inventory, and creates unique order record.
    """
    order = order_service.execute_checkout(
        db=db,
        user=current_user,
        confirmation_token=req.confirmation_token,
        confirmed=req.confirmed,
        shipping_address=req.shipping_address
    )
    return OrderOut.from_orm(order)


@router.get("/user", response_model=List[OrderOut])
def get_user_orders(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Retrieve all simulated orders placed by current user."""
    orders = order_service.list_user_orders(db, current_user.id)
    return [OrderOut.from_orm(o) for o in orders]


@router.get("/track/{order_number}", response_model=OrderTrackingOut)
def track_order_status(
    order_number: str,
    current_user: Optional[User] = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get real-time carrier tracking timeline and status for an order."""
    return order_service.track_order(
        db=db,
        order_number=order_number,
        user_id=current_user.id if current_user else None
    )


@router.get("/{order_id}", response_model=OrderOut)
def get_order_by_id(
    order_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get single order details by primary ID."""
    order = order_service.get_order_by_id(db, order_id, user_id=current_user.id)
    if not order:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Order not found.")
    return OrderOut.from_orm(order)
