import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from app.models.product import Product


def test_prepare_checkout_empty_cart_fails(client: TestClient, auth_headers: dict):
    client.delete("/api/cart/clear", headers=auth_headers)
    response = client.post(
        "/api/orders/prepare",
        json={"shipping_address": "123 Test St, Silicon Valley, CA 94025"},
        headers=auth_headers
    )
    assert response.status_code == 400
    assert "empty" in response.json()["detail"].lower()


def test_prepare_checkout_success(client: TestClient, auth_headers: dict, db_session: Session):
    client.delete("/api/cart/clear", headers=auth_headers)
    product = db_session.query(Product).filter(Product.inventory_count > 5, Product.is_available.is_(True)).first()
    client.post("/api/cart/items", json={"product_id": product.id, "quantity": 1}, headers=auth_headers)

    response = client.post(
        "/api/orders/prepare",
        json={"shipping_address": "456 Innovation Ave, Austin, TX 78701"},
        headers=auth_headers
    )
    assert response.status_code == 200
    data = response.json()
    assert "confirmation_token" in data
    assert data["requires_explicit_confirmation"] is True
    assert data["item_count"] >= 1
    assert data["total_amount"] > 0


def test_confirm_checkout_without_confirmation_flag_fails(client: TestClient, auth_headers: dict, db_session: Session):
    client.delete("/api/cart/clear", headers=auth_headers)
    product = db_session.query(Product).filter(Product.inventory_count > 5, Product.is_available.is_(True)).first()
    client.post("/api/cart/items", json={"product_id": product.id, "quantity": 1}, headers=auth_headers)

    prep_res = client.post(
        "/api/orders/prepare",
        json={"shipping_address": "456 Innovation Ave, Austin, TX 78701"},
        headers=auth_headers
    )
    token = prep_res.json()["confirmation_token"]

    confirm_res = client.post(
        "/api/orders/confirm",
        json={"confirmation_token": token, "confirmed": False},
        headers=auth_headers
    )
    assert confirm_res.status_code == 400
    assert "explicit confirmation is required" in confirm_res.json()["detail"].lower()


def test_confirm_checkout_success_and_decrements_inventory(client: TestClient, auth_headers: dict, db_session: Session):
    client.delete("/api/cart/clear", headers=auth_headers)
    product = db_session.query(Product).filter(Product.inventory_count > 5, Product.is_available.is_(True)).first()
    initial_stock = product.inventory_count
    client.post("/api/cart/items", json={"product_id": product.id, "quantity": 2}, headers=auth_headers)

    prep_res = client.post(
        "/api/orders/prepare",
        json={"shipping_address": "789 Robotic Way, Boston, MA 02115"},
        headers=auth_headers
    )
    token = prep_res.json()["confirmation_token"]

    confirm_res = client.post(
        "/api/orders/confirm",
        json={
            "confirmation_token": token,
            "confirmed": True,
            "shipping_address": "789 Robotic Way, Boston, MA 02115"
        },
        headers=auth_headers
    )
    assert confirm_res.status_code == 200
    order_data = confirm_res.json()
    assert order_data["status"] == "CONFIRMED"
    assert order_data["order_number"].startswith("ORD-")
    assert order_data["payment_status"] == "SIMULATED_PAID"

    # Verify inventory was decremented
    db_session.refresh(product)
    assert product.inventory_count == initial_stock - 2


def test_duplicate_checkout_prevention(client: TestClient, auth_headers: dict, db_session: Session):
    client.delete("/api/cart/clear", headers=auth_headers)
    product = db_session.query(Product).filter(Product.inventory_count > 5).first()
    client.post("/api/cart/items", json={"product_id": product.id, "quantity": 1}, headers=auth_headers)

    prep_res = client.post(
        "/api/orders/prepare",
        json={"shipping_address": "123 Duplication St, Seattle, WA 98101"},
        headers=auth_headers
    )
    token = prep_res.json()["confirmation_token"]

    # First checkout
    res1 = client.post("/api/orders/confirm", json={"confirmation_token": token, "confirmed": True}, headers=auth_headers)
    assert res1.status_code == 200

    # Second checkout with same confirmation code
    res2 = client.post("/api/orders/confirm", json={"confirmation_token": token, "confirmed": True}, headers=auth_headers)
    assert res2.status_code in [400, 409]
    assert "duplicate" in res2.json()["detail"].lower() or "empty" in res2.json()["detail"].lower()


def test_order_tracking_timeline(client: TestClient, auth_headers: dict):
    # Fetch user orders
    orders_res = client.get("/api/orders/user", headers=auth_headers)
    assert orders_res.status_code == 200
    orders = orders_res.json()
    assert len(orders) > 0

    order_num = orders[0]["order_number"]
    track_res = client.get(f"/api/orders/track/{order_num}", headers=auth_headers)
    assert track_res.status_code == 200
    track_data = track_res.json()
    assert track_data["order"]["order_number"] == order_num
    assert len(track_data["timeline"]) == 4
    assert track_data["timeline"][0]["status"] == "CONFIRMED"
    assert track_data["timeline"][0]["is_completed"] is True


def test_api_health_endpoint(client: TestClient):
    response = client.get("/api/metrics/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert data["database"] == "healthy"
    assert data["catalog_size"] >= 5000
    assert data["sub_300ms_compliant"] is True
