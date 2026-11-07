import re
from typing import Dict, Any, Optional
from sqlalchemy.orm import Session
from fastapi import HTTPException
from app.services.order_service import order_service
from app.services.cart_service import cart_service
from app.models.user import User


class OrderManagementAgent:
    """
    Order Management Agent:
    - Prepares checkout summaries and asks for explicit confirmation before committing purchase
    - Processes simulated checkout, decrements stock atomically, and produces unique Order IDs
    - Tracks order status and transitions (CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED)
    - Retrieves customer order history
    """

    def __init__(self):
        self.name = "Order Management Agent"

    def execute(
        self,
        db: Session,
        query: str,
        user: Optional[User] = None,
        session_token: Optional[str] = None,
        confirmation_payload: Optional[Dict[str, Any]] = None,
        context: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        context = context or {}
        q_low = query.lower()

        # Check action: Track Order
        track_match = re.search(r"\b(ORD-\d{8}-[A-Z0-9]{4,8})\b", query, re.IGNORECASE)
        is_tracking_intent = "track" in q_low or "status" in q_low or "where is my order" in q_low or track_match

        if is_tracking_intent and (track_match or user):
            order_number = track_match.group(1).upper() if track_match else None

            if not order_number and user:
                # Find most recent order
                recent_orders = order_service.list_user_orders(db, user.id)
                if recent_orders:
                    order_number = recent_orders[0].order_number

            if order_number:
                try:
                    tracking = order_service.track_order(db, order_number, user_id=user.id if user else None)
                    current_step = next((s for s in tracking.timeline if s.is_current), tracking.timeline[0])

                    thought = f"Retrieved real-time logistics tracking for order {order_number}."
                    lines = [
                        f"### Tracking Order: **{order_number}**\n",
                        f"- **Current Status:** `{tracking.order.status}` - **{current_step.title}**",
                        f"- **Details:** {current_step.description}",
                        f"- **Estimated Delivery:** {tracking.estimated_delivery.strftime('%B %d, %Y')}",
                        f"- **Total Paid (Simulated):** ${tracking.order.total_amount:.2f} ({len(tracking.order.items)} items)\n",
                        "**Logistics Timeline:**"
                    ]
                    for s in tracking.timeline:
                        check = "✅" if s.is_completed else "⏳"
                        active = "👉 " if s.is_current else "   "
                        lines.append(f"{active}{check} **{s.title}** ({s.status})")

                    return {
                        "agent_name": self.name,
                        "thought": thought,
                        "response": "\n".join(lines),
                        "order_details": tracking.order.dict(),
                        "suggested_actions": ["Track another order", "View all past orders", "Shop again"]
                    }
                except HTTPException as e:
                    return {
                        "agent_name": self.name,
                        "thought": f"Order tracking error: {e.detail}",
                        "response": f"I couldn't locate that order: {e.detail}",
                        "suggested_actions": ["View past orders", "Contact support"]
                    }

        # Check action: Order history
        if any(w in q_low for w in ["order history", "past orders", "my orders", "previous orders"]):
            if not user:
                return {
                    "agent_name": self.name,
                    "thought": "Authentication needed for order history.",
                    "response": "Please log in to your account to view your past orders and receipts.",
                    "suggested_actions": ["Log In", "Search products"]
                }

            orders = order_service.list_user_orders(db, user.id)
            thought = f"Retrieved {len(orders)} past orders for user #{user.id}."
            if not orders:
                return {
                    "agent_name": self.name,
                    "thought": thought,
                    "response": "You haven't placed any simulated orders yet! Explore our products to start shopping.",
                    "suggested_actions": ["Browse electronics", "View cart"]
                }

            lines = [f"You have **{len(orders)}** previous order{'s' if len(orders) > 1 else ''}:\n"]
            for ord_obj in orders[:5]:
                lines.append(
                    f"- **{ord_obj.order_number}** • Status: `{ord_obj.status}` • "
                    f"**${ord_obj.total_amount:.2f}** ({ord_obj.created_at.strftime('%Y-%m-%d')})"
                )
            lines.append("\nYou can say 'Track ORD-...' for step-by-step carrier tracking updates!")

            return {
                "agent_name": self.name,
                "thought": thought,
                "response": "\n".join(lines),
                "suggested_actions": [f"Track {orders[0].order_number}", "Browse catalog"]
            }

        # Check checkout intent
        if not user:
            return {
                "agent_name": self.name,
                "thought": "User must be authenticated to checkout.",
                "response": "To proceed to checkout and secure your order, please log in or register an account. Your cart items will be saved!",
                "suggested_actions": ["Log In", "Register", "View cart"]
            }

        # Check if this is an explicit confirmation of a pending checkout
        if confirmation_payload and confirmation_payload.get("confirmation_token"):
            conf_token = confirmation_payload.get("confirmation_token")
            confirmed = confirmation_payload.get("confirmed", False)
            address = confirmation_payload.get("shipping_address")

            try:
                created_order = order_service.execute_checkout(
                    db=db,
                    user=user,
                    confirmation_token=conf_token,
                    confirmed=confirmed,
                    shipping_address=address
                )
                thought = f"Authorized simulated purchase with token {conf_token}. Created order {created_order.order_number}."
                lines = [
                    f"🎉 **Congratulations! Your simulated order has been placed successfully!**\n",
                    f"- **Order ID:** `{created_order.order_number}`",
                    f"- **Status:** `{created_order.status}`",
                    f"- **Items Total:** ${created_order.total_amount:.2f}",
                    f"- **Shipping Address:** {created_order.shipping_address}",
                    f"- **Payment Method:** Simulated Sandbox Express\n",
                    f"Your inventory allocation has been reserved. You can track this package anytime by saying `Track {created_order.order_number}`."
                ]

                return {
                    "agent_name": self.name,
                    "thought": thought,
                    "response": "\n".join(lines),
                    "order_details": {
                        "id": created_order.id,
                        "order_number": created_order.order_number,
                        "status": created_order.status,
                        "total_amount": created_order.total_amount,
                        "shipping_address": created_order.shipping_address,
                    },
                    "confirmation_required": False,
                    "suggested_actions": [
                        f"Track {created_order.order_number}",
                        "View order receipt",
                        "Continue shopping"
                    ]
                }
            except HTTPException as e:
                return {
                    "agent_name": self.name,
                    "thought": f"Checkout execution failed: {e.detail}",
                    "response": f"Failed to complete checkout: {e.detail}",
                    "confirmation_required": False,
                    "suggested_actions": ["Review cart", "Try again"]
                }

        # Otherwise: Prepare checkout summary and request explicit confirmation
        shipping_address = "742 Evergreen Terrace, Springfield, OR 97477"
        # Check if address is mentioned in text
        addr_match = re.search(r"(?:ship to|address:?)\s*(.+)$", query, re.IGNORECASE)
        if addr_match:
            shipping_address = addr_match.group(1).strip()

        try:
            prep = order_service.prepare_checkout(
                db=db,
                user=user,
                shipping_address=shipping_address
            )

            thought = "Prepared order summary; awaiting explicit user authorization before financial and inventory commit."

            lines = [
                f"### 📋 Checkout Review & Authorization Required\n",
                f"Please review your order details before confirming purchase:\n",
                f"- **Items ({prep.item_count}):**",
            ]
            for it in prep.items_summary:
                lines.append(f"  • {it['quantity']}x {it['name']} – ${it['total_price']:.2f}")

            lines.extend([
                f"\n- **Subtotal:** ${prep.subtotal:.2f}",
                f"- **Estimated Tax (8%):** ${prep.tax_amount:.2f}",
                f"- **Shipping:** {'FREE' if prep.shipping_amount == 0 else f'${prep.shipping_amount:.2f}'}",
                f"- **Grand Total:** **${prep.total_amount:.2f}**",
                f"- **Shipping To:** {prep.shipping_address}\n",
                f"🔒 **Explicit Confirmation Required:** Click the button below or reply 'Confirm purchase' to authorize this order."
            ])

            return {
                "agent_name": self.name,
                "thought": thought,
                "response": "\n".join(lines),
                "confirmation_required": True,
                "confirmation_token": prep.confirmation_token,
                "shipping_address": prep.shipping_address,
                "order_summary": prep.dict(),
                "suggested_actions": [
                    "Confirm purchase",
                    "Change shipping address",
                    "Cancel and return to cart"
                ]
            }
        except HTTPException as e:
            return {
                "agent_name": self.name,
                "thought": f"Preparation rejected: {e.detail}",
                "response": f"Cannot proceed to checkout: {e.detail}",
                "suggested_actions": ["Browse catalog", "View cart"]
            }


order_agent = OrderManagementAgent()
