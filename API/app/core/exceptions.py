import logging

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

logger = logging.getLogger(__name__)


class AppException(Exception):
    def __init__(self, message: str, status_code: int):
        self.message = message
        self.status_code = status_code


class InvalidImageException(AppException):
    def __init__(self, message: str = "ERR_UNSUPPORTED_MEDIA_TYPE: The transmitted stream is not recognized as a valid image matrix."):
        super().__init__(message, 400)


class PayloadSizeException(AppException):
    def __init__(self, message: str = "ERR_PAYLOAD_LIMIT_EXCEEDED: The payload volume exceeds the maximum allowable threshold."):
        super().__init__(message, 413)


class UnsupportedFormatException(AppException):
    def __init__(self, message: str = "ERR_UNRECOGNIZED_FORMAT: The target encoding format is not supported by the core engine."):
        super().__init__(message, 400)


def register_exception_handlers(app: FastAPI):
    @app.exception_handler(AppException)
    async def app_exception_handler(request: Request, exc: AppException):
        return JSONResponse(
            status_code=exc.status_code,
            content={"status": "fail", "message": exc.message, "data": None}
        )

    @app.exception_handler(Exception)
    async def global_exception_handler(request: Request, exc: Exception):
        logger.error(f"SYS_CRITICAL_FAULT: Unhandled exception intercepted - {str(exc)}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"status": "error", "message": "ERR_INTERNAL_FAULT: A system-level exception interrupted the execution flow.", "data": None}
        )
