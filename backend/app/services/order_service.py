import uuid
import datetime
import json
from typing import List, Optional
from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from app.models.order import Order, OrderItem, OrderStatus
from app.models.cart import ShoppingCart, CartItem
from app.models.product import Product
from app.models.user import User
from app.schemas.order import (
    CheckoutPrepareResponse, OrderOut, OrderItemOut,
    OrderTrackingOut, OrderTrackingStep
)
from app.services.cart_service import cart_service


class OrderService:
    @staticmethod
    def prepare_checkout(
        db: Session,
        user: User,
        shipping_address: str,
        notes: Optional[str] = None
    ) -> CheckoutPrepareResponse:
        cart = cart_service.get_or_create_cart(db, user_id=user.id)
        if not cart.items:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Shopping cart is empty.")

        # Inventory check
        items_summary = []
        for item in cart.items:
            p = item.product
            if not p or not p.is_available or p.inventory_count < item.quantity:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Product '{p.name if p else item.product_id}' does not have enough stock available."
                )
            items_summary.append({
                "product_id": p.id,
                "name": p.name,
                "sku": p.sku,
                "quantity": item.quantity,
                "unit_price": item.unit_price,
                "total_price": round(item.quantity * item.unit_price, 2)
            })

        cart_data = cart_service.build_cart_response(cart)
        conf_token = f"CONF-{user.id}-{int(datetime.datetime.utcnow().timestamp())}-{uuid.uuid4().hex[:8]}"

        prompt_message = (
            f"Please confirm your purchase of {cart_data.summary.item_count} items "
            f"for a total of ${cart_data.summary.total:.2f} to be shipped to: {shipping_address}."
        )

        return CheckoutPrepareResponse(
            confirmation_token=conf_token,
            subtotal=cart_data.summary.subtotal,
            tax_amount=cart_data.summary.tax_estimated,
            shipping_amount=cart_data.summary.shipping_estimated,
            total_amount=cart_data.summary.total,
            item_count=cart_data.summary.item_count,
            items_summary=items_summary,
            shipping_address=shipping_address,
            requires_explicit_confirmation=True,
            prompt_message=prompt_message
        )

    @staticmethod
    def execute_checkout(
        db: Session,
        user: User,
        confirmation_token: str,
        confirmed: bool,
        shipping_address: Optional[str] = None
    ) -> Order:
        if not confirmed:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Checkout cancelled. Explicit confirmation is required to process order."
            )

        # Validate confirmation token structure
        if not confirmation_token or not confirmation_token.startswith("CONF-"):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid or expired checkout confirmation token."
            )

        token_parts = confirmation_token.split("-")
        if len(token_parts) < 4 or int(token_parts[1]) != user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Confirmation token does not match authorized user."
            )

        # Duplicate checkout prevention: check if this confirmation code was already used
        existing_order = db.query(Order).filter(Order.confirmation_code == confirmation_token).first()
        if existing_order:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Duplicate checkout detected. This order has already been processed."
            )

        cart = cart_service.get_or_create_cart(db, user_id=user.id)
        if not cart.items:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Shopping cart is empty.")

        # Atomic transaction: validate inventory, decrement stock, create order
        subtotal = 0.0
        order_items_to_create = []

        for item in cart.items:
            # Lock/re-verify stock
            product = db.query(Product).filter(Product.id == item.product_id).with_for_update().first()
            if not product or product.inventory_count < item.quantity:
                db.rollback()
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Insufficient inventory for '{product.name if product else item.product_id}'."
                )

            # Decrement inventory
            product.inventory_count -= item.quantity
            if product.inventory_count == 0:
                product.is_available = False

            item_total = round(item.quantity * item.unit_price, 2)
            subtotal += item_total

            order_items_to_create.append(
                OrderItem(
                    product_id=product.id,
                    product_name=product.name,
                    product_sku=product.sku,
                    quantity=item.quantity,
                    unit_price=item.unit_price,
                    total_price=item_total
                )
            )

        subtotal = round(subtotal, 2)
        tax = round(subtotal * 0.08, 2)
        shipping = 0.0 if subtotal >= 50.0 else 9.99
        total = round(subtotal + tax + shipping, 2)

        now = datetime.datetime.utcnow()
        order_num = f"ORD-{now.strftime('%Y%m%d')}-{uuid.uuid4().hex[:6].upper()}"

        final_address = shipping_address or "123 Technology Way, Silicon Valley, CA 94025"

        new_order = Order(
            order_number=order_num,
            user_id=user.id,
            status=OrderStatus.CONFIRMED.value,
            subtotal=subtotal,
            tax_amount=tax,
            shipping_amount=shipping,
            total_amount=total,
            shipping_address=final_address,
            payment_status="SIMULATED_PAID",
            confirmation_code=confirmation_token,
            created_at=now,
            updated_at=now
        )
        db.add(new_order)
        db.flush()

        for oi in order_items_to_create:
            oi.order_id = new_order.id
            db.add(oi)

        # Clear user cart
        db.query(CartItem).filter(CartItem.cart_id == cart.id).delete()
        db.commit()
        db.refresh(new_order)

        return new_order

    @staticmethod
    def get_order_by_number(db: Session, order_number: str, user_id: Optional[int] = None) -> Optional[Order]:
        query = db.query(Order).filter(Order.order_number == order_number)
        if user_id:
            query = query.filter(Order.user_id == user_id)
        return query.first()

    @staticmethod
    def get_order_by_id(db: Session, order_id: int, user_id: Optional[int] = None) -> Optional[Order]:
        query = db.query(Order).filter(Order.id == order_id)
        if user_id:
            query = query.filter(Order.user_id == user_id)
        return query.first()

    @staticmethod
    def list_user_orders(db: Session, user_id: int) -> List[Order]:
        return db.query(Order).filter(Order.user_id == user_id).order_by(Order.created_at.desc()).all()

    @staticmethod
    def track_order(db: Session, order_number: str, user_id: Optional[int] = None) -> OrderTrackingOut:
        order = OrderService.get_order_by_number(db, order_number, user_id)
        if not order:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Order not found.")

        created = order.created_at
        timeline = [
            OrderTrackingStep(
                status=OrderStatus.CONFIRMED.value,
                title="Order Confirmed",
                description="Your simulated order has been verified and inventory allocated.",
                timestamp=created,
                is_completed=True,
                is_current=(order.status == OrderStatus.CONFIRMED.value)
            ),
            OrderTrackingStep(
                status=OrderStatus.PROCESSING.value,
                title="Processing in Fulfillment Center",
                description="Items packaged and quality checked for shipment.",
                timestamp=created + datetime.timedelta(hours=2),
                is_completed=(order.status in [OrderStatus.PROCESSING.value, OrderStatus.SHIPPED.value, OrderStatus.DELIVERED.value]),
                is_current=(order.status == OrderStatus.PROCESSING.value)
            ),
            OrderTrackingStep(
                status=OrderStatus.SHIPPED.value,
                title="Shipped with Express Courier",
                description="Carrier picked up package. In transit to destination.",
                timestamp=created + datetime.timedelta(days=1),
                is_completed=(order.status in [OrderStatus.SHIPPED.value, OrderStatus.DELIVERED.value]),
                is_current=(order.status == OrderStatus.SHIPPED.value)
            ),
            OrderTrackingStep(
                status=OrderStatus.DELIVERED.value,
                title="Out for Delivery / Delivered",
                description="Package delivered to customer destination.",
                timestamp=created + datetime.timedelta(days=3),
                is_completed=(order.status == OrderStatus.DELIVERED.value),
                is_current=(order.status == OrderStatus.DELIVERED.value)
            ),
        ]

        order_out = OrderOut.from_orm(order)
        return OrderTrackingOut(
            order=order_out,
            timeline=timeline,
            estimated_delivery=created + datetime.timedelta(days=3)
        )


order_service = OrderService()
