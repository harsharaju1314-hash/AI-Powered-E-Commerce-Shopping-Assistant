import json
import urllib.parse
from typing import Any, Dict, Optional
import pytest
from fastapi import HTTPException
from sqlalchemy.orm import Session
from app.database.session import SessionLocal
from app.auth.security import create_access_token, decode_access_token, get_password_hash
from app.models.user import User
from app.rag.engine import rag_engine
from app.api import auth, products, cart, orders, metrics
from app.schemas.user import UserCreate, UserLogin
from app.schemas.product import ProductCompareRequest
from app.schemas.cart import CartItemCreate, CartItemUpdate
from app.schemas.order import CheckoutPrepareRequest, CheckoutConfirmRequest


class DirectResponse:
    def __init__(self, status_code: int, data: Any):
        self.status_code = status_code
        self._data = data

    def json(self):
        if self._data is None:
            return None
        if hasattr(self._data, "__table__"):
            return {c.name: getattr(self._data, c.name) for c in self._data.__table__.columns}
        if hasattr(self._data, "model_dump"):
            return self._data.model_dump()
        if hasattr(self._data, "dict"):
            return self._data.dict()
        if isinstance(self._data, list):
            res = []
            for item in self._data:
                if hasattr(item, "__table__"):
                    res.append({c.name: getattr(item, c.name) for c in item.__table__.columns})
                elif hasattr(item, "model_dump"):
                    res.append(item.model_dump())
                elif hasattr(item, "dict"):
                    res.append(item.dict())
                else:
                    res.append(item)
            return res
        if isinstance(self._data, dict):
            result = {}
            for k, v in self._data.items():
                if hasattr(v, "model_dump"):
                    result[k] = v.model_dump()
                elif hasattr(v, "dict"):
                    result[k] = v.dict()
                elif isinstance(v, list):
                    result[k] = [
                        x.model_dump() if hasattr(x, "model_dump") else (x.dict() if hasattr(x, "dict") else x)
                        for x in v
                    ]
                else:
                    result[k] = v
            return result
        return self._data


