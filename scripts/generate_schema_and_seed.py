import json
import random

random.seed(42)

CATEGORIES = [
    {
        "id": 1,
        "name": "Electronics & Audio",
        "slug": "electronics-audio",
        "description": "High-fidelity audio equipment, computing peripherals, and smart devices.",
        "icon": "headphones",
        "brands": ["AuraSound", "NovaTech", "Veloce", "SoundWave", "TitanLabs", "HyperPulse"],
        "items": [
            ("Wireless Noise-Canceling Headphones", 79.99, 299.99, {"battery": "35 hours", "connectivity": "Bluetooth 5.3", "anc": "Hybrid Active ANC"}),
            ("Mechanical Ergonomic Keyboard", 49.99, 159.99, {"switches": "Tactile Quiet", "backlight": "RGB Multi-Zone", "interface": "USB-C / 2.4GHz"}),
            ("Precision Wireless Optical Mouse", 29.99, 89.99, {"dpi": "4000 DPI", "sensors": "Dual Laser", "battery": "Rechargeable 70-day"}),
            ("Ultra-Wide Curved Productivity Monitor", 249.99, 599.99, {"resolution": "3440x1440", "refresh": "120Hz", "panel": "IPS Anti-Glare"}),
            ("Compact Fast-Charge Power Bank", 24.99, 69.99, {"capacity": "20000mAh", "power": "65W USB-C PD", "ports": "3 Ports"})
        ]
    },
    {
        "id": 2,
        "name": "Apparel & Activewear",
        "slug": "apparel-activewear",
        "description": "Performance athletic wear, weather-resistant jackets, and versatile everyday clothing.",
        "icon": "activity",
        "brands": ["ApexStyles", "ZenithGear", "SwiftFlow", "TrailCraft", "AeroKnit", "UrbanPeak"],
        "items": [
            ("Breathable Performance Running Shoes", 65.00, 150.00, {"material": "Recycled Mesh", "cushioning": "Responsive Foam", "drop": "8mm"}),
            ("Weatherproof Thermal Outdoor Parka", 99.00, 240.00, {"waterproof": "10000mm rating", "insulation": "Eco-Thermal Fill", "hood": "Adjustable"}),
            ("Moisture-Wicking Athletic Training Hoodie", 35.00, 75.00, {"fabric": "Stretch Poly-Blend", "fit": "Athletic", "pockets": "Zippered Side"}),
            ("Everyday Classic Organic Cotton Crewneck", 19.99, 45.00, {"material": "100% Combed Cotton", "pre_shrunk": "Yes", "care": "Machine Washable"}),
            ("All-Weather Flexible Hiking Pants", 45.00, 95.00, {"stretch": "4-Way Comfort", "pockets": "Reinforced Cargo", "finish": "Water-Repellent"})
        ]
    },
    {
        "id": 3,
        "name": "Home & Kitchen",
        "slug": "home-kitchen",
        "description": "Modern culinary appliances, ergonomic furniture, and living essentials.",
        "icon": "home",
        "brands": ["OmniHome", "CuisineMaster", "LivingCraft", "PureAir", "ErgoForm", "ArtisanBlend"],
        "items": [
            ("Programmable Stainless Coffee Brewer", 49.99, 149.99, {"capacity": "12 Cup", "carafe": "Thermal Stainless", "timer": "24-Hour Digital"}),
            ("Digital Convection Air Fryer Oven", 79.99, 189.99, {"capacity": "8-Quart", "presets": "10 Presets", "temp": "100F - 400F"}),
            ("Ergonomic Dynamic Lumbar Office Chair", 129.99, 349.99, {"mesh": "Breathable High-Density", "recline": "90-135 deg", "weight_cap": "300 lbs"}),
            ("Non-Stick Ceramic Cookware 10-Piece Set", 89.99, 219.99, {"coating": "Toxin-Free Ceramic", "oven_safe": "Up to 450F", "stoves": "All types"}),
            ("Smart Navigation Autonomous Floor Cleaner", 149.99, 399.99, {"suction": "3200Pa", "runtime": "140 min", "filter": "HEPA Sealed"})
        ]
    },
    {
        "id": 4,
        "name": "Smart Gadgets & Wearables",
        "slug": "smart-gadgets-wearables",
        "description": "Fitness tracking wearables, connected travel accessories, and smart organizers.",
        "icon": "watch",
        "brands": ["ChronosTech", "VentureGear", "OmniShield", "PulseTrack", "AeroGadget", "TerraWear"],
        "items": [
            ("All-Day Fitness Tracking Smartwatch", 89.99, 279.99, {"display": "AMOLED 1.4 inch", "battery": "8 Days", "waterproof": "5 ATM"}),
            ("Water-Resistant Commuter Backpack 30L", 45.00, 110.00, {"capacity": "30 Liters", "laptop_sleeve": "Up to 16 inch", "usb_pass": "Included"}),
            ("Minimalist RFID Shielded Card Wallet", 22.00, 50.00, {"material": "Full-Grain Eco Leather", "cards": "Up to 10 cards", "rfid": "13.56 MHz"}),
            ("Polarized UV400 Lightweight Sunglasses", 25.00, 75.00, {"protection": "100% UV400", "frame": "Flexible TR90", "polarization": "Grade A"}),
            ("High-Density Cushion Exercise Mat", 20.00, 48.00, {"thickness": "6mm", "material": "Eco TPE", "texture": "Non-Slip Dual Surface"})
        ]
    }
]

