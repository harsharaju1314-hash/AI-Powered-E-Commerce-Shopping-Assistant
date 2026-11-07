import random
import json
import logging
from sqlalchemy.orm import Session
from app.database.session import engine, SessionLocal, Base
from app.models.user import User
from app.models.product import Product, ProductCategory
from app.models.preference import CustomerPreference
from app.auth.security import get_password_hash
from app.rag.engine import rag_engine

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("seed_data")

random.seed(42)  # Deterministic seed for reproducible testing and evaluation

CATEGORIES_DATA = [
    {
        "name": "Electronics",
        "slug": "electronics",
        "description": "High-performance computing, audio gear, peripherals, and consumer gadgets.",
        "icon": "cpu",
        "brands": ["Sony", "Bose", "Apple", "Dell", "Logitech", "Samsung", "Anker", "Asus", "Sennheiser", "Audio-Technica"],
        "types": [
            ("Wireless Headphones", "Over-ear noise-cancelling wireless headphones with high-fidelity acoustic drivers.", 79.99, 349.99, {"Bluetooth": "5.3", "Battery Life": "30 hours", "Noise Cancellation": "Active ANC", "Weight": "250g"}),
            ("Gaming Laptop", "High-performance laptop engineered for programming, gaming, and creative workloads.", 749.99, 1499.99, {"RAM": "16GB DDR5", "Processor": "Intel Core i7 / Ryzen 7", "Storage": "1TB NVMe SSD", "Display": "15.6 inch 144Hz"}),
            ("Noise-Cancelling Earbuds", "Compact true wireless earbuds with immersive transparency mode and deep bass.", 39.99, 199.99, {"Water Resistance": "IPX5", "Battery Life": "28 hours with case", "Fast Charging": "Yes", "Microphone": "Quad-mic array"}),
            ("Mechanical Gaming Keyboard", "Tactile mechanical keyboard with customizable RGB backlighting and durable switches.", 49.99, 149.99, {"Switch Type": "Cherry MX Red / Blue", "Layout": "Tenkeyless / Full", "Connectivity": "USB-C & 2.4GHz", "Keycaps": "PBT Doubleshot"}),
            ("Ergonomic Wireless Mouse", "Precision optical sensor mouse with customizable shortcut buttons and silent clicks.", 24.99, 89.99, {"DPI": "4000 DPI Optical", "Battery": "Rechargeable Li-Ion", "Connectivity": "Bluetooth + 2.4GHz", "Buttons": "6 Programmable"}),
            ("4K Ultra HD Monitor", "Crisp IPS computer monitor designed for productivity, software development, and graphic design.", 199.99, 499.99, {"Resolution": "3840x2160 (4K)", "Refresh Rate": "60Hz / 120Hz", "Panel Type": "IPS Anti-Glare", "Ports": "HDMI 2.1, DisplayPort, USB-C 65W PD"}),
            ("Fast Portable Power Bank", "High capacity portable external battery pack with Power Delivery 65W fast charging.", 29.99, 79.99, {"Capacity": "20000mAh", "Output": "65W USB-C PD", "Ports": "2x USB-C, 1x USB-A", "Weight": "380g"})
        ]
    },
    {
        "name": "Clothing & Apparel",
        "slug": "clothing-apparel",
        "description": "Premium casual wear, performance activewear, footwear, and weather-resistant outerwear.",
        "icon": "shopping-bag",
        "brands": ["Nike", "Adidas", "Patagonia", "Under Armour", "Levi's", "Lululemon", "The North Face", "Champion", "Puma", "Columbia"],
        "types": [
            ("Breathable Running Shoes", "Lightweight athletic running sneakers engineered with responsive energy-return foam cushioning.", 65.00, 160.00, {"Material": "Engineered Mesh", "Sole": "Durable Carbon Rubber", "Arch Support": "Neutral", "Drop": "10mm"}),
            ("Organic Cotton Crewneck T-Shirt", "Everyday premium soft combed cotton t-shirt with reinforced double-stitched hem.", 18.00, 38.00, {"Material": "100% Organic Ring-Spun Cotton", "Fit": "Regular Modern Fit", "Care": "Machine wash cold", "Pre-shrunk": "Yes"}),
            ("Winter Warm Hooded Parka Jacket", "Insulated weather-resistant winter jacket featuring thermal down-alternative fill.", 99.00, 249.00, {"Waterproof Rating": "10000mm", "Insulation": "Eco-Down Fill", "Pockets": "6 Fleece-lined", "Hood": "Detachable Faux Fur"}),
            ("Moisture-Wicking Athletic Hoodie", "Performance pullover workout sweatshirt with four-way stretch fabric.", 35.00, 75.00, {"Fabric": "Polyester Spandex Blend", "Moisture Wicking": "Advanced Dry-Fit", "Pockets": "Front Kangaroo", "Thumbholes": "Yes"}),
            ("Stretch Slim Fit Denim Jeans", "Classic five-pocket blue jeans crafted with durable denim and flexible elastane.", 45.00, 95.00, {"Denim Weight": "12oz Stretch Denim", "Closure": "Button fly with zipper", "Wash": "Vintage Indigo / Deep Black", "Stretch": "2% Elastane"})
        ]
    },
    {
        "name": "Home & Kitchen",
        "slug": "home-kitchen",
        "description": "Smart appliances, culinary cookware, ergonomic furniture, and modern home essentials.",
        "icon": "home",
        "brands": ["Instant Pot", "KitchenAid", "Ninja", "Cuisinart", "Breville", "Herman Miller", "Dyson", "Philips", "De'Longhi", "Lodge"],
        "types": [
            ("Programmable Stainless Steel Coffee Maker", "Multi-cup thermal carafe drip coffee brewer with 24-hour advance timer.", 49.99, 149.99, {"Capacity": "12 Cups", "Carafe": "Double-Wall Thermal Stainless", "Brew Strength": "Regular / Bold", "Auto Shut-Off": "Yes"}),
            ("Digital Convection Air Fryer Oven", "Versatile countertop air fryer with rapid hot air circulation and dehydrate modes.", 79.99, 189.99, {"Capacity": "6 Quart / 10L", "Presets": "8 Digital Functions", "Temperature Range": "105F - 400F", "Basket": "Dishwasher Safe Non-Stick"}),
            ("Non-Stick Ceramic Cookware Set", "Scratch-resistant ceramic coated induction-compatible frying pans and saucepans.", 89.99, 229.99, {"Pieces": "10-Piece Set", "Coating": "Toxic-Free Ceramic (PTFE/PFOA Free)", "Oven Safe": "Up to 450F", "Compatibility": "Gas, Electric, Induction"}),
            ("Ergonomic Mesh Office Chair", "Executive desk chair with dynamic lumbar spine support and 3D adjustable armrests.", 129.99, 299.99, {"Mesh": "High-Density Breathable Mesh", "Recline Angle": "90 to 135 degrees", "Cylinder": "Class 4 Gas Lift", "Weight Capacity": "300 lbs"}),
            ("Robotic Vacuum Cleaner with Smart Mapping", "Autonomous quiet floor vacuum with smart LiDAR laser room navigation.", 149.99, 399.99, {"Suction Power": "3000Pa", "Run Time": "150 Minutes", "Filter": "True HEPA", "App Control": "Wi-Fi & Voice Assistant"})
        ]
    },
    {
        "name": "Accessories & Wearables",
        "slug": "accessories-wearables",
        "description": "Smart wearables, outdoor travel gear, leather accessories, and optical eyewear.",
        "icon": "watch",
        "brands": ["Garmin", "Fitbit", "Fossil", "Bellroy", "Ray-Ban", "Oakley", "Osprey", "Herschel", "Timberland", "Samsonite"],
        "types": [
            ("Waterproof GPS Smartwatch", "Health fitness tracking smartwatch with continuous heart rate monitor and AMOLED screen.", 89.99, 299.99, {"Battery Life": "7 Days", "Waterproof": "5 ATM (50m)", "Sensors": "Heart Rate, SpO2, Sleep, GPS", "Display": "1.4 inch AMOLED"}),
            ("Waterproof Hiking Travel Backpack", "Rugged 40L outdoor expedition rucksack with rain cover and laptop compartment.", 45.00, 110.00, {"Capacity": "40 Liters", "Material": "Ripstop Nylon 600D", "Laptop Sleeve": "Fits up to 16 inch", "Rain Cover": "Included"}),
            ("Slim Minimalist RFID Leather Wallet", "Genuine full-grain leather front pocket cardholder with RFID protection shielding.", 22.00, 55.00, {"Material": "Full-Grain Italian Leather", "Card Capacity": "Up to 8 cards + cash", "RFID Blocking": "13.56 MHz Frequency", "Profile": "Ultra Slim 0.3 inch"}),
            ("Polarized UV400 Sunglasses", "Classic lightweight sunglasses with glare-reducing polarized lenses.", 28.00, 85.00, {"Protection": "100% UV400 Protection", "Frame": "Lightweight Polycarbonate / Metal", "Lens": "Triacetate Cellulose Polarized", "Hinges": "Spring Loaded"}),
            ("Extra Thick Non-Slip Yoga Mat", "High-density cushioned workout mat designed for pilates, fitness, and yoga.", 19.99, 45.00, {"Thickness": "6mm / 0.25 inch", "Material": "Eco-Friendly TPE", "Texture": "Dual-Sided Non-Slip", "Carrying Strap": "Included"})
        ]
    }
]

