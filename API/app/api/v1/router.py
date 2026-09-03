from fastapi import APIRouter

from app.api.v1.endpoints import analyze, optimize

api_router = APIRouter()
api_router.include_router(optimize.router, prefix="/images", tags=["Image Optimization"])
api_router.include_router(analyze.router, prefix="/analyze", tags=["Image Analysis"])
