import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings
from app.database.session import Base, engine, SessionLocal
from app.database import base  # Ensure all models are imported
from app.api.api import api_router
from app.rag.engine import rag_engine

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("ecommerce_app")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: ensure tables are created
    logger.info("Initializing database tables...")
    Base.metadata.create_all(bind=engine)

    # Pre-index catalog in RAG engine if products already exist
    try:
        db = SessionLocal()
        rag_engine.index_catalog(db)
        db.close()
    except Exception as e:
        logger.warning(f"Catalog indexing at startup skipped or deferred: {e}")

    yield

    logger.info("Shutting down AI-Powered E-Commerce Assistant...")


app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description=settings.DESCRIPTION,
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url=f"{settings.API_V1_STR}/openapi.json"
)

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/", tags=["Root"])
def root():
    return {
        "app": settings.PROJECT_NAME,
        "version": settings.VERSION,
        "status": "online",
        "documentation": "/docs",
        "api_v1": settings.API_V1_STR,
        "agents": [
            "Product Discovery Agent",
            "Recommendation Agent",
            "Cart Management Agent",
            "Order Management Agent"
        ]
    }


# Include all API routes under /api
app.include_router(api_router, prefix=settings.API_V1_STR)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