MODIFIERS = ["Pro", "Ultra", "Elite", "Max", "Air", "Classic", "Plus", "Advanced", "Prime", "Signature"]
COLORS = ["Midnight Black", "Arctic White", "Space Gray", "Navy Blue", "Forest Green", "Rose Gold", "Charcoal", "Matte Silver"]


def generate_synthetic_products(target_count: int = 5200) -> list:
    """Generate 5,000+ realistic synthetic products with rich specifications."""
    products = []
    category_count = len(CATEGORIES_DATA)
    items_per_category = target_count // category_count

    sku_counter = 1000

    for cat_idx, cat in enumerate(CATEGORIES_DATA, 1):
        cat_name = cat["name"]
        cat_brands = cat["brands"]
        types = cat["types"]

        for i in range(items_per_category):
            sku_counter += 1
            brand = random.choice(cat_brands)
            ptype, base_desc, min_p, max_p, base_specs = random.choice(types)
            mod = random.choice(MODIFIERS)
            color = random.choice(COLORS)
            gen = random.choice(["Gen 2", "Gen 3", "2026 Edition", "Series 5", "V2", "Edition X", "Studio Edition"])

            name = f"{brand} {ptype} {mod} {gen}"
            price = round(random.uniform(min_p, max_p), 2)
            has_discount = random.random() < 0.45
            orig_price = round(price * random.uniform(1.15, 1.40), 2) if has_discount else None
            rating = round(random.uniform(3.9, 5.0), 1)
            review_count = random.randint(12, 1450)
            in_stock = random.random() > 0.04  # 96% in stock
            inventory = random.randint(5, 120) if in_stock else 0

            # Customized specifications
            specs = dict(base_specs)
            specs["Color"] = color
            specs["Warranty"] = random.choice(["1 Year Limited", "2 Year Manufacturer", "Lifetime Limited"])
            specs["Brand"] = brand
            specs["Model SKU"] = f"SKU-{sku_counter}"

            features = [
                f"Designed by {brand} for exceptional everyday reliability and performance.",
                f"Features {specs.get(list(specs.keys())[0], 'superior build quality')} for demanding use.",
                f"Available in stunning {color} finish.",
                f"Backed by a comprehensive {specs['Warranty']} warranty and customer support."
            ]

            tags = [
                cat_name.lower(),
                brand.lower(),
                ptype.lower(),
                color.lower(),
                "top rated" if rating >= 4.5 else "popular",
                "deal" if has_discount else "best value"
            ]

            desc = (
                f"The {name} delivers unmatched performance in {cat_name.lower()}. "
                f"{base_desc} Crafted in a sleek {color} finish by {brand}, this model combines "
                f"cutting-edge design with premium durability. Verified customer satisfaction rating of {rating}/5.0."
            )

            sku = f"{cat['slug'][:4].upper()}-{sku_counter}"
            slug = f"{name.lower().replace(' ', '-').replace('&', 'and')}-{sku_counter}"

            products.append({
                "sku": sku,
                "name": name,
                "slug": slug,
                "category_id": cat_idx,
                "category_name": cat_name,
                "brand": brand,
                "price": price,
                "original_price": orig_price,
                "rating": rating,
                "review_count": review_count,
                "inventory_count": inventory,
                "is_available": in_stock,
                "description": desc,
                "specifications": json.dumps(specs),
                "features": json.dumps(features),
                "tags": json.dumps(tags),
                "image_url": f"https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60"
            })

    return products


