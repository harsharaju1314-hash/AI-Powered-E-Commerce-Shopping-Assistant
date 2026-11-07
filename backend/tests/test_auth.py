import uuid
import pytest
from fastapi.testclient import TestClient


def test_register_success(client: TestClient):
    unique_email = f"test_{uuid.uuid4().hex[:8]}@example.com"
    payload = {
        "email": unique_email,
        "full_name": "Test Customer",
        "password": "SecurePassword123"
    }
    response = client.post("/api/auth/register", json=payload)
    assert response.status_code == 201
    data = response.json()
    assert "access_token" in data
    assert data["token_type"] == "bearer"
    assert data["user"]["email"] == unique_email


def test_register_duplicate_email(client: TestClient):
    payload = {
        "email": "customer@example.com",
        "full_name": "Duplicate User",
        "password": "password123"
    }
    response = client.post("/api/auth/register", json=payload)
    assert response.status_code == 400
    assert "already exists" in response.json()["detail"]


def test_login_success(client: TestClient):
    payload = {
        "email": "customer@example.com",
        "password": "password123"
    }
    response = client.post("/api/auth/login", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert data["user"]["email"] == "customer@example.com"


def test_login_invalid_password(client: TestClient):
    payload = {
        "email": "customer@example.com",
        "password": "WrongPassword!"
    }
    response = client.post("/api/auth/login", json=payload)
    assert response.status_code == 401
    assert "Incorrect email or password" in response.json()["detail"]


def test_me_authenticated(client: TestClient, auth_headers: dict):
    response = client.get("/api/auth/me", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["email"] == "customer@example.com"
    assert "id" in data


def test_me_unauthorized(client: TestClient):
    response = client.get("/api/auth/me")
    assert response.status_code == 401
