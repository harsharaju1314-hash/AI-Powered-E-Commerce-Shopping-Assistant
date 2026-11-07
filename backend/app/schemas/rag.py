from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from app.schemas.product import ProductOut


class RAGQueryRequest(BaseModel):
    query: str = Field(..., min_length=2)
    category: Optional[str] = None
    brand: Optional[str] = None
    min_price: Optional[float] = None
    max_price: Optional[float] = None
    top_k: int = Field(default=5, ge=1, le=20)


class RAGSearchResult(BaseModel):
    product: ProductOut
    similarity_score: float
    hybrid_score: float
    relevance_explanation: str


class RAGSearchResponse(BaseModel):
    query: str
    results: List[RAGSearchResult]
    total_retrieved: int
    context_text: str


class RAGEvaluationQuery(BaseModel):
    query_id: str
    query_text: str
    target_category: Optional[str] = None
    target_brand: Optional[str] = None
    max_price: Optional[float] = None
    ground_truth_product_ids: List[int] = []
    ground_truth_keywords: List[str] = []


class RAGEvaluationMetric(BaseModel):
    precision_at_5: float
    precision_at_3: float
    precision_at_1: float
    mean_reciprocal_rank: float
    total_queries_evaluated: int
    successful_queries: int
    benchmark_passed: bool  # >= 90% Precision@5
    details: List[Dict[str, Any]] = []
