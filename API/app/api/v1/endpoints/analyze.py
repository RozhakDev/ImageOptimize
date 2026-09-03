from fastapi import APIRouter, File, UploadFile

from app.core.exceptions import InvalidImageException
from app.domain.schemas import ImageAnalysisData, JSendErrorResponse, JSendResponse
from app.services.image_engine import ImageEngine

router = APIRouter()

@router.post(
    "/", 
    summary="Analyze Image", 
    response_model=JSendResponse,
    responses={
        400: {
            "model": JSendErrorResponse, 
            "description": "Invalid Image Data",
            "content": {
                "application/json": {
                    "example": {"status": "fail", "message": "ERR_UNSUPPORTED_MEDIA_TYPE: The transmitted stream is not recognized as a valid image matrix.", "data": None}
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
async def analyze_image(file: UploadFile = File(..., description="File gambar sumber untuk dianalisis")):
    if not file.content_type.startswith("image/"):
        raise InvalidImageException()
    
    image_bytes = await file.read()
    if not image_bytes:
        raise InvalidImageException("ERR_EMPTY_STREAM: The provided byte stream contains no data.")
        
    img = ImageEngine.load_from_buffer(image_bytes)
    analysis = ImageEngine.analyze_image(img)
    return JSendResponse(status="success", message="IMG_ANALYSIS_SUCCESS", data=ImageAnalysisData(**analysis))
