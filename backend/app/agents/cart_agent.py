import re
from typing import Dict, Any, List, Optional
from sqlalchemy.orm import Session
from fastapi import HTTPException
from app.services.cart_service import cart_service
from app.services.product_service import product_service
from app.models.user import User


class CartManagementAgent:
    """
    Cart Management Agent:
    - Add, update, and remove items from user-specific shopping cart
    - Validate real-time inventory levels
    - Calculate subtotal, taxes, shipping, and grand total
    - Request confirmation for ambiguous actions or high quantities
    """

    def __init__(self):
        self.name = "Cart Management Agent"

    def execute(
        self,
        db: Session,
        query: str,
        user: Optional[User] = None,
        session_token: Optional[str] = None,
        context: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        context = context or {}
        cart = cart_service.get_or_create_cart(
            db,
            user_id=user.id if user else None,
            session_token=session_token
        )

        q_low = query.lower()

        # Check action: show / view cart
        is_view_intent = (
            any(w in q_low for w in ["view", "show", "what is in", "check", "see", "inspect", "open", "display"]) and "cart" in q_low
        ) or q_low.strip() in ["cart", "my cart", "shopping cart", "cart details", "view my cart", "show my cart"]

        # Ensure it's not an add or remove intent
        if is_view_intent and not any(w in q_low for w in ["add", "put", "remove", "delete", "clear", "empty", "drop"]):
            cart_data = cart_service.build_cart_response(cart)
            thought = f"Retrieved current cart contents ({cart_data.summary.item_count} items)."

            if not cart_data.items:
                return {
                    "agent_name": self.name,
                    "thought": thought,
                    "response": "Your shopping cart is currently empty. Would you like me to find popular electronics or clothing deals for you?",
                    "cart_summary": cart_data.summary.dict(),
                    "suggested_actions": ["Search wireless headphones", "View trending products", "Browse laptops"]
                }

            lines = [f"Here is your current shopping cart ({cart_data.summary.item_count} items):"]
            for it in cart_data.items:
                p_name = it.product.name if it.product else f"Product #{it.product_id}"
                lines.append(f"- **{it.quantity}x {p_name}** – ${it.unit_price:.2f} each (${it.total_price:.2f})")

            lines.append(
                f"\n**Subtotal:** ${cart_data.summary.subtotal:.2f}\n"
                f"**Tax (8%):** ${cart_data.summary.tax_estimated:.2f}\n"
                f"**Shipping:** {'FREE' if cart_data.summary.shipping_estimated == 0 else f'${cart_data.summary.shipping_estimated:.2f}'}\n"
                f"**Estimated Total:** **${cart_data.summary.total:.2f}**\n\n"
                f"Would you like to proceed to simulated checkout or make any adjustments?"
            )

            return {
                "agent_name": self.name,
                "thought": thought,
                "response": "\n".join(lines),
                "cart_summary": cart_data.dict(),
                "suggested_actions": ["Proceed to checkout", "Clear cart", "Add more items"]
            }

        # Check action: clear cart
        if "clear cart" in q_low or "empty cart" in q_low:
            cart_service.clear_cart(db, cart)
            return {
                "agent_name": self.name,
                "thought": "Cleared all items from user cart.",
                "response": "Your shopping cart has been cleared.",
                "cart_summary": {"item_count": 0, "total": 0.0},
                "suggested_actions": ["Search products", "Browse categories"]
            }

        # Parse quantity (e.g. "add 2 of product #5")
        quantity = 1
        explicit_qty = re.search(r"(?:add|buy|get)\s+(\d{1,2})\s+", query, re.IGNORECASE)
        if explicit_qty:
            try:
                quantity = int(explicit_qty.group(1))
            except ValueError:
                quantity = 1

        # Extract product ID
        target_product_id = None
        id_match = re.search(r"(?:product|id|item|#)\s*(\d+)", query, re.IGNORECASE)
        if id_match:
            try:
                target_product_id = int(id_match.group(1))
            except ValueError:
                pass
        elif context.get("candidate_ids"):
            target_product_id = context["candidate_ids"][0]

        # Check remove action
        is_remove = any(w in q_low for w in ["remove", "delete", "take out", "drop"])

        if is_remove:
            if not target_product_id:
                for it in cart.items:
                    if it.product and it.product.name.lower() in q_low:
                        target_product_id = it.product_id
                        break

            if target_product_id:
                removed = cart_service.remove_item(db, cart, target_product_id)
                cart_data = cart_service.build_cart_response(cart)
                if removed:
                    return {
                        "agent_name": self.name,
                        "thought": f"Removed product #{target_product_id} from cart.",
                        "response": f"Removed product #{target_product_id} from your cart. New cart total: **${cart_data.summary.total:.2f}** ({cart_data.summary.item_count} items).",
                        "cart_summary": cart_data.dict(),
                        "suggested_actions": ["View cart", "Proceed to checkout", "Continue shopping"]
                    }
                else:
                    return {
                        "agent_name": self.name,
                        "thought": f"Product #{target_product_id} was not present in cart.",
                        "response": f"Product #{target_product_id} was not found in your cart.",
                        "cart_summary": cart_data.dict(),
                        "suggested_actions": ["View cart", "Browse products"]
                    }
            else:
                return {
                    "agent_name": self.name,
                    "thought": "Ambiguous product target for removal.",
                    "response": "Which item would you like to remove from your cart? Please specify the product name or ID.",
                    "suggested_actions": ["View cart"]
                }

        # Otherwise: Add to cart
        if not target_product_id:
            search_term = re.sub(r"(?:add|put|to|in|my|cart|please|can|you|item|buy)\s*", "", query, flags=re.IGNORECASE).strip()
            if search_term and len(search_term) > 2:
                candidates, _ = product_service.filter_products(
                    db,
                    params=type("Params", (), {
                        "category": None, "brand": None, "min_price": None, "max_price": None,
                        "min_rating": None, "in_stock_only": True, "query": search_term,
                        "sort_by": "rating", "page": 1, "page_size": 1
                    })()
                )
                if candidates:
                    target_product_id = candidates[0].id

        if not target_product_id:
            return {
                "agent_name": self.name,
                "thought": "Missing product reference for cart addition.",
                "response": "Which product would you like to add to your cart? You can provide a product name, ID, or pick from recommendations.",
                "suggested_actions": ["Find wireless headphones", "Browse laptops", "View cart"]
            }

        # Guard against unusually large quantities without explicit confirmation
        if quantity > 5:
            return {
                "agent_name": self.name,
                "thought": f"High quantity ({quantity}) requested; flagging for confirmation.",
                "confirmation_required": True,
                "response": f"You requested {quantity} units of product #{target_product_id}. Please confirm if you wish to add this quantity.",
                "suggested_actions": [f"Confirm add {quantity} units", "Add 1 unit instead", "Cancel"]
            }

        try:
            cart_item = cart_service.add_to_cart(db, cart, target_product_id, quantity)
            cart_data = cart_service.build_cart_response(cart)
            prod = cart_item.product

            thought = f"Validated inventory ({prod.inventory_count} available) and added {quantity}x '{prod.name}' to cart."
            response_text = (
                f"Added **{quantity}x {prod.name}** to your shopping cart!\n\n"
                f"- **Unit Price:** ${cart_item.unit_price:.2f}\n"
                f"- **Cart Total:** **${cart_data.summary.total:.2f}** ({cart_data.summary.item_count} items total)\n\n"
                f"Would you like to proceed to simulated checkout or continue shopping?"
            )

            return {
                "agent_name": self.name,
                "thought": thought,
                "response": response_text,
                "cart_summary": cart_data.dict(),
                "suggested_actions": [
                    "Proceed to checkout",
                    "View cart details",
                    "Find matching accessories"
                ]
            }
        except HTTPException as e:
            return {
                "agent_name": self.name,
                "thought": f"Inventory or validation error: {e.detail}",
                "response": f"I couldn't add that product to your cart: {e.detail}",
                "suggested_actions": ["Find alternative products", "View cart"]
            }


cart_agent = CartManagementAgent()
