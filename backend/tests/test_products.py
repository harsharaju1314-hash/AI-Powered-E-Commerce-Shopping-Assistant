import pytest
from fastapi.testclient import TestClient


def test_list_products_pagination(client: TestClient):
    response = client.get("/api/products?page=1&page_size=10")
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert len(data["items"]) == 10
    assert data["total"] >= 5000
    assert data["page"] == 1


def test_filter_by_category(client: TestClient):
    response = client.get("/api/products?category=Electronics&page_size=5")
    assert response.status_code == 200
    data = response.json()
    assert len(data["items"]) > 0
    for p in data["items"]:
        assert p["category_name"] == "Electronics"


def test_filter_by_brand(client: TestClient):
    response = client.get("/api/products?brand=Sony&page_size=5")
    assert response.status_code == 200
    data = response.json()
    assert len(data["items"]) > 0
    for p in data["items"]:
        assert p["brand"].lower() == "sony"


def test_filter_by_price_range(client: TestClient):
    min_p, max_p = 50.0, 150.0
    response = client.get(f"/api/products?min_price={min_p}&max_price={max_p}&page_size=10")
    assert response.status_code == 200
    data = response.json()
    assert len(data["items"]) > 0
    for p in data["items"]:
        assert min_p <= p["price"] <= max_p


def test_filter_in_stock_only(client: TestClient):
    response = client.get("/api/products?in_stock_only=true&page_size=10")
    assert response.status_code == 200
    data = response.json()
    for p in data["items"]:
        assert p["is_available"] is True
        assert p["inventory_count"] > 0


def test_sort_by_price_asc(client: TestClient):
    response = client.get("/api/products?sort_by=price_asc&page_size=10")
    assert response.status_code == 200
    items = response.json()["items"]
    assert len(items) >= 2
    prices = [p["price"] for p in items]
    assert prices == sorted(prices)


def test_get_product_by_id(client: TestClient):
    # Fetch first product
    list_res = client.get("/api/products?page_size=1")
    first_id = list_res.json()["items"][0]["id"]

    response = client.get(f"/api/products/{first_id}")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == first_id
    assert "sku" in data
    assert "price" in data


def test_get_product_not_found(client: TestClient):
    response = client.get("/api/products/9999999")
    assert response.status_code == 404
    assert "not found" in response.json()["detail"].lower()


def test_compare_products_api(client: TestClient):
    list_res = client.get("/api/products?page_size=3")
    ids = [p["id"] for p in list_res.json()["items"]]

    response = client.post("/api/products/compare", json={"product_ids": ids})
    assert response.status_code == 200
    data = response.json()
    assert len(data["products"]) == len(ids)
    assert "comparison_matrix" in data
    assert "recommendation_summary" in data
    assert data["winner_product_id"] in ids
