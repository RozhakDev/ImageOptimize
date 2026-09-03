import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

import app.core.vips_bootstrap
import app.domain.models.history
from app.api.v1.router import api_router
from app.core.config import settings
from app.core.exceptions import register_exception_handlers
from app.core.logger import setup_logging
from app.core.scheduler import init_scheduler
from app.domain.schemas import HealthData, JSendErrorResponse, JSendResponse
from app.infrastructure.database import Base, engine

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    setup_logging()
    logger.info("SYS_BOOT: API Gateway initialized.")
    Base.metadata.create_all(bind=engine)
    logger.info("SYS_DB_SYNC: Database schema synchronized.")
    scheduler = init_scheduler()
    yield
    if scheduler:
        scheduler.shutdown()
        logger.info("SYS_SHUTDOWN: GC Scheduler terminated.")


def create_app() -> FastAPI:
    tags_metadata = [
        {
            "name": "Image Optimization",
            "description": "Layanan utama (Core Service) yang mengorkestrasi kompresi biner gambar secara asinkron dengan preservasi metadata EXIF dan optimasi rasio ruang warna (Color Space Optimization).",
        },
        {
            "name": "Image Analysis",
            "description": "Layanan inspeksi matriks biner untuk mengekstrak metrik gambar tingkat rendah (resolusi, alpha channel, byte bands) secara instan tanpa membebani heap memory.",
        },
        {
            "name": "System",
            "description": "Layanan telemetri (Telemetry Service) untuk memonitor siklus hidup Gateway API, status I/O, dan metrik kesehatan mesin kompresi.",
        },
    ]

    app = FastAPI(
        title=settings.PROJECT_NAME,
        version=settings.VERSION,
        docs_url="/api/docs/",
        lifespan=lifespan,
        summary="Enterprise-Grade Image Optimization Gateway",
        description="Spesifikasi teknis API ImageOptimize Gateway.\nBerdiri di atas arsitektur C-bindings `libvips` untuk efisiensi memori ekstrem dan operasi I/O asinkron yang non-blocking.\nInfrastruktur ini didesain untuk menangani lalu lintas kompresi skala raksasa (High-Throughput Workloads) secara instan.",
        contact={
            "name": "Engineering Team",
            "email": "engineering@imageengine.id",
        },
        openapi_tags=tags_metadata,
        openapi_url=f"{settings.API_V1_STR}/openapi.json",
        swagger_ui_parameters={
            "defaultModelsExpandDepth": -1,
            "displayRequestDuration": True,
            "docExpansion": "none",
            "filter": True,
        }
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    os.makedirs(settings.STORAGE_DIR, exist_ok=True)
    app.mount("/static", StaticFiles(directory=settings.STORAGE_DIR), name="static")

    app.include_router(api_router, prefix=settings.API_V1_STR)
    register_exception_handlers(app)

    @app.get(
        "/health", 
        tags=["System"], 
        summary="Health Check", 
        response_model=JSendResponse,
        responses={
            500: {
                "model": JSendErrorResponse, 
                "description": "Internal Server Error",
                "content": {
                    "application/json": {
                        "example": {"status": "error", "message": "ERR_INTERNAL_FAULT: A system-level exception interrupted the execution flow.", "data": None}
                    }
                }
            }
        }
    )
    def health_check():
        return JSendResponse(status="success", message="SVC_HEALTH_OK", data=HealthData(version=settings.VERSION))
        
    return app

app = create_app()
