import json
from typing import Optional, Tuple
from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from app.models.cart import ShoppingCart, CartItem
from app.models.product import Product
from app.schemas.cart import CartOut, CartSummary, CartItemOut
from app.schemas.product import ProductOut


class CartService:
    @staticmethod
    def get_or_create_cart(db: Session, user_id: Optional[int] = None, session_token: Optional[str] = None) -> ShoppingCart:
        cart = None
        if user_id:
            cart = db.query(ShoppingCart).filter(ShoppingCart.user_id == user_id).first()
        elif session_token:
            cart = db.query(ShoppingCart).filter(ShoppingCart.session_token == session_token).first()

        if not cart:
            cart = ShoppingCart(user_id=user_id, session_token=session_token)
            db.add(cart)
            db.commit()
            db.refresh(cart)

        return cart

    @staticmethod
    def add_to_cart(db: Session, cart: ShoppingCart, product_id: int, quantity: int = 1) -> CartItem:
        if quantity <= 0:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Quantity must be greater than zero.")

        product = db.query(Product).filter(Product.id == product_id).first()
        if not product:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found.")

        if not product.is_available or product.inventory_count <= 0:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"'{product.name}' is currently out of stock.")

        # Check existing item
        cart_item = db.query(CartItem).filter(
            CartItem.cart_id == cart.id,
            CartItem.product_id == product_id
        ).first()

        new_total_qty = (cart_item.quantity + quantity) if cart_item else quantity
        if new_total_qty > product.inventory_count:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Requested quantity ({new_total_qty}) exceeds available inventory ({product.inventory_count})."
            )

        if cart_item:
            cart_item.quantity = new_total_qty
            cart_item.unit_price = product.price
        else:
            cart_item = CartItem(
                cart_id=cart.id,
                product_id=product_id,
                quantity=quantity,
                unit_price=product.price
            )
            db.add(cart_item)

        db.commit()
        db.refresh(cart)
        db.refresh(cart_item)
        return cart_item

    @staticmethod
    def update_quantity(db: Session, cart: ShoppingCart, product_id: int, quantity: int) -> Optional[CartItem]:
        cart_item = db.query(CartItem).filter(
            CartItem.cart_id == cart.id,
            CartItem.product_id == product_id
        ).first()

        if not cart_item:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Item not in cart.")

        if quantity <= 0:
            db.delete(cart_item)
            db.commit()
            return None

        product = db.query(Product).filter(Product.id == product_id).first()
        if product and quantity > product.inventory_count:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Requested quantity ({quantity}) exceeds available inventory ({product.inventory_count})."
            )

        cart_item.quantity = quantity
        db.commit()
        db.refresh(cart_item)
        return cart_item

    @staticmethod
    def remove_item(db: Session, cart: ShoppingCart, product_id: int) -> bool:
        cart_item = db.query(CartItem).filter(
            CartItem.cart_id == cart.id,
            CartItem.product_id == product_id
        ).first()

        if cart_item:
            db.delete(cart_item)
            db.commit()
            return True
        return False

    @staticmethod
    def clear_cart(db: Session, cart: ShoppingCart):
        db.query(CartItem).filter(CartItem.cart_id == cart.id).delete()
        db.commit()

    @staticmethod
    def build_cart_response(cart: ShoppingCart) -> CartOut:
        subtotal = 0.0
        item_count = 0
        items_out = []

        for item in cart.items:
            p = item.product
            item_total = round(item.quantity * item.unit_price, 2)
            subtotal += item_total
            item_count += item.quantity

            p_out = None
            if p:
                p_out = ProductOut(
                    id=p.id,
                    name=p.name,
                    sku=p.sku,
                    slug=p.slug,
                    category_id=p.category_id,
                    category_name=p.category_name,
                    brand=p.brand,
                    price=p.price,
                    original_price=p.original_price,
                    rating=p.rating,
                    review_count=p.review_count,
                    inventory_count=p.inventory_count,
                    is_available=p.is_available,
                    description=p.description,
                    specifications=json.loads(p.specifications) if p.specifications and isinstance(p.specifications, str) else p.specifications,
                    features=json.loads(p.features) if p.features and isinstance(p.features, str) else p.features,
                    tags=json.loads(p.tags) if p.tags and isinstance(p.tags, str) else p.tags,
                    image_url=p.image_url,
                    created_at=p.created_at,
                    updated_at=p.updated_at
                )

            items_out.append(
                CartItemOut(
                    id=item.id,
                    product_id=item.product_id,
                    quantity=item.quantity,
                    unit_price=item.unit_price,
                    total_price=item_total,
                    product=p_out,
                    created_at=item.created_at
                )
            )

        subtotal = round(subtotal, 2)
        tax = round(subtotal * 0.08, 2)
        shipping = 0.0 if (subtotal >= 50.0 or subtotal == 0) else 9.99
        total = round(subtotal + tax + shipping, 2)

        summary = CartSummary(
            subtotal=subtotal,
            tax_estimated=tax,
            shipping_estimated=shipping,
            total=total,
            item_count=item_count
        )

        return CartOut(
            id=cart.id,
            user_id=cart.user_id,
            items=items_out,
            summary=summary
        )


cart_service = CartService()
