from fastapi import APIRouter
from app.api import auth, products, cart, orders, chat, recommendations, metrics

api_router = APIRouter()

api_router.include_router(auth.router, prefix="/auth", tags=["Authentication"])
api_router.include_router(products.router, prefix="/products", tags=["Products"])
api_router.include_router(cart.router, prefix="/cart", tags=["Shopping Cart"])
api_router.include_router(orders.router, prefix="/orders", tags=["Orders & Checkout"])
api_router.include_router(chat.router, prefix="/chat", tags=["Multi-Agent AI Chat"])
api_router.include_router(recommendations.router, prefix="/recommendations", tags=["Recommendations"])
api_router.include_router(metrics.router, prefix="/metrics", tags=["Metrics & Benchmarks"])
