from typing import Optional

from fastapi import APIRouter, Depends, File, Form, Request, UploadFile
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.exceptions import InvalidImageException, PayloadSizeException
from app.domain.schemas import JSendErrorResponse, JSendResponse
from app.infrastructure.database import get_db
from app.services.orchestrator import OptimizationOrchestrator

router = APIRouter()

@router.post(
    "/", 
    summary="Optimize Image", 
    response_model=JSendResponse,
    responses={
        400: {
            "model": JSendErrorResponse, 
            "description": "Bad Request / Unsupported Format",
            "content": {
                "application/json": {
                    "example": {"status": "fail", "message": "ERR_UNRECOGNIZED_FORMAT: The target encoding format is not supported by the core engine.", "data": None}
                }
            }
        },
        413: {
            "model": JSendErrorResponse, 
            "description": "Payload Too Large",
            "content": {
                "application/json": {
                    "example": {"status": "fail", "message": "ERR_PAYLOAD_LIMIT_EXCEEDED: The payload volume exceeds the maximum allowable threshold.", "data": None}
                }
            }
        },
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
async def optimize_image(
    request: Request,
    file: UploadFile = File(..., description="File gambar sumber (Mendukung: JPEG, PNG)"),
    target_size_kb: Optional[int] = Form(None, gt=0, description="Target KB. Kosongkan jika menggunakan preset.", json_schema_extra={"example": 300}),
    resize_width: Optional[int] = Form(None, gt=0, description="Lebar piksel target.", json_schema_extra={"example": 1024}),
    resize_height: Optional[int] = Form(None, gt=0, description="Tinggi piksel target.", json_schema_extra={"example": 768}),
    output_format: str = Form("auto", description="Format output target: auto, jpeg, webp, avif, png", json_schema_extra={"example": "auto"}),
    preset: str = Form("custom", description="Preset: custom, balanced, high_quality, maximum_compression, web, thumbnail", json_schema_extra={"example": "custom"}),
    smart_crop: bool = Form(False, description="Aktifkan pemotongan cerdas jika lebar dan tinggi diatur.", json_schema_extra={"example": False}),
    preserve_metadata: bool = Form(False, description="Pertahankan metadata EXIF gambar.", json_schema_extra={"example": False}),
    db: Session = Depends(get_db)
):
    if not file.content_type.startswith("image/"):
        raise InvalidImageException()
        
    image_bytes = await file.read()
    
    if not image_bytes:
        raise InvalidImageException("ERR_EMPTY_STREAM: The provided byte stream contains no data.")
        
    if len(image_bytes) > settings.MAX_PAYLOAD_SIZE_BYTES:
        raise PayloadSizeException()
    
    response_data = OptimizationOrchestrator.execute(
        image_bytes=image_bytes,
        filename=file.filename or "unknown.bin",
        base_url=str(request.base_url).rstrip('/'),
        db=db,
        target_size_kb=target_size_kb,
        resize_width=resize_width,
        resize_height=resize_height,
        output_format=output_format,
        preset=preset,
        smart_crop=smart_crop,
        preserve_metadata=preserve_metadata
    )
    
    return JSendResponse(status="success", message="IMG_OPTIMIZATION_SUCCESS", data=response_data)
