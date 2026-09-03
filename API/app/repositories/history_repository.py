import time

from sqlalchemy.orm import Session

from app.domain.models.history import OptimizationHistory


class HistoryRepository:
    def __init__(self, db: Session):
        self.db = db

    def log_optimization(
        self, 
        original_filename: str, 
        original_bytes: int, 
        optimized_bytes: int, 
        optimized_format: str, 
        start_time: float
    ):
        processing_time = (time.time() - start_time) * 1000
        history = OptimizationHistory(
            original_filename=original_filename,
            original_size_bytes=original_bytes,
            optimized_size_bytes=optimized_bytes,
            optimized_format=optimized_format,
            processing_time_ms=processing_time
        )
        self.db.add(history)
        self.db.commit()
