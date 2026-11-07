import os
from typing import List
from pathlib import Path
from pydantic_settings import BaseSettings

# Absolute directory of backend root
BACKEND_DIR = Path(__file__).resolve().parent.parent.parent
DEFAULT_SQLITE_PATH = BACKEND_DIR / "ecommerce.db"


class Settings(BaseSettings):
    PROJECT_NAME: str = "AI-Powered E-Commerce Shopping Assistant"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api"
    DESCRIPTION: str = "AI-Native Agentic Commerce Platform with LangGraph Multi-Agent System & RAG"

    # Database
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL",
        "postgresql://postgres:postgres@localhost:5432/ecommerce_db"
    )
    SQLITE_FALLBACK_URL: str = f"sqlite:///{DEFAULT_SQLITE_PATH.as_posix()}"
    # Default to True so local development and pytest never wait on Postgres TCP timeouts
    FORCE_SQLITE: bool = os.getenv("FORCE_SQLITE", "true").lower() in ("true", "1", "yes")

    # Security & JWT
    SECRET_KEY: str = os.getenv("SECRET_KEY", "super-secret-production-quality-jwt-signing-key-2026")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days

    # AI & LLM Provider Configuration
    LLM_PROVIDER: str = os.getenv("LLM_PROVIDER", "mock").lower()
    OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY", "")
    GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY", "")
    GROQ_API_KEY: str = os.getenv("GROQ_API_KEY", "")
    LLM_MODEL: str = os.getenv("LLM_MODEL", "gpt-4o-mini")

    # CORS
    BACKEND_CORS_ORIGINS: List[str] = [
        "http://localhost",
        "http://localhost:4200",
        "http://127.0.0.1:4200",
        "http://localhost:80",
        "http://localhost:3000",
        "http://localhost:8000",
    ]

    # RAG Settings
    RAG_TOP_K: int = 5
    RAG_SIMILARITY_THRESHOLD: float = 0.35
    EMBEDDING_DIMENSION: int = 128

    class Config:
        env_file = ".env"
        case_sensitive = True
        extra = "allow"


settings = Settings()
