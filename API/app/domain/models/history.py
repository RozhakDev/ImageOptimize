from datetime import datetime, timezone

from sqlalchemy import Column, DateTime, Float, Integer, String

from app.infrastructure.database import Base


class OptimizationHistory(Base):
    __tablename__ = "optimization_history"
    id = Column(Integer, primary_key=True, index=True)
    original_filename = Column(String, index=True)
    original_size_bytes = Column(Integer)
    optimized_size_bytes = Column(Integer)
    optimized_format = Column(String)
    processing_time_ms = Column(Float)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
