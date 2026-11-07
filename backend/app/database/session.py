import logging
from sqlalchemy import create_engine, event
from sqlalchemy.orm import declarative_base, sessionmaker
from app.core.config import settings

logger = logging.getLogger(__name__)

Base = declarative_base()


def create_app_engine():
    if settings.FORCE_SQLITE:
        logger.info(f"FORCE_SQLITE is set. Using SQLite: {settings.SQLITE_FALLBACK_URL}")
        eng = create_engine(
            settings.SQLITE_FALLBACK_URL,
            connect_args={"check_same_thread": False, "timeout": 30}
        )

        @event.listens_for(eng, "connect")
        def set_sqlite_pragma(dbapi_connection, connection_record):
            cursor = dbapi_connection.cursor()
            cursor.execute("PRAGMA journal_mode=WAL")
            cursor.execute("PRAGMA synchronous=NORMAL")
            cursor.close()

        return eng

    try:
        engine_args = {"pool_pre_ping": True}
        if "postgresql" in settings.DATABASE_URL:
            engine_args["connect_args"] = {"connect_timeout": 1}

        eng = create_engine(settings.DATABASE_URL, **engine_args)
        with eng.connect() as conn:
            logger.info("Successfully connected to primary database.")
        return eng
    except Exception as e:
        logger.warning(
            f"Could not connect to primary database ({settings.DATABASE_URL}). "
            f"Falling back to local SQLite ({settings.SQLITE_FALLBACK_URL}). Reason: {e}"
        )
        eng = create_engine(
            settings.SQLITE_FALLBACK_URL,
            connect_args={"check_same_thread": False, "timeout": 30}
        )

        @event.listens_for(eng, "connect")
        def set_sqlite_pragma(dbapi_connection, connection_record):
            cursor = dbapi_connection.cursor()
            cursor.execute("PRAGMA journal_mode=WAL")
            cursor.execute("PRAGMA synchronous=NORMAL")
            cursor.close()

        return eng


engine = create_app_engine()
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
