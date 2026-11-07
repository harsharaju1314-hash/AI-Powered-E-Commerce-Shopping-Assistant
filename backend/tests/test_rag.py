import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from app.rag.engine import rag_engine


def test_rag_semantic_search_endpoint(client: TestClient):
    response = client.get("/api/products/search?q=wireless+noise+cancelling+headphones&top_k=5")
    assert response.status_code == 200
    data = response.json()
    assert "results" in data
    assert len(data["results"]) > 0
    assert "context_text" in data
    first_result = data["results"][0]
    assert "similarity_score" in first_result
    assert "hybrid_score" in first_result
    assert "relevance_explanation" in first_result


def test_rag_hybrid_filtering(client: TestClient):
    response = client.get("/api/products/search?q=running+shoes&category=Clothing+%26+Apparel&max_price=120&top_k=5")
    assert response.status_code == 200
    data = response.json()
    for res in data["results"]:
        p = res["product"]
        assert p["category_name"] == "Clothing & Apparel"
        assert p["price"] <= 120.0 * 1.05


def test_rag_precision_at_5_benchmark(db_session: Session):
    metric = rag_engine.evaluate_retrieval_precision(db_session, top_k=5)
    assert metric.total_queries_evaluated >= 20
    assert metric.precision_at_5 >= 90.0
    assert metric.benchmark_passed is True
    assert metric.mean_reciprocal_rank >= 0.85


def test_rag_grounded_context_format(db_session: Session):
    results = rag_engine.hybrid_search(db_session, query="mechanical keyboard RGB switches", top_k=3)
    context = rag_engine.build_grounded_context(results)
    assert "Verified Product Catalog Information:" in context
    assert "SKU:" in context
    assert "Customer Rating:" in context


def test_rag_precision_endpoint(client: TestClient):
    response = client.get("/api/metrics/rag-evaluation")
    assert response.status_code == 200
    data = response.json()
    assert data["precision_at_5"] >= 90.0
    assert data["benchmark_passed"] is True