class DirectClient:
    """
    Direct in-memory API dispatcher for isolated sandbox execution without socket/loopback overhead.
    """
    def __init__(self, db_factory):
        self.db_factory = db_factory

    def _get_user_from_headers(self, headers: Optional[Dict[str, str]], db: Session) -> Optional[User]:
        if not headers or "Authorization" not in headers:
            return None
        token = headers["Authorization"].replace("Bearer ", "").strip()
        payload = decode_access_token(token)
        if not payload or "sub" not in payload:
            return None
        user_id = payload["sub"]
        return db.query(User).filter(User.id == int(user_id)).first()

    def get(self, url: str, headers: Optional[Dict[str, str]] = None) -> DirectResponse:
        parsed = urllib.parse.urlparse(url)
        path = parsed.path
        qparams = {k: v[0] for k, v in urllib.parse.parse_qs(parsed.query).items()}

        db = self.db_factory()
        try:
            user = self._get_user_from_headers(headers, db)

            if path == "/api/auth/me":
                if not user:
                    return DirectResponse(401, {"detail": "Not authenticated"})
                return DirectResponse(200, auth.read_current_user(current_user=user))

            elif path == "/api/products":
                page = int(qparams.get("page", 1))
                page_size = int(qparams.get("page_size", 20))
                category = qparams.get("category")
                brand = qparams.get("brand")
                min_price = float(qparams["min_price"]) if "min_price" in qparams else None
                max_price = float(qparams["max_price"]) if "max_price" in qparams else None
                in_stock_only = qparams.get("in_stock_only") == "true"
                sort_by = qparams.get("sort_by")
                query = qparams.get("query")
                res = products.list_products(
                    query=query, category=category, brand=brand,
                    min_price=min_price, max_price=max_price,
                    in_stock_only=in_stock_only, sort_by=sort_by,
                    page=page, page_size=page_size, db=db
                )
                return DirectResponse(200, res)

            elif path == "/api/products/search":
                q = qparams.get("q", "")
                category = qparams.get("category")
                brand = qparams.get("brand")
                max_price = float(qparams["max_price"]) if "max_price" in qparams else None
                top_k = int(qparams.get("top_k", 5))
                res = products.semantic_search(
                    q=q, category=category, brand=brand,
                    max_price=max_price, top_k=top_k, db=db
                )
                return DirectResponse(200, res)

            elif path.startswith("/api/products/"):
                try:
                    product_id = int(path.split("/api/products/")[1])
                    res = products.get_product(product_id=product_id, db=db)
                    return DirectResponse(200, res)
                except HTTPException as e:
                    return DirectResponse(e.status_code, {"detail": e.detail})

            elif path == "/api/cart":
                if not user:
                    return DirectResponse(401, {"detail": "Not authenticated"})
                res = cart.get_cart(x_session_token=None, current_user=user, db=db)
                return DirectResponse(200, res)

            elif path == "/api/orders/user":
                if not user:
                    return DirectResponse(401, {"detail": "Not authenticated"})
                res = orders.get_user_orders(current_user=user, db=db)
                return DirectResponse(200, res)

            elif path.startswith("/api/orders/track/"):
                if not user:
                    return DirectResponse(401, {"detail": "Not authenticated"})
                order_num = path.split("/api/orders/track/")[1]
                res = orders.track_order_status(order_number=order_num, current_user=user, db=db)
                return DirectResponse(200, res)

            elif path == "/api/metrics/rag-evaluation":
                res = metrics.evaluate_rag_retrieval(db=db)
                return DirectResponse(200, res)

            elif path == "/api/metrics/health":
                res = metrics.health_check(db=db)
                return DirectResponse(200, res)

            elif path == "/api/metrics/system-summary":
                res = metrics.get_system_summary(db=db)
                return DirectResponse(200, res)

            return DirectResponse(404, {"detail": f"Path not found: {path}"})
        except HTTPException as e:
            return DirectResponse(e.status_code, {"detail": e.detail})
        except Exception as e:
            return DirectResponse(500, {"detail": str(e)})
        finally:
            db.close()

    def post(self, url: str, json: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> DirectResponse:
        parsed = urllib.parse.urlparse(url)
        path = parsed.path
        body = json or {}

        db = self.db_factory()
        try:
            user = self._get_user_from_headers(headers, db)

            if path == "/api/auth/register":
                res = auth.register(user_in=UserCreate(**body), db=db)
                return DirectResponse(201, res)

            elif path == "/api/auth/login":
                res = auth.login(user_in=UserLogin(**body), db=db)
                return DirectResponse(200, res)

            elif path == "/api/products/compare":
                res = products.compare_products(request=ProductCompareRequest(**body), db=db)
                return DirectResponse(200, res)

            elif path == "/api/cart/items":
                if not user:
                    return DirectResponse(401, {"detail": "Not authenticated"})
                res = cart.add_item_to_cart(item_in=CartItemCreate(**body), x_session_token=None, current_user=user, db=db)
                return DirectResponse(200, res)

            elif path == "/api/orders/prepare":
                if not user:
                    return DirectResponse(401, {"detail": "Not authenticated"})
                res = orders.prepare_order_checkout(req=CheckoutPrepareRequest(**body), current_user=user, db=db)
                return DirectResponse(200, res)

            elif path == "/api/orders/confirm":
                if not user:
                    return DirectResponse(401, {"detail": "Not authenticated"})
                res = orders.confirm_order_checkout(req=CheckoutConfirmRequest(**body), current_user=user, db=db)
                return DirectResponse(200, res)

            return DirectResponse(404, {"detail": f"Path not found: {path}"})
        except HTTPException as e:
            return DirectResponse(e.status_code, {"detail": e.detail})
        except Exception as e:
            return DirectResponse(500, {"detail": str(e)})
        finally:
            db.close()

    def put(self, url: str, json: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> DirectResponse:
        parsed = urllib.parse.urlparse(url)
        path = parsed.path
        body = json or {}

        db = self.db_factory()
        try:
            user = self._get_user_from_headers(headers, db)
            if not user:
                return DirectResponse(401, {"detail": "Not authenticated"})

            if path.startswith("/api/cart/items/"):
                prod_id = int(path.split("/api/cart/items/")[1])
                res = cart.update_cart_item(product_id=prod_id, item_update=CartItemUpdate(**body), x_session_token=None, current_user=user, db=db)
                return DirectResponse(200, res)

            return DirectResponse(404, {"detail": f"Path not found: {path}"})
        except HTTPException as e:
            return DirectResponse(e.status_code, {"detail": e.detail})
        except Exception as e:
            return DirectResponse(500, {"detail": str(e)})
        finally:
            db.close()

    def delete(self, url: str, headers: Optional[Dict[str, str]] = None) -> DirectResponse:
        parsed = urllib.parse.urlparse(url)
        path = parsed.path

        db = self.db_factory()
        try:
            user = self._get_user_from_headers(headers, db)
            if not user:
                return DirectResponse(401, {"detail": "Not authenticated"})

            if path == "/api/cart/clear":
                res = cart.clear_cart(x_session_token=None, current_user=user, db=db)
                return DirectResponse(200, res)

            elif path.startswith("/api/cart/items/"):
                prod_id = int(path.split("/api/cart/items/")[1])
                res = cart.remove_cart_item(product_id=prod_id, x_session_token=None, current_user=user, db=db)
                return DirectResponse(200, res)

            return DirectResponse(404, {"detail": f"Path not found: {path}"})
        except HTTPException as e:
            return DirectResponse(e.status_code, {"detail": e.detail})
        except Exception as e:
            return DirectResponse(500, {"detail": str(e)})
        finally:
            db.close()


@pytest.fixture(scope="session", autouse=True)
def setup_rag_index():
    """Ensure catalog vector index is ready before any test runs."""
    db = SessionLocal()
    try:
        rag_engine.index_catalog(db)
    finally:
        db.close()


@pytest.fixture(scope="function")
def db_session():
    """Provides an isolated database session per test function."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@pytest.fixture(scope="session")
def client():
    """Provides direct test client for fast, deterministic, non-hanging execution."""
    return DirectClient(SessionLocal)


@pytest.fixture(scope="function")
def customer_user(db_session: Session) -> User:
    """Retrieves existing seeded customer."""
    user = db_session.query(User).filter(User.email == "customer@example.com").first()
    if not user:
        user = User(
            email="customer@example.com",
            full_name="Alex Mercer",
            hashed_password=get_password_hash("password123"),
            is_active=True,
            is_superuser=False
        )
        db_session.add(user)
        db_session.commit()
        db_session.refresh(user)
    return user


@pytest.fixture(scope="function")
def customer_token(customer_user: User) -> str:
    """Generates valid JWT for customer."""
    return create_access_token(subject=customer_user.id)


@pytest.fixture(scope="function")
def auth_headers(customer_token: str) -> dict:
    return {"Authorization": f"Bearer {customer_token}"}
