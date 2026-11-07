import re
from typing import Dict, Any, List, Optional
from sqlalchemy.orm import Session
from app.rag.engine import rag_engine
from app.services.product_service import product_service


class ProductDiscoveryAgent:
    """
    Product Discovery Agent:
    - Parses natural-language shopping queries (e.g. 'Find the best wireless headphones under $100')
    - Applies semantic hybrid search and structured SQL filters
    - Retrieves product details, specs, and availability
    """

    def __init__(self):
        self.name = "Product Discovery Agent"

    def execute(self, db: Session, query: str, context: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        context = context or {}

        # 1. Parse natural language constraints
        min_price = None
        max_price = None
        category = context.get("category")
        brand = context.get("brand")

        # Price parsing
        under_match = re.search(r"(?:under|below|less than|within|cheaper than|\<)\s*\$?(\d+(?:\.\d+)?)", query, re.IGNORECASE)
        if under_match:
            try:
                max_price = float(under_match.group(1))
            except ValueError:
                pass

        over_match = re.search(r"(?:above|over|more than|at least|\>)\s*\$?(\d+(?:\.\d+)?)", query, re.IGNORECASE)
        if over_match:
            try:
                min_price = float(over_match.group(1))
            except ValueError:
                pass

        # Category extraction heuristics if not specified
        q_low = query.lower()
        if not category:
            if any(w in q_low for w in ["headphone", "earbud", "laptop", "monitor", "keyboard", "mouse", "phone", "audio", "charger", "cable", "tech"]):
                category = "Electronics"
            elif any(w in q_low for w in ["shirt", "jacket", "pants", "shoe", "hoodie", "dress", "jeans", "apparel", "wear", "cotton"]):
                category = "Clothing & Apparel"
            elif any(w in q_low for w in ["coffee", "blender", "fryer", "pan", "pot", "cookware", "kettle", "chair", "desk", "kitchen", "home"]):
                category = "Home & Kitchen"
            elif any(w in q_low for w in ["watch", "smartwatch", "backpack", "wallet", "sunglass", "bag", "belt", "fitness", "yoga"]):
                category = "Accessories & Wearables"

        # 2. Run Hybrid Search via RAG Engine
        raw_results = rag_engine.hybrid_search(
            db=db,
            query=query,
            category=category,
            brand=brand,
            min_price=min_price,
            max_price=max_price,
            in_stock_only=False,
            top_k=5
        )

        matched_products = []
        for prod, sim, score, expl in raw_results:
            matched_products.append({
                "id": prod["id"],
                "name": prod["name"],
                "sku": prod["sku"],
                "brand": prod["brand"],
                "category_name": prod["category_name"],
                "price": prod["price"],
                "rating": prod["rating"],
                "review_count": prod["review_count"],
                "inventory_count": prod["inventory_count"],
                "is_available": prod["is_available"],
                "description": prod["description"],
                "specifications": prod.get("specifications"),
                "features": prod.get("features"),
                "image_url": prod.get("image_url"),
                "relevance_explanation": expl
            })

        # 3. Construct Agent response message
        if matched_products:
            top_p = matched_products[0]
            count = len(matched_products)
            constraints_desc = []
            if category:
                constraints_desc.append(f"in {category}")
            if max_price:
                constraints_desc.append(f"under ${max_price:.2f}")

            filter_str = f" ({', '.join(constraints_desc)})" if constraints_desc else ""
            thought = f"Identified intent as product discovery{filter_str}. Retrieved {count} top-matching items using hybrid RAG search."

            resp_lines = [
                f"I found {count} great option{'s' if count > 1 else ''} matching your search '{query}'{filter_str}:"
            ]
            for i, p in enumerate(matched_products, 1):
                resp_lines.append(
                    f"{i}. **{p['name']}** by {p['brand']} – **${p['price']:.2f}** "
                    f"(⭐ {p['rating']}/5.0 • {p['inventory_count']} in stock)"
                )

            resp_lines.append(
                f"\nWould you like me to compare specifications, add **{top_p['name']}** to your cart, or filter by a different brand or budget?"
            )
            response_text = "\n".join(resp_lines)
        else:
            thought = f"No direct catalog items matched '{query}' with current constraints."
            response_text = (
                f"I searched our catalog for '{query}', but couldn't find an exact match under those criteria. "
                f"Try relaxing the price limit or browsing categories like Electronics, Clothing, or Home & Kitchen!"
            )

        return {
            "agent_name": self.name,
            "thought": thought,
            "response": response_text,
            "products": matched_products,
            "suggested_actions": [
                f"Compare top {min(len(matched_products), 3)} products" if len(matched_products) >= 2 else "Show more items",
                f"Add {matched_products[0]['name'][:25]}... to cart" if matched_products else "View categories",
                "Filter under $50"
            ]
        }


discovery_agent = ProductDiscoveryAgent()