def seed_database():
    """Execute complete database seeding with 5,000+ products, categories, and test users."""
    logger.info("Connecting to database and creating schema...")
    Base.metadata.create_all(bind=engine)

    db: Session = SessionLocal()
    try:
        # 1. Seed or get Categories
        logger.info("Seeding product categories...")
        cat_map = {}
        for cat_info in CATEGORIES_DATA:
            cat_obj = db.query(ProductCategory).filter(ProductCategory.slug == cat_info["slug"]).first()
            if not cat_obj:
                cat_obj = ProductCategory(
                    name=cat_info["name"],
                    slug=cat_info["slug"],
                    description=cat_info["description"],
                    icon=cat_info["icon"]
                )
                db.add(cat_obj)
                db.flush()
            cat_map[cat_info["name"]] = cat_obj.id
        db.commit()

        # 2. Seed Default Test Users
        logger.info("Seeding test users...")
        test_users = [
            {
                "email": "customer@example.com",
                "full_name": "Alex Mercer",
                "password": "password123",
                "is_superuser": False,
                "pref_cat": ["Electronics", "Clothing & Apparel"],
                "budget": 500.0
            },
            {
                "email": "admin@example.com",
                "full_name": "Admin Specialist",
                "password": "adminpassword123",
                "is_superuser": True,
                "pref_cat": ["Electronics"],
                "budget": 2000.0
            }
        ]

        for u in test_users:
            existing_user = db.query(User).filter(User.email == u["email"]).first()
            if not existing_user:
                new_u = User(
                    email=u["email"],
                    full_name=u["full_name"],
                    hashed_password=get_password_hash(u["password"]),
                    is_active=True,
                    is_superuser=u["is_superuser"]
                )
                db.add(new_u)
                db.flush()

                pref = CustomerPreference(
                    user_id=new_u.id,
                    preferred_categories=json.dumps(u["pref_cat"]),
                    preferred_brands=json.dumps(["Sony", "Apple", "Nike"]),
                    max_budget=u["budget"],
                    favorite_features=json.dumps(["Wireless", "Noise Cancellation", "Lightweight"])
                )
                db.add(pref)
        db.commit()

        # 3. Check existing products count
        existing_count = db.query(Product).count()
        if existing_count >= 5000:
            logger.info(f"Database already contains {existing_count} products. Skipping product generation.")
        else:
            logger.info("Generating 5,200 synthetic product records...")
            products_data = generate_synthetic_products(target_count=5200)

            # Insert in chunks of 500
            chunk_size = 500
            for i in range(0, len(products_data), chunk_size):
                chunk = products_data[i:i + chunk_size]
                product_objs = [
                    Product(
                        sku=p["sku"],
                        name=p["name"],
                        slug=p["slug"],
                        category_id=cat_map.get(p["category_name"], 1),
                        category_name=p["category_name"],
                        brand=p["brand"],
                        price=p["price"],
                        original_price=p["original_price"],
                        rating=p["rating"],
                        review_count=p["review_count"],
                        inventory_count=p["inventory_count"],
                        is_available=p["is_available"],
                        description=p["description"],
                        specifications=p["specifications"],
                        features=p["features"],
                        tags=p["tags"],
                        image_url=p["image_url"]
                    )
                    for p in chunk
                ]
                db.bulk_save_objects(product_objs)
                db.commit()
                logger.info(f"Inserted products batch {i + len(chunk)} / {len(products_data)}")

        final_count = db.query(Product).count()
        logger.info(f"Total products in catalog: {final_count}")

        # 4. Build RAG Index
        logger.info("Indexing catalog in RAG Vector Engine...")
        rag_engine.index_catalog(db, force_reload=True)
        logger.info("RAG vector index successfully built and ready for queries!")

    finally:
        db.close()


if __name__ == "__main__":
    seed_database()
