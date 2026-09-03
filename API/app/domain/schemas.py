from typing import Any

from pydantic import BaseModel, Field


class JSendResponse(BaseModel):
    status: str = Field(..., description="JSend status", examples=["success"])
    message: str = Field(..., description="Human readable message", examples=["SVC_HEALTH_OK"])
    data: Any = Field(None, description="Payload data")


class JSendErrorResponse(BaseModel):
    status: str = Field(..., description="JSend status", examples=["error"])
    message: str = Field(..., description="Human readable message", examples=["ERR_UNSUPPORTED_MEDIA_TYPE"])


class HealthData(BaseModel):
    version: str = Field(..., description="Application version", examples=["1.0.0"])


class OptimizationData(BaseModel):
    original_url: str = Field(..., description="URL to access the original file")
    optimized_url: str = Field(..., description="URL to access the optimized file")
    original_size_bytes: int = Field(..., description="Original image size in bytes")
    optimized_size_bytes: int = Field(..., description="Optimized image size in bytes")
    compression_ratio: str = Field(..., description="Compression ratio percentage")
    processing_time_ms: str = Field(..., description="Processing time in milliseconds")


class ImageAnalysisData(BaseModel):
    width: int = Field(..., description="Image width in pixels", examples=[1920])
    height: int = Field(..., description="Image height in pixels", examples=[1080])
    bands: int = Field(..., description="Number of color channels", examples=[3])
    has_alpha: bool = Field(..., description="Presence of alpha channel", examples=[False])
    format: str = Field(..., description="Internal format loader", examples=["jpegload"])
