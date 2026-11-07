import json
import math
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from app.database.session import get_db
from app.models.product import Product
from app.schemas.product import (
    ProductOut, ProductFilterParams, ProductListResponse,
    ProductCompareRequest, ProductCompareResponse, CategoryOut
)
from app.schemas.rag import RAGSearchResponse, RAGSearchResult
from app.services.product_service import product_service
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


@router.get("", response_model=ProductListResponse)
def list_products(
    query: Optional[str] = None,
    category: Optional[str] = None,
    brand: Optional[str] = None,
    min_price: Optional[float] = None,
    max_price: Optional[float] = None,
    min_rating: Optional[float] = None,
    in_stock_only: Optional[bool] = False,
    sort_by: Optional[str] = "relevance",
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=12, ge=1, le=100),
    db: Session = Depends(get_db)
):
    """Retrieve catalog products with comprehensive filtering, sorting, and pagination."""
    params = ProductFilterParams(
        query=query,
        category=category,
        brand=brand,
        min_price=min_price,
        max_price=max_price,
        min_rating=min_rating,
        in_stock_only=in_stock_only,
        sort_by=sort_by,
        page=page,
        page_size=page_size
    )
    items, total = product_service.filter_products(db, params)
    total_pages = math.ceil(total / page_size) if total > 0 else 0

    return ProductListResponse(
        items=[_format_product(p) for p in items],
        total=total,
        page=page,
        page_size=page_size,
        total_pages=total_pages
    )


@router.get("/search", response_model=RAGSearchResponse)
def semantic_search(
    q: str = Query(..., min_length=2, description="Natural language search query"),
    category: Optional[str] = None,
    brand: Optional[str] = None,
    max_price: Optional[float] = None,
    top_k: int = Query(default=5, ge=1, le=20),
    db: Session = Depends(get_db)
):
    """Semantic hybrid search powered by RAG embeddings and catalog filters."""
    raw_results = rag_engine.hybrid_search(
        db=db,
        query=q,
        category=category,
        brand=brand,
        max_price=max_price,
        top_k=top_k
    )

    results = []
    for prod_dict, sim, score, expl in raw_results:
        p_obj = db.query(Product).filter(Product.id == prod_dict["id"]).first()
        if p_obj:
            results.append(
                RAGSearchResult(
                    product=_format_product(p_obj),
                    similarity_score=round(sim, 3),
                    hybrid_score=round(score, 3),
                    relevance_explanation=expl
                )
            )

    context_text = rag_engine.build_grounded_context(raw_results)

    return RAGSearchResponse(
        query=q,
        results=results,
        total_retrieved=len(results),
        context_text=context_text
    )


@router.get("/categories", response_model=List[CategoryOut])
def get_categories(db: Session = Depends(get_db)):
    """List all product categories."""
    cats = product_service.list_categories(db)
    return [CategoryOut.from_orm(c) for c in cats]


@router.get("/brands", response_model=List[str])
def get_brands(db: Session = Depends(get_db)):
    """List all available brand names."""
    return product_service.list_brands(db)


@router.post("/compare", response_model=ProductCompareResponse)
def compare_products(request: ProductCompareRequest, db: Session = Depends(get_db)):
    """Compare up to 5 products side-by-side with attribute matrices."""
    return product_service.compare_products(db, request.product_ids)


@router.get("/{product_id}", response_model=ProductOut)
def get_product(product_id: int, db: Session = Depends(get_db)):
    """Retrieve full product details by ID."""
    p = product_service.get_by_id(db, product_id)
    if not p:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found.")
    return _format_product(p)


@router.get("/slug/{slug}", response_model=ProductOut)
def get_product_by_slug(slug: str, db: Session = Depends(get_db)):
    """Retrieve product details by URL slug."""
    p = product_service.get_by_slug(db, slug)
    if not p:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found.")
    return _format_product(p)
