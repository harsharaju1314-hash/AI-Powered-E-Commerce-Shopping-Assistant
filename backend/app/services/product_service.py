import json
from typing import List, Optional, Tuple, Dict, Any
from sqlalchemy.orm import Session
from sqlalchemy import or_, desc, asc
from app.models.product import Product, ProductCategory
from app.schemas.product import ProductFilterParams, ProductCompareResponse, ProductOut
from app.rag.engine import rag_engine


class ProductService:
    @staticmethod
    def get_by_id(db: Session, product_id: int) -> Optional[Product]:
        return db.query(Product).filter(Product.id == product_id).first()

    @staticmethod
    def get_by_slug(db: Session, slug: str) -> Optional[Product]:
        return db.query(Product).filter(Product.slug == slug).first()

    @staticmethod
    def list_categories(db: Session) -> List[ProductCategory]:
        return db.query(ProductCategory).order_by(ProductCategory.name.asc()).all()

    @staticmethod
    def list_brands(db: Session) -> List[str]:
        results = db.query(Product.brand).distinct().order_by(Product.brand.asc()).all()
        return [r[0] for r in results if r[0]]

    @staticmethod
    def filter_products(db: Session, params: ProductFilterParams) -> Tuple[List[Product], int]:
        query = db.query(Product)

        if params.category:
            query = query.filter(Product.category_name.ilike(f"%{params.category}%"))

        if params.brand:
            query = query.filter(Product.brand.ilike(f"%{params.brand}%"))

        if params.min_price is not None:
            query = query.filter(Product.price >= params.min_price)

        if params.max_price is not None:
            query = query.filter(Product.price <= params.max_price)

        if params.min_rating is not None:
            query = query.filter(Product.rating >= params.min_rating)

        if params.in_stock_only:
            query = query.filter(Product.inventory_count > 0, Product.is_available.is_(True))

        if params.query:
            search_str = f"%{params.query}%"
            query = query.filter(
                or_(
                    Product.name.ilike(search_str),
                    Product.brand.ilike(search_str),
                    Product.description.ilike(search_str),
                    Product.category_name.ilike(search_str)
                )
            )

        # Sorting
        if params.sort_by == "price_asc":
            query = query.order_by(asc(Product.price))
        elif params.sort_by == "price_desc":
            query = query.order_by(desc(Product.price))
        elif params.sort_by == "rating":
            query = query.order_by(desc(Product.rating), desc(Product.review_count))
        elif params.sort_by == "newest":
            query = query.order_by(desc(Product.created_at))
        else:
            # Default sorting
            query = query.order_by(desc(Product.rating), desc(Product.review_count))

        total = query.count()
        offset = (params.page - 1) * params.page_size
        items = query.offset(offset).limit(params.page_size).all()
        return items, total

    @staticmethod
    def compare_products(db: Session, product_ids: List[int]) -> ProductCompareResponse:
        products = db.query(Product).filter(Product.id.in_(product_ids)).all()
        if not products:
            return ProductCompareResponse(
                products=[],
                comparison_matrix={},
                recommendation_summary="No products found to compare.",
                winner_product_id=None
            )

        # Build comparison matrix of specs
        matrix: Dict[str, Dict[str, Any]] = {
            "Price ($)": {},
            "Rating": {},
            "Brand": {},
            "Category": {},
            "Inventory Status": {},
        }

        # Gather custom specs keys
        all_spec_keys = set()
        product_parsed_specs: Dict[int, Dict[str, Any]] = {}
        for p in products:
            p_specs = {}
            if p.specifications:
                try:
                    p_specs = json.loads(p.specifications) if isinstance(p.specifications, str) else p.specifications
                except Exception:
                    p_specs = {}
            product_parsed_specs[p.id] = p_specs
            for k in p_specs.keys():
                all_spec_keys.add(k)

        for k in sorted(list(all_spec_keys))[:10]:
            matrix[k] = {}

        highest_score = -1.0
        winner_id = None

        product_outs = []
        for p in products:
            matrix["Price ($)"][str(p.id)] = f"${p.price:.2f}"
            matrix["Rating"][str(p.id)] = f"{p.rating} / 5.0 ({p.review_count} reviews)"
            matrix["Brand"][str(p.id)] = p.brand
            matrix["Category"][str(p.id)] = p.category_name
            matrix["Inventory Status"][str(p.id)] = f"{p.inventory_count} in stock" if p.inventory_count > 0 else "Out of stock"

            p_specs = product_parsed_specs.get(p.id, {})
            for k in all_spec_keys:
                if k in matrix:
                    matrix[k][str(p.id)] = p_specs.get(k, "N/A")

            # Value score: rating / price ratio with inventory consideration
            score = (p.rating * 100.0) / max(p.price, 10.0) + (10.0 if p.inventory_count > 0 else 0)
            if score > highest_score:
                highest_score = score
                winner_id = p.id

            # Parse for Pydantic
            p_out = ProductOut(
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
                specifications=p_specs,
                features=json.loads(p.features) if p.features and isinstance(p.features, str) else p.features,
                tags=json.loads(p.tags) if p.tags and isinstance(p.tags, str) else p.tags,
                image_url=p.image_url,
                created_at=p.created_at,
                updated_at=p.updated_at
            )
            product_outs.append(p_out)

        winner_prod = next((p for p in products if p.id == winner_id), products[0])
        summary = (
            f"Based on technical specifications, customer satisfaction ({winner_prod.rating}/5.0), "
            f"and price-to-performance value, the recommended choice is **{winner_prod.name}** (${winner_prod.price:.2f})."
        )

        return ProductCompareResponse(
            products=product_outs,
            comparison_matrix=matrix,
            recommendation_summary=summary,
            winner_product_id=winner_id
        )


product_service = ProductService()
