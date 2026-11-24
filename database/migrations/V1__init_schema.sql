-- V1 Initial Schema for PostgreSQL and H2 Compatibility
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
