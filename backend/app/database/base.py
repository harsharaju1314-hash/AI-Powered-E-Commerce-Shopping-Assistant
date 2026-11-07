# Import all models here for Alembic and Base metadata detection
from app.database.session import Base
from app.models.user import User
from app.models.product import Product, ProductCategory
from app.models.preference import CustomerPreference
from app.models.cart import ShoppingCart, CartItem
from app.models.order import Order, OrderItem
from app.models.chat import ChatSession, ChatMessage

__all__ = [
    "Base",
    "User",
    "Product",
    "ProductCategory",
    "CustomerPreference",
    "ShoppingCart",
    "CartItem",
    "Order",
    "OrderItem",
    "ChatSession",
    "ChatMessage",
]