MODIFIERS = ["Pro", "Ultra", "Elite", "Max", "Prime", "Studio", "Signature", "Series X", "Flex", "Stealth"]
COLORS = ["Matte Black", "Silver Gray", "Deep Navy", "Pure White", "Forest Green", "Graphite"]

products = []
sku_counter = 1000
total_target = 5120
per_category = total_target // len(CATEGORIES)

for cat in CATEGORIES:
    cat_id = cat["id"]
    cat_name = cat["name"]
    brands = cat["brands"]
    items = cat["items"]
    
    for i in range(per_category):
        sku_counter += 1
        item_name, min_p, max_p, base_specs = random.choice(items)
        brand = random.choice(brands)
        mod = random.choice(MODIFIERS)
        color = random.choice(COLORS)
        
        full_name = f"{brand} {item_name} {mod}"
        price = round(random.uniform(min_p, max_p), 2)
        has_orig = random.random() < 0.4
        orig_price = round(price * random.uniform(1.15, 1.35), 2) if has_orig else None
        rating = round(random.uniform(3.8, 5.0), 1)
        review_count = random.randint(15, 950)
        stock_qty = random.randint(10, 150)
        
        specs = dict(base_specs)
        specs["Color"] = color
        specs["Brand"] = brand
        specs["Model"] = f"{brand[:3].upper()}-{sku_counter}"
        specs["Warranty"] = random.choice(["1 Year Limited", "2 Year Limited", "Lifetime Hardware"])
        
        desc = f"The {full_name} is engineered for peak reliability and performance. Features {color} aesthetic finish and verified quality with a {rating} customer score."
        
        sku = f"{cat['slug'][:4].upper()}-{sku_counter}"
        slug = f"{brand.lower()}-{item_name.lower().replace(' ', '-')}-{mod.lower()}-{sku_counter}"
        
        products.append({
            "id": sku_counter - 1000,
            "sku": sku,
            "name": full_name,
            "slug": slug,
            "category_id": cat_id,
            "brand": brand,
            "price": price,
            "original_price": orig_price,
            "rating": rating,
            "review_count": review_count,
            "description": desc,
            "specifications": json.dumps(specs),
            "features": json.dumps([f"Premium {brand} construction", f"Optimized {color} styling", f"Backed by {specs['Warranty']}"]),
            "tags": json.dumps([brand.lower(), cat_name.lower(), color.lower()]),
            "stock": stock_qty
        })

print(f"Generated {len(products)} products.")

v1_sql = """-- V1 Initial Schema for PostgreSQL and H2 Compatibility
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    preferred_categories TEXT,
    preferred_brands TEXT,
    max_budget NUMERIC(12, 2),
    favorite_features TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    category_id BIGINT REFERENCES product_categories(id),
    brand VARCHAR(100) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    original_price NUMERIC(12, 2),
    rating NUMERIC(3, 2) DEFAULT 0.0,
    review_count INTEGER DEFAULT 0,
    description TEXT NOT NULL,
    specifications TEXT,
    features TEXT,
    tags TEXT,
    image_url VARCHAR(500),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_embeddings (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    embedding_vector TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shopping_carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    discount_code VARCHAR(50),
    discount_amount NUMERIC(12, 2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES shopping_carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    subtotal NUMERIC(12, 2) NOT NULL,
    discount_amount NUMERIC(12, 2) DEFAULT 0.00,
    tax_amount NUMERIC(12, 2) NOT NULL,
    shipping_amount NUMERIC(12, 2) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    shipping_address TEXT NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    estimated_delivery TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    total_price NUMERIC(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS conversation_sessions (
    id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conversation_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL REFERENCES conversation_sessions(id) ON DELETE CASCADE,
    sender_type VARCHAR(50) NOT NULL,
    agent_type VARCHAR(100),
    message_content TEXT NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS agent_execution_logs (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100),
    agent_type VARCHAR(100) NOT NULL,
    tool_name VARCHAR(100),
    tool_arguments TEXT,
    tool_result TEXT,
    execution_duration_ms BIGINT,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS approval_requests (
    id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    session_id VARCHAR(100),
    action_type VARCHAR(100) NOT NULL,
    action_payload TEXT NOT NULL,
    summary TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);
CREATE INDEX IF NOT EXISTS idx_products_rating ON products(rating);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_conv_messages_session ON conversation_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_approvals_user_status ON approval_requests(user_id, status);
CREATE INDEX IF NOT EXISTS idx_agent_logs_session ON agent_execution_logs(session_id);

-- Seed Roles
INSERT INTO roles (name) VALUES ('ROLE_CUSTOMER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
"""

