import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from app.models.product import Product


def test_get_empty_cart(client: TestClient, auth_headers: dict):
    # Clear cart first
    client.delete("/api/cart/clear", headers=auth_headers)
    response = client.get("/api/cart", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["summary"]["item_count"] == 0
    assert data["summary"]["total"] == 0.0


def test_add_product_to_cart(client: TestClient, auth_headers: dict, db_session: Session):
    product = db_session.query(Product).filter(Product.inventory_count > 5, Product.is_available.is_(True)).first()
    assert product is not None

    payload = {"product_id": product.id, "quantity": 2}
    response = client.post("/api/cart/items", json=payload, headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["summary"]["item_count"] >= 2
    matched = [it for it in data["items"] if it["product_id"] == product.id]
    assert len(matched) == 1
    assert matched[0]["quantity"] == 2


def test_update_cart_quantity(client: TestClient, auth_headers: dict, db_session: Session):
    product = db_session.query(Product).filter(Product.inventory_count > 5, Product.is_available.is_(True)).first()
    payload = {"quantity": 3}
    response = client.put(f"/api/cart/items/{product.id}", json=payload, headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    matched = next(it for it in data["items"] if it["product_id"] == product.id)
    assert matched["quantity"] == 3


def test_update_quantity_zero_removes_item(client: TestClient, auth_headers: dict, db_session: Session):
    product = db_session.query(Product).filter(Product.inventory_count > 5, Product.is_available.is_(True)).first()
    payload = {"quantity": 0}
    response = client.put(f"/api/cart/items/{product.id}", json=payload, headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    matched = [it for it in data["items"] if it["product_id"] == product.id]
    assert len(matched) == 0


def test_add_out_of_stock_fails(client: TestClient, auth_headers: dict, db_session: Session):
    # Find or set an out-of-stock product
    oos = db_session.query(Product).filter(Product.inventory_count == 0).first()
    if not oos:
        oos = db_session.query(Product).first()
        oos.inventory_count = 0
        oos.is_available = False
        db_session.commit()

    payload = {"product_id": oos.id, "quantity": 1}
    response = client.post("/api/cart/items", json=payload, headers=auth_headers)
    assert response.status_code == 400
    assert "out of stock" in response.json()["detail"].lower()


def test_cart_tax_and_shipping_rules(client: TestClient, auth_headers: dict, db_session: Session):
    client.delete("/api/cart/clear", headers=auth_headers)
    # Add a product > $50 for free shipping test
    product = db_session.query(Product).filter(Product.price > 60.0, Product.inventory_count > 5).first()
    client.post("/api/cart/items", json={"product_id": product.id, "quantity": 1}, headers=auth_headers)

    response = client.get("/api/cart", headers=auth_headers)
    summary = response.json()["summary"]
    assert summary["subtotal"] >= 60.0
    assert summary["shipping_estimated"] == 0.0  # Free shipping over $50
    assert summary["tax_estimated"] == round(summary["subtotal"] * 0.08, 2)
    assert summary["total"] == round(summary["subtotal"] + summary["tax_estimated"], 2)


def test_remove_cart_item(client: TestClient, auth_headers: dict, db_session: Session):
    product = db_session.query(Product).filter(Product.inventory_count > 5).first()
    client.post("/api/cart/items", json={"product_id": product.id, "quantity": 1}, headers=auth_headers)

    del_res = client.delete(f"/api/cart/items/{product.id}", headers=auth_headers)
    assert del_res.status_code == 200
    items = del_res.json()["items"]
    assert not any(it["product_id"] == product.id for it in items)
