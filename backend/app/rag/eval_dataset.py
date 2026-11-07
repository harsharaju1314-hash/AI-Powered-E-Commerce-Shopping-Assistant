"""
Labeled evaluation dataset for RAG retrieval quality.
Used to evaluate Precision@5, Precision@3, and Mean Reciprocal Rank (MRR).
Target: >= 90% Precision@5 relevance.
"""

LABELED_EVALUATION_QUERIES = [
    {
        "query_id": "eval_01",
        "query_text": "Find the best wireless headphones under $100",
        "target_category": "Electronics",
        "target_brand": None,
        "max_price": 100.0,
        "must_have_keywords": ["headphones", "wireless", "bluetooth", "audio"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_02",
        "query_text": "High performance gaming laptop 16GB RAM under $1200",
        "target_category": "Electronics",
        "target_brand": None,
        "max_price": 1200.0,
        "must_have_keywords": ["laptop", "gaming", "ram", "performance"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_03",
        "query_text": "Men lightweight running shoes breathable",
        "target_category": "Clothing & Apparel",
        "target_brand": None,
        "max_price": 150.0,
        "must_have_keywords": ["running", "shoes", "breathable", "lightweight"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_04",
        "query_text": "Stainless steel programmable coffee maker kitchen",
        "target_category": "Home & Kitchen",
        "target_brand": None,
        "max_price": 150.0,
        "must_have_keywords": ["coffee", "maker", "programmable", "stainless"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_05",
        "query_text": "Smart watch with heart rate monitor waterproof fitness",
        "target_category": "Accessories & Wearables",
        "target_brand": None,
        "max_price": 250.0,
        "must_have_keywords": ["smartwatch", "watch", "heart", "fitness"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_06",
        "query_text": "Mechanical gaming keyboard RGB backlit switches",
        "target_category": "Electronics",
        "target_brand": None,
        "max_price": 120.0,
        "must_have_keywords": ["keyboard", "mechanical", "rgb", "gaming"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_07",
        "query_text": "Noise cancelling earbuds with charging case",
        "target_category": "Electronics",
        "target_brand": None,
        "max_price": 80.0,
        "must_have_keywords": ["earbuds", "noise", "cancelling", "charging"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_08",
        "query_text": "Non-stick ceramic cookware pot and pan set induction",
        "target_category": "Home & Kitchen",
        "target_brand": None,
        "max_price": 200.0,
        "must_have_keywords": ["cookware", "pan", "pot", "ceramic", "kitchen"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_09",
        "query_text": "Waterproof hiking backpack 40L outdoor travel",
        "target_category": "Accessories & Wearables",
        "target_brand": None,
        "max_price": 90.0,
        "must_have_keywords": ["backpack", "hiking", "waterproof", "outdoor"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_10",
        "query_text": "Women organic cotton casual crewneck t-shirt",
        "target_category": "Clothing & Apparel",
        "target_brand": None,
        "max_price": 40.0,
        "must_have_keywords": ["cotton", "shirt", "casual", "crewneck"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_11",
        "query_text": "Ultra HD 4K computer monitor 27 inch IPS display",
        "target_category": "Electronics",
        "target_brand": None,
        "max_price": 350.0,
        "must_have_keywords": ["monitor", "4k", "display", "ips", "screen"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_12",
        "query_text": "Air fryer convection toaster oven multi-function",
        "target_category": "Home & Kitchen",
        "target_brand": None,
        "max_price": 180.0,
        "must_have_keywords": ["fryer", "air", "oven", "toaster", "kitchen"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_13",
        "query_text": "Polarized UV protection sunglasses lightweight frame",
        "target_category": "Accessories & Wearables",
        "target_brand": None,
        "max_price": 60.0,
        "must_have_keywords": ["sunglasses", "polarized", "uv", "eyewear"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_14",
        "query_text": "Winter warm hooded parka jacket water resistant",
        "target_category": "Clothing & Apparel",
        "target_brand": None,
        "max_price": 220.0,
        "must_have_keywords": ["jacket", "parka", "winter", "warm", "hooded"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_15",
        "query_text": "Robotic vacuum cleaner smart navigation self-charging",
        "target_category": "Home & Kitchen",
        "target_brand": None,
        "max_price": 300.0,
        "must_have_keywords": ["robot", "vacuum", "cleaner", "smart"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_16",
        "query_text": "Ergonomic mesh office chair lumbar support adjustable",
        "target_category": "Home & Kitchen",
        "target_brand": None,
        "max_price": 250.0,
        "must_have_keywords": ["chair", "office", "ergonomic", "mesh", "lumbar"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_17",
        "query_text": "Leather minimalist slim wallet RFID blocking",
        "target_category": "Accessories & Wearables",
        "target_brand": None,
        "max_price": 50.0,
        "must_have_keywords": ["wallet", "leather", "rfid", "slim"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_18",
        "query_text": "Wireless ergonomic mouse rechargeable silent click",
        "target_category": "Electronics",
        "target_brand": None,
        "max_price": 45.0,
        "must_have_keywords": ["mouse", "wireless", "ergonomic", "silent"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_19",
        "query_text": "Yoga mat extra thick non-slip eco-friendly exercise",
        "target_category": "Accessories & Wearables",
        "target_brand": None,
        "max_price": 40.0,
        "must_have_keywords": ["yoga", "mat", "exercise", "non-slip"],
        "expected_min_rating": 4.0
    },
    {
        "query_id": "eval_20",
        "query_text": "Electric kettle temperature control fast boiling cordless",
        "target_category": "Home & Kitchen",
        "target_brand": None,
        "max_price": 75.0,
        "must_have_keywords": ["kettle", "electric", "temperature", "boiling"],
        "expected_min_rating": 4.0
    }
]
