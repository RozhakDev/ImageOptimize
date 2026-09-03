import os
import uuid

from app.core.config import settings


class StorageService:
    @staticmethod
    def _ensure_dir(path: str):
        os.makedirs(path, exist_ok=True)

    @staticmethod
    def init_storage():
        StorageService._ensure_dir(os.path.join(settings.STORAGE_DIR, "originals"))
        StorageService._ensure_dir(os.path.join(settings.STORAGE_DIR, "optimized"))

    @staticmethod
    def save_original(image_bytes: bytes, filename: str) -> str:
        StorageService.init_storage()
        ext = filename.rsplit('.', 1)[-1] if '.' in filename else 'bin'
        unique_name = f"{uuid.uuid4().hex}.{ext}"
        path = os.path.join(settings.STORAGE_DIR, "originals", unique_name)
        with open(path, "wb") as f:
            f.write(image_bytes)
        return unique_name

    @staticmethod
    def save_optimized(image_bytes: bytes, fmt: str) -> str:
        StorageService.init_storage()
        unique_name = f"{uuid.uuid4().hex}.{fmt}"
        path = os.path.join(settings.STORAGE_DIR, "optimized", unique_name)
        with open(path, "wb") as f:
            f.write(image_bytes)
        return unique_name
