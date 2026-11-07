import re
import json
from typing import Dict, Any, List, Optional
from sqlalchemy.orm import Session
from app.rag.engine import rag_engine
from app.services.product_service import product_service
from app.models.user import User


class RecommendationAgent:
    """
    Recommendation Agent:
    - Generates personalized recommendations based on customer preferences and context
    - Compares products side-by-side by specifications, ratings, and pricing
    - Grounds all reasoning in verified RAG catalog facts
    - Delivers transparent, structured explanations with pros and cons
    """

    def __init__(self):
        self.name = "Recommendation Agent"

    def execute(
        self,
        db: Session,
        query: str,
        user: Optional[User] = None,
        context: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        context = context or {}
        candidate_ids = context.get("candidate_ids", [])

        # Check if the query specifically requests a comparison or product IDs
        id_matches = re.findall(r"\b(?:product|id|#)?\s*(\d{1,6})\b", query, re.IGNORECASE)
        explicit_ids = [int(i) for i in id_matches if 0 < int(i) < 100000]

        target_ids = explicit_ids if len(explicit_ids) >= 2 else candidate_ids

        # If we have at least 2 candidate products, do side-by-side comparison
        if len(target_ids) >= 2:
            comparison = product_service.compare_products(db, target_ids[:4])
            thought = f"Conducted comparative multi-attribute analysis across {len(comparison.products)} products."

            resp_lines = [
                f"### Product Comparison Analysis\n",
                f"{comparison.recommendation_summary}\n",
                "| Feature | " + " | ".join([f"**{p.name[:20]}...**" for p in comparison.products]) + " |",
                "| --- | " + " | ".join(["---" for _ in comparison.products]) + " |",
            ]

            for key, values in list(comparison.comparison_matrix.items())[:6]:
                row = f"| **{key}** | " + " | ".join([str(values.get(str(p.id), "N/A")) for p in comparison.products]) + " |"
                resp_lines.append(row)

            resp_lines.append(
                f"\nWould you like me to add the recommended choice (**ID: {comparison.winner_product_id}**) to your cart?"
            )

            products_payload = [p.dict() for p in comparison.products]

            return {
                "agent_name": self.name,
                "thought": thought,
                "response": "\n".join(resp_lines),
                "products": products_payload,
                "winner_id": comparison.winner_product_id,
                "suggested_actions": [
                    f"Add recommended product #{comparison.winner_product_id} to cart",
                    "Ask follow-up question on battery life / warranty",
                    "Show cheaper alternatives"
                ]
            }

        # Otherwise, generate personalized recommendations using RAG
        # Check user preferences
        preferred_cat = None
        if user and user.preference and user.preference.preferred_categories:
            try:
                cats = json.loads(user.preference.preferred_categories)
                if cats:
                    preferred_cat = cats[0]
            except Exception:
                pass

        search_results = rag_engine.hybrid_search(
            db=db,
            query=query,
            category=preferred_cat,
            top_k=4
        )

        matched_products = []
        for prod, sim, score, expl in search_results:
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

        grounded_context = rag_engine.build_grounded_context(search_results)
        thought = f"Evaluated catalog against preference patterns and query context. Retrieved {len(matched_products)} verified items."

        resp_lines = [
            "Here are my tailored recommendations based on factual specifications, verified reviews, and value rating:\n"
        ]

        for i, p in enumerate(matched_products, 1):
            specs_brief = ""
            if p.get("specifications"):
                specs_brief = " • ".join([f"{k}: {v}" for k, v in list(p["specifications"].items())[:2]])

            resp_lines.append(
                f"**{i}. {p['name']}** (${p['price']:.2f})\n"
                f"- **Why it's recommended:** {p['relevance_explanation']}\n"
                f"- **Key Specs:** {specs_brief or 'Top-tier build'}\n"
                f"- **Stock Status:** {p['inventory_count']} available\n"
            )

        resp_lines.append(
            "Which of these fits your requirements best? I can also run a direct head-to-head comparison!"
        )

        return {
            "agent_name": self.name,
            "thought": thought,
            "response": "\n".join(resp_lines),
            "products": matched_products,
            "grounded_context": grounded_context,
            "suggested_actions": [
                f"Compare #1 and #2",
                f"Add #{matched_products[0]['id']} to cart" if matched_products else "Search other items",
                "Ask about battery/durability"
            ]
        }


recommendation_agent = RecommendationAgent()
