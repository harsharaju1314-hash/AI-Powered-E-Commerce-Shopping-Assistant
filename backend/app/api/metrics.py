import time
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from app.database.session import get_db
from app.models.product import Product
from app.models.order import Order
from app.models.user import User
from app.rag.engine import rag_engine
from app.schemas.rag import RAGEvaluationMetric

router = APIRouter()


@router.get("/health")
def health_check(db: Session = Depends(get_db)):
    """Health check endpoint to verify database connectivity and system status."""
    start_time = time.time()
    try:
        # Quick query
        product_count = db.query(func.count(Product.id)).scalar() or 0
        db_status = "healthy"
    except Exception as e:
        product_count = 0
        db_status = f"unhealthy: {str(e)}"

    latency_ms = round((time.time() - start_time) * 1000, 2)

    return {
        "status": "healthy" if db_status == "healthy" else "degraded",
        "database": db_status,
        "database_latency_ms": latency_ms,
        "catalog_size": product_count,
        "version": "1.0.0",
        "sub_300ms_compliant": latency_ms < 300
    }


@router.get("/rag-evaluation", response_model=RAGEvaluationMetric)
def evaluate_rag_retrieval(db: Session = Depends(get_db)):
    """
    Evaluates RAG retrieval precision against the labeled ground-truth dataset.
    Target: >= 90% Precision@5.
    """
    return rag_engine.evaluate_retrieval_precision(db, top_k=5)


@router.get("/system-summary")
def get_system_summary(db: Session = Depends(get_db)):
    """Overview of platform metrics, active agents, catalog, and orders."""
    product_count = db.query(func.count(Product.id)).scalar() or 0
    order_count = db.query(func.count(Order.id)).scalar() or 0
    user_count = db.query(func.count(User.id)).scalar() or 0

    return {
        "platform_name": "AI-Powered E-Commerce Shopping Assistant",
        "agents": {
            "total_count": 4,
            "orchestrator": "LangGraph StateGraph",
            "active_agents": [
                {
                    "name": "Product Discovery Agent",
                    "role": "Semantic hybrid search and constraint filtering",
                    "status": "active"
                },
                {
                    "name": "Recommendation Agent",
                    "role": "Comparative analysis and grounded RAG synthesis",
                    "status": "active"
                },
                {
                    "name": "Cart Management Agent",
                    "role": "Inventory-aware cart updates and pricing calculation",
                    "status": "active"
                },
                {
                    "name": "Order Management Agent",
                    "role": "Explicit authorization, checkout execution, and tracking",
                    "status": "active"
                }
            ]
        },
        "catalog": {
            "total_products": product_count,
            "target_scale": "5,000+ records",
            "categories": ["Electronics", "Clothing & Apparel", "Home & Kitchen", "Accessories & Wearables"]
        },
        "orders_simulated": order_count,
        "users_registered": user_count,
        "rag_engine": {
            "status": "ready" if rag_engine.is_indexed else "indexing_on_first_query",
            "indexed_documents": len(rag_engine.product_ids)
        }
    }
