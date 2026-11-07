import json
from typing import List, Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.database.session import get_db
from app.models.product import Product
from app.models.user import User
from app.auth.deps import get_current_user_optional
from app.schemas.product import ProductOut
from app.rag.engine import rag_engine

router = APIRouter()


def _format_product(p: Product) -> ProductOut:
    specs = json.loads(p.specifications) if p.specifications and isinstance(p.specifications, str) else p.specifications
    feats = json.loads(p.features) if p.features and isinstance(p.features, str) else p.features
    tags = json.loads(p.tags) if p.tags and isinstance(p.tags, str) else p.tags

    return ProductOut(
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
        specifications=specs,
        features=feats,
        tags=tags,
        image_url=p.image_url,
        created_at=p.created_at,
        updated_at=p.updated_at
    )


@router.get("", response_model=List[ProductOut])
def get_personalized_recommendations(
    category: Optional[str] = None,
    limit: int = Query(default=6, ge=1, le=20),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db)
):
    """Generate recommendations tailored to customer preferences and category interest."""
    query = "premium high rating top value reliable"
    results = rag_engine.hybrid_search(
        db=db,
        query=query,
        category=category,
        top_k=limit
    )

    items = []
    for prod_dict, sim, score, expl in results:
        p_obj = db.query(Product).filter(Product.id == prod_dict["id"]).first()
        if p_obj:
            items.append(_format_product(p_obj))

    # If RAG didn't find enough, backfill with top-rated products
    if len(items) < limit:
        backfill = db.query(Product).filter(
            Product.is_available.is_(True),
            Product.inventory_count > 0
        ).order_by(Product.rating.desc(), Product.review_count.desc()).limit(limit).all()
        for b in backfill:
            if not any(it.id == b.id for it in items):
                items.append(_format_product(b))

    return items[:limit]


@router.get("/trending", response_model=List[ProductOut])
def get_trending_products(
    limit: int = Query(default=8, ge=1, le=20),
    db: Session = Depends(get_db)
):
    """Retrieve top-rated trending items across all catalog categories."""
    trending = db.query(Product).filter(
        Product.is_available.is_(True),
        Product.inventory_count > 0,
        Product.rating >= 4.5
    ).order_by(Product.review_count.desc(), Product.rating.desc()).limit(limit).all()

    return [_format_product(p) for p in trending]
