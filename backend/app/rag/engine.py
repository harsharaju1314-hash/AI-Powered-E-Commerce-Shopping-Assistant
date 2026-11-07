import json
import re
import math
import logging
from typing import List, Dict, Any, Optional, Tuple
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sqlalchemy.orm import Session
from app.models.product import Product
from app.schemas.rag import RAGSearchResult, RAGSearchResponse, RAGEvaluationMetric
from app.schemas.product import ProductOut
from app.rag.eval_dataset import LABELED_EVALUATION_QUERIES

logger = logging.getLogger(__name__)


class RAGEngine:
    """
    RAG Search and Recommendation Engine.
    Implements:
    - Semantic similarity vector search
    - Hybrid retrieval combining vector similarity + SQL constraints + product ratings
    - Grounded product context generation for LLM agents
    - Precision@5 retrieval evaluation
    """

    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            ngram_range=(1, 1),
            max_features=10000,
            sublinear_tf=True,
            stop_words="english"
        )
        self.product_ids: List[int] = []
        self.product_cache: Dict[int, Dict[str, Any]] = {}
        self.tfidf_matrix = None
        self.is_indexed = False

    def build_product_corpus_text(self, product: Product) -> str:
        """Create rich searchable text representing a product."""
        specs_str = ""
        if product.specifications:
            try:
                specs_dict = json.loads(product.specifications) if isinstance(product.specifications, str) else product.specifications
                specs_str = " ".join([f"{k} {v}" for k, v in specs_dict.items()])
            except Exception:
                specs_str = str(product.specifications)

        features_str = ""
        if product.features:
            try:
                features_list = json.loads(product.features) if isinstance(product.features, str) else product.features
                features_str = " ".join(features_list)
            except Exception:
                features_str = str(product.features)

        tags_str = ""
        if product.tags:
            try:
                tags_list = json.loads(product.tags) if isinstance(product.tags, str) else product.tags
                tags_str = " ".join(tags_list)
            except Exception:
                tags_str = str(product.tags)

        return (
            f"{product.name} {product.category_name} {product.brand} "
            f"{product.description} {specs_str} {features_str} {tags_str}"
        )

    def index_catalog(self, db: Session, force_reload: bool = False):
        """Build vector embeddings index from database products."""
        if self.is_indexed and not force_reload:
            return

        products = db.query(Product).all()
        if not products:
            logger.warning("No products found to index in RAGEngine.")
            return

        corpus = []
        self.product_ids = []
        self.product_cache = {}

        for p in products:
            text = self.build_product_corpus_text(p)
            corpus.append(text)
            self.product_ids.append(p.id)

            # Parse JSON fields safely for fast retrieval
            specs = None
            if p.specifications:
                try:
                    specs = json.loads(p.specifications) if isinstance(p.specifications, str) else p.specifications
                except Exception:
                    specs = {}

            feats = None
            if p.features:
                try:
                    feats = json.loads(p.features) if isinstance(p.features, str) else p.features
                except Exception:
                    feats = []

            tags = None
            if p.tags:
                try:
                    tags = json.loads(p.tags) if isinstance(p.tags, str) else p.tags
                except Exception:
                    tags = []

            self.product_cache[p.id] = {
                "id": p.id,
                "name": p.name,
                "sku": p.sku,
                "slug": p.slug,
                "category_id": p.category_id,
                "category_name": p.category_name,
                "brand": p.brand,
                "price": p.price,
                "original_price": p.original_price,
                "rating": p.rating,
                "review_count": p.review_count,
                "inventory_count": p.inventory_count,
                "is_available": p.is_available,
                "description": p.description,
                "specifications": specs,
                "features": feats,
                "tags": tags,
                "image_url": p.image_url,
                "created_at": p.created_at,
                "updated_at": p.updated_at,
            }

        self.tfidf_matrix = self.vectorizer.fit_transform(corpus)
        self.is_indexed = True
        logger.info(f"RAGEngine successfully indexed {len(products)} products into vector space.")

    def hybrid_search(
        self,
        db: Session,
        query: str,
        category: Optional[str] = None,
        brand: Optional[str] = None,
        min_price: Optional[float] = None,
        max_price: Optional[float] = None,
        in_stock_only: bool = False,
        top_k: int = 5
    ) -> List[Tuple[Dict[str, Any], float, float, str]]:
        """
        Hybrid retrieval combining vector semantic similarity, SQL filtering, and rating quality boost.
        Returns: List of tuples (product_dict, similarity_score, hybrid_score, explanation)
        """
        if not self.is_indexed or not self.product_ids:
            self.index_catalog(db)

        if not self.is_indexed or not self.product_ids:
            return []

        # Vector semantic scoring
        query_vec = self.vectorizer.transform([query])
        sim_scores = (self.tfidf_matrix * query_vec.T).toarray().flatten()

        # Parse query for natural price indications (e.g. "under $100", "below 500")
        extracted_max_price = max_price
        price_match = re.search(r"(?:under|below|less than|\<)\s*\$?(\d+(?:\.\d+)?)", query, re.IGNORECASE)
        if price_match and extracted_max_price is None:
            try:
                extracted_max_price = float(price_match.group(1))
            except ValueError:
                pass

        query_lower = query.lower()
        query_tokens = [w for w in re.findall(r"\w+", query_lower) if len(w) > 2]

        results = []
        for idx, pid in enumerate(self.product_ids):
            p = self.product_cache.get(pid)
            if not p:
                continue

            # Hard filtering constraints (Hybrid SQL logic)
            if category and p["category_name"].lower() != category.lower():
                continue
            if brand and p["brand"].lower() != brand.lower():
                continue
            if min_price is not None and p["price"] < min_price:
                continue
            if extracted_max_price is not None and p["price"] > extracted_max_price:
                continue
            if in_stock_only and (not p["is_available"] or p["inventory_count"] <= 0):
                continue

            base_sim = float(sim_scores[idx])

            # Exact keyword match bonus
            kw_matches = 0
            p_text = f"{p['name']} {p['category_name']} {p['brand']} {p['description']}".lower()
            for tok in query_tokens:
                if tok in p_text:
                    kw_matches += 1
            kw_ratio = kw_matches / max(len(query_tokens), 1)

            # Rating quality boost (normalized 0 to 1)
            rating_boost = (p.get("rating", 4.0) or 4.0) / 5.0

            # Stock availability factor
            stock_factor = 1.0 if p["inventory_count"] > 0 else 0.4

            # Hybrid score computation:
            # 50% semantic vector + 30% keyword match + 15% rating + 5% in-stock
            hybrid_score = (
                (0.50 * base_sim) +
                (0.30 * kw_ratio) +
                (0.15 * rating_boost) +
                (0.05 * (1.0 if p["inventory_count"] > 0 else 0.0))
            ) * stock_factor

            explanation = (
                f"Matched with {base_sim:.2f} semantic relevance; "
                f"{kw_matches} query keywords aligned; "
                f"Rated {p.get('rating', 0.0)}/5.0 with {p.get('inventory_count', 0)} in stock."
            )

            results.append((p, base_sim, hybrid_score, explanation))

        # Sort by hybrid score descending
        results.sort(key=lambda x: x[2], reverse=True)
        return results[:top_k]

    def build_grounded_context(self, search_results: List[Tuple[Dict[str, Any], float, float, str]]) -> str:
        """Construct grounded, factual Markdown context for LLM prompt generation."""
        if not search_results:
            return "No verified products match the criteria in our current catalog."

        context_lines = ["### Verified Product Catalog Information:"]
        for i, (prod, sim, score, expl) in enumerate(search_results, 1):
            specs_summary = ""
            if prod.get("specifications"):
                specs_summary = ", ".join([f"{k}: {v}" for k, v in list(prod["specifications"].items())[:4]])

            context_lines.append(
                f"{i}. **{prod['name']}** (SKU: {prod['sku']}, ID: {prod['id']})\n"
                f"   - Brand: {prod['brand']} | Category: {prod['category_name']}\n"
                f"   - Price: ${prod['price']:.2f} (Inventory: {prod['inventory_count']} units available)\n"
                f"   - Customer Rating: {prod['rating']} / 5.0 ({prod['review_count']} verified reviews)\n"
                f"   - Key Specs: {specs_summary or 'Standard specifications'}\n"
                f"   - Summary: {prod['description']}\n"
            )
        return "\n".join(context_lines)

    def evaluate_retrieval_precision(self, db: Session, top_k: int = 5) -> RAGEvaluationMetric:
        """
        Evaluate Precision@5 across the labeled evaluation queries.
        Target: >= 90% Precision@5.
        """
        if not self.is_indexed:
            self.index_catalog(db)

        total_queries = len(LABELED_EVALUATION_QUERIES)
        p5_scores = []
        p3_scores = []
        p1_scores = []
        rr_scores = []
        details = []

        for item in LABELED_EVALUATION_QUERIES:
            q_id = item["query_id"]
            q_text = item["query_text"]
            tgt_cat = item.get("target_category")
            max_p = item.get("max_price")
            must_keywords = [k.lower() for k in item.get("must_have_keywords", [])]

            results = self.hybrid_search(
                db=db,
                query=q_text,
                category=tgt_cat,
                max_price=max_p,
                top_k=top_k
            )

            relevant_count_5 = 0
            relevant_count_3 = 0
            first_relevant_rank = None

            for rank, (prod, sim, score, _) in enumerate(results, 1):
                prod_text = f"{prod['name']} {prod['category_name']} {prod['brand']} {prod['description']}".lower()

                # A retrieved product is relevant if it matches the target category (if any),
                # satisfies price constraints, and matches at least one core domain keyword
                is_cat_match = (not tgt_cat) or (prod["category_name"].lower() == tgt_cat.lower())
                is_price_match = (not max_p) or (prod["price"] <= max_p * 1.05)  # 5% tolerance
                is_kw_match = any(kw in prod_text for kw in must_keywords)

                if is_cat_match and is_price_match and is_kw_match:
                    if rank <= 5:
                        relevant_count_5 += 1
                    if rank <= 3:
                        relevant_count_3 += 1
                    if first_relevant_rank is None:
                        first_relevant_rank = rank

            p5 = relevant_count_5 / float(min(top_k, max(len(results), 1)))
            p3 = relevant_count_3 / 3.0
            p1 = 1.0 if (first_relevant_rank == 1) else 0.0
            rr = 1.0 / first_relevant_rank if first_relevant_rank else 0.0

            p5_scores.append(p5)
            p3_scores.append(p3)
            p1_scores.append(p1)
            rr_scores.append(rr)

            details.append({
                "query_id": q_id,
                "query": q_text,
                "precision_at_5": round(p5, 3),
                "retrieved_count": len(results),
                "relevant_retrieved": relevant_count_5
            })

        avg_p5 = float(np.mean(p5_scores)) if p5_scores else 0.0
        avg_p3 = float(np.mean(p3_scores)) if p3_scores else 0.0
        avg_p1 = float(np.mean(p1_scores)) if p1_scores else 0.0
        mrr = float(np.mean(rr_scores)) if rr_scores else 0.0

        return RAGEvaluationMetric(
            precision_at_5=round(avg_p5 * 100, 2),
            precision_at_3=round(avg_p3 * 100, 2),
            precision_at_1=round(avg_p1 * 100, 2),
            mean_reciprocal_rank=round(mrr, 3),
            total_queries_evaluated=total_queries,
            successful_queries=sum(1 for s in p5_scores if s >= 0.8),
            benchmark_passed=bool(avg_p5 >= 0.90),
            details=details
        )


rag_engine = RAGEngine()
