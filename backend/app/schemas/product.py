from typing import List, Optional, Dict, Any
from datetime import datetime
from pydantic import BaseModel, Field


class CategoryOut(BaseModel):
    id: int
    name: str
    slug: str
    description: Optional[str] = None
    icon: Optional[str] = "tag"

    class Config:
        from_attributes = True


class ProductBase(BaseModel):
    name: str
    sku: str
    category_name: str
    brand: str
    price: float
    original_price: Optional[float] = None
    rating: float = 4.5
    review_count: int = 0
    inventory_count: int = 10
    is_available: bool = True
    description: str
    specifications: Optional[Dict[str, Any]] = None
    features: Optional[List[str]] = None
    tags: Optional[List[str]] = None
    image_url: Optional[str] = None


class ProductCreate(ProductBase):
    slug: Optional[str] = None
    category_id: Optional[int] = None


class ProductOut(ProductBase):
    id: int
    slug: str
    category_id: Optional[int] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class ProductFilterParams(BaseModel):
    query: Optional[str] = None
    category: Optional[str] = None
    brand: Optional[str] = None
    min_price: Optional[float] = None
    max_price: Optional[float] = None
    in_stock_only: Optional[bool] = False
    min_rating: Optional[float] = None
    sort_by: Optional[str] = "relevance"  # price_asc, price_desc, rating, newest
    page: int = Field(default=1, ge=1)
    page_size: int = Field(default=12, ge=1, le=100)


class ProductListResponse(BaseModel):
    items: List[ProductOut]
    total: int
    page: int
    page_size: int
    total_pages: int


class ProductCompareRequest(BaseModel):
    product_ids: List[int] = Field(..., min_length=2, max_length=5)


class ProductCompareItem(BaseModel):
    product: ProductOut
    pros: List[str]
    cons: List[str]
    best_for: str


class ProductCompareResponse(BaseModel):
    products: List[ProductOut]
    comparison_matrix: Dict[str, Dict[str, Any]]  # spec_key -> {product_id -> value}
    recommendation_summary: str
    winner_product_id: Optional[int] = None
