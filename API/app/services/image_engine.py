import pyvips

from app.core.config import settings
from app.core.exceptions import InvalidImageException


class ImageEngine:
    @staticmethod
    def load_from_buffer(buffer: bytes) -> pyvips.Image:
        try:
            img = pyvips.Image.new_from_buffer(buffer, "")
        except Exception:
            raise InvalidImageException("ERR_CORRUPT_BUFFER: The provided stream could not be decoded.")
            
        if img.width * img.height > settings.MAX_IMAGE_PIXELS:
            raise InvalidImageException("ERR_DECOMPRESSION_BOMB: Image dimensions exceed safety thresholds.")
        return img

    @staticmethod
    def resize(image: pyvips.Image, width: int | None, height: int | None, smart: bool) -> pyvips.Image:
        if not width and not height:
            return image
        
        if smart and width and height:
            return image.smartcrop(width, height, interesting="attention")

        orig_width = image.width
        orig_height = image.height

        if width and height:
            scale_w = width / orig_width
            scale_h = height / orig_height
            scale = min(scale_w, scale_h)
        elif width:
            scale = width / orig_width
        elif height:
            scale = height / orig_height
        else:
            scale = 1.0

        if scale == 1.0:
            return image
            
        return image.resize(scale)

    @staticmethod
    def save_to_jpeg_buffer(image: pyvips.Image, quality: int, strip: bool) -> bytes:
        if image.hasalpha():
            image = image.flatten(background=[255, 255, 255])
        return image.write_to_buffer(".jpg", Q=quality, optimize_coding=True, strip=strip)

    @staticmethod
    def save_to_webp_buffer(image: pyvips.Image, quality: int, strip: bool) -> bytes:
        return image.write_to_buffer(".webp", Q=quality, lossless=False, effort=2, strip=strip)

    @staticmethod
    def save_to_avif_buffer(image: pyvips.Image, quality: int, strip: bool) -> bytes:
        return image.write_to_buffer(".avif", Q=quality, effort=2, strip=strip)

    @staticmethod
    def save_to_png_buffer(image: pyvips.Image, compression: int, strip: bool) -> bytes:
        return image.write_to_buffer(".png", compression=compression, strip=strip)

    @staticmethod
    def analyze_image(image: pyvips.Image) -> dict:
        loader = "unknown"
        try:
            if image.get_typeof("vips-loader") != 0:
                loader = image.get("vips-loader")
        except Exception:
            pass
            
        return {
            "width": image.width,
            "height": image.height,
            "bands": image.bands,
            "has_alpha": image.hasalpha(),
            "format": loader
        }
