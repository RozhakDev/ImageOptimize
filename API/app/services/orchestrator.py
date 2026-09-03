import time

from sqlalchemy.orm import Session

from app.domain.schemas import OptimizationData
from app.repositories.history_repository import HistoryRepository
from app.services.optimization_service import OptimizationService
from app.services.storage_service import StorageService


class OptimizationOrchestrator:
    @staticmethod
    def execute(
        image_bytes: bytes,
        filename: str,
        base_url: str,
        db: Session,
        target_size_kb: int | None,
        resize_width: int | None,
        resize_height: int | None,
        output_format: str,
        preset: str,
        smart_crop: bool,
        preserve_metadata: bool
    ) -> OptimizationData:
        start_time = time.time()
        
        optimized_bytes, final_format = OptimizationService.compress_to_target_size(
            image_bytes=image_bytes,
            target_size_kb=target_size_kb,
            resize_width=resize_width,
            resize_height=resize_height,
            output_format=output_format,
            preset=preset,
            smart_crop=smart_crop,
            preserve_metadata=preserve_metadata
        )
        
        original_size = len(image_bytes)
        optimized_size = len(optimized_bytes)
        compression_ratio = round((1 - (optimized_size / original_size)) * 100, 2)
        processing_time = round((time.time() - start_time) * 1000, 2)
        
        orig_name = StorageService.save_original(image_bytes, filename)
        opt_name = StorageService.save_optimized(optimized_bytes, final_format)
        
        orig_url = f"{base_url}/static/originals/{orig_name}"
        opt_url = f"{base_url}/static/optimized/{opt_name}"
        
        HistoryRepository(db).log_optimization(
            original_filename=filename,
            original_bytes=original_size,
            optimized_bytes=optimized_size,
            optimized_format=final_format,
            start_time=start_time
        )
        
        return OptimizationData(
            original_url=orig_url,
            optimized_url=opt_url,
            original_size_bytes=original_size,
            optimized_size_bytes=optimized_size,
            compression_ratio=f"{compression_ratio}%",
            processing_time_ms=f"{processing_time}ms"
        )
