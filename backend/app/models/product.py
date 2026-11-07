import datetime
from sqlalchemy import Column, Integer, String, Float, Boolean, Text, DateTime, ForeignKey, Index
from sqlalchemy.orm import relationship
from app.database.session import Base


class ProductCategory(Base):
    __tablename__ = "product_categories"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), unique=True, index=True, nullable=False)
    slug = Column(String(120), unique=True, index=True, nullable=False)
    description = Column(Text, nullable=True)
    icon = Column(String(50), nullable=True, default="tag")
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    products = relationship("Product", back_populates="category_rel")


class Product(Base):
    __tablename__ = "products"

    id = Column(Integer, primary_key=True, index=True)
    sku = Column(String(64), unique=True, index=True, nullable=False)
    name = Column(String(255), index=True, nullable=False)
    slug = Column(String(280), unique=True, index=True, nullable=False)
    category_id = Column(Integer, ForeignKey("product_categories.id", ondelete="SET NULL"), nullable=True, index=True)
    category_name = Column(String(100), index=True, nullable=False)
    brand = Column(String(100), index=True, nullable=False)
    price = Column(Float, index=True, nullable=False)
    original_price = Column(Float, nullable=True)
    rating = Column(Float, default=4.5, index=True)
    review_count = Column(Integer, default=0)
    inventory_count = Column(Integer, default=10, index=True)
    is_available = Column(Boolean, default=True, index=True)
    description = Column(Text, nullable=False)
    specifications = Column(Text, nullable=True)  # JSON-encoded dict of key/value specs
    features = Column(Text, nullable=True)        # JSON-encoded list of bullet points
    tags = Column(Text, nullable=True)            # JSON-encoded list of keyword tags
    image_url = Column(String(500), nullable=True)
    embedding = Column(Text, nullable=True)       # JSON-encoded list of floats for vector search
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)

    category_rel = relationship("ProductCategory", back_populates="products")
    cart_items = relationship("CartItem", back_populates="product", cascade="all, delete-orphan")
    order_items = relationship("OrderItem", back_populates="product")

    __table_args__ = (
        Index("ix_products_cat_price", "category_name", "price"),
        Index("ix_products_brand_cat", "brand", "category_name"),
    )
