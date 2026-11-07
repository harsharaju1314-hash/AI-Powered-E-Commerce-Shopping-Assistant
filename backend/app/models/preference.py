import datetime
from sqlalchemy import Column, Integer, Float, Text, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from app.database.session import Base


class CustomerPreference(Base):
    __tablename__ = "customer_preferences"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    preferred_categories = Column(Text, nullable=True)  # JSON-encoded list of categories
    preferred_brands = Column(Text, nullable=True)      # JSON-encoded list of brands
    max_budget = Column(Float, nullable=True)
    favorite_features = Column(Text, nullable=True)     # JSON-encoded list of features
    notes = Column(Text, nullable=True)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)

    user = relationship("User", back_populates="preference")