with open("database/migrations/V1__init_schema.sql", "w", encoding="utf-8") as f:
    f.write(v1_sql)
with open("backend/src/main/resources/db/migration/V1__init_schema.sql", "w", encoding="utf-8") as f:
    f.write(v1_sql)

lines = ["-- V2 Seed 5000+ Products, Categories, and Default Users\n"]

for cat in CATEGORIES:
    lines.append(f"INSERT INTO product_categories (id, name, slug, description, icon) VALUES ({cat['id']}, '{cat['name']}', '{cat['slug']}', '{cat['description']}', '{cat['icon']}') ON CONFLICT (id) DO NOTHING;\n")

pwd_hash = "$2a$10$7Zq4vP4x87lJq3wJzI5Q1eGZ4TqH5iZ7mGjO6W0J9R8b1C2d3E4F."
lines.append(f"INSERT INTO users (id, email, password_hash, full_name, is_active) VALUES (1, 'customer@example.com', '{pwd_hash}', 'Alex Mercer', true) ON CONFLICT (id) DO NOTHING;\n")
lines.append(f"INSERT INTO users (id, email, password_hash, full_name, is_active) VALUES (2, 'admin@example.com', '{pwd_hash}', 'System Administrator', true) ON CONFLICT (id) DO NOTHING;\n")
lines.append("INSERT INTO user_roles (user_id, role_id) VALUES (1, 1) ON CONFLICT DO NOTHING;\n")
lines.append("INSERT INTO user_roles (user_id, role_id) VALUES (2, 2) ON CONFLICT DO NOTHING;\n")
lines.append("INSERT INTO user_preferences (user_id, preferred_categories, preferred_brands, max_budget, favorite_features) VALUES (1, '[\"Electronics & Audio\", \"Apparel & Activewear\"]', '[\"AuraSound\", \"ApexStyles\"]', 500.00, '[\"Wireless\", \"Active ANC\"]') ON CONFLICT DO NOTHING;\n")
lines.append("INSERT INTO shopping_carts (id, user_id, status) VALUES (1, 1, 'ACTIVE') ON CONFLICT DO NOTHING;\n")

lines.append("\n-- Products Batch Seeding\n")
for p in products:
    orig = f"{p['original_price']}" if p['original_price'] is not None else "NULL"
    spec_escaped = p['specifications'].replace("'", "''")
    feat_escaped = p['features'].replace("'", "''")
    tags_escaped = p['tags'].replace("'", "''")
    desc_escaped = p['description'].replace("'", "''")
    name_escaped = p['name'].replace("'", "''")
    
    img = f"https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60"
    lines.append(
        f"INSERT INTO products (id, sku, name, slug, category_id, brand, price, original_price, rating, review_count, description, specifications, features, tags, image_url, is_available) "
        f"VALUES ({p['id']}, '{p['sku']}', '{name_escaped}', '{p['slug']}', {p['category_id']}, '{p['brand']}', {p['price']}, {orig}, {p['rating']}, {p['review_count']}, '{desc_escaped}', '{spec_escaped}', '{feat_escaped}', '{tags_escaped}', '{img}', true) ON CONFLICT (id) DO NOTHING;\n"
    )
    lines.append(
        f"INSERT INTO inventory (product_id, available_quantity, reserved_quantity, version) VALUES ({p['id']}, {p['stock']}, 0, 0) ON CONFLICT (product_id) DO NOTHING;\n"
    )

with open("database/migrations/V2__seed_5000_products.sql", "w", encoding="utf-8") as f:
    f.writelines(lines)
with open("backend/src/main/resources/db/migration/V2__seed_5000_products.sql", "w", encoding="utf-8") as f:
    f.writelines(lines)

print("V1 and V2 SQL migrations generated successfully.")
