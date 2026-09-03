import logging

import pyvips

from app.core.config import settings
from app.core.exceptions import UnsupportedFormatException
from app.domain.presets import PresetManager
from app.services.image_engine import ImageEngine
from app.services.perceptual_evaluator import PerceptualEvaluator

logger = logging.getLogger(__name__)


class OptimizationService:
    @staticmethod
    def _encode_image(image: pyvips.Image, fmt: str, quality: int, strip: bool) -> bytes:
        if fmt in ["jpeg", "jpg"]:
            return ImageEngine.save_to_jpeg_buffer(image, quality, strip)
        elif fmt == "webp":
            return ImageEngine.save_to_webp_buffer(image, quality, strip)
        elif fmt == "avif":
            return ImageEngine.save_to_avif_buffer(image, quality, strip)
        elif fmt == "png":
            comp_level = min(9, max(0, int((quality / 100) * 9)))
            return ImageEngine.save_to_png_buffer(image, comp_level, strip)
        raise UnsupportedFormatException(f"Format_not_supported: {fmt}")

    @staticmethod
    def _binary_search_compress(image: pyvips.Image, fmt: str, target_bytes: int, strip: bool) -> bytes:
        low = settings.DEFAULT_QUALITY_LOW
        high = settings.DEFAULT_QUALITY_HIGH
        
        best_buffer = OptimizationService._encode_image(image, fmt, high, strip)
        if len(best_buffer) <= target_bytes:
            return best_buffer

        best_buffer = OptimizationService._encode_image(image, fmt, low, strip)
        if len(best_buffer) > target_bytes:
            return best_buffer

        while low <= high:
            mid = (low + high) // 2
            current_buffer = OptimizationService._encode_image(image, fmt, mid, strip)
            current_size = len(current_buffer)

            if current_size <= target_bytes:
                best_buffer = current_buffer
                low = mid + 1
            else:
                high = mid - 1

        return best_buffer

    @staticmethod
    def _evaluate_candidates(original: pyvips.Image, formats: list[str], target_bytes: int, strip_meta: bool) -> tuple[bytes | None, str]:
        best_buffer = None
        best_fmt = "jpeg"
        best_score = float('inf')
        
        for fmt in formats:
            try:
                current_buffer = OptimizationService._binary_search_compress(original, fmt, target_bytes, strip_meta)
                current_size = len(current_buffer)
                
                if current_size <= target_bytes and current_size > 0:
                    candidate_img = ImageEngine.load_from_buffer(current_buffer)
                    score = PerceptualEvaluator.evaluate_de00(original, candidate_img)
                    if score < best_score:
                        best_score = score
                        best_buffer = current_buffer
                        best_fmt = fmt
            except Exception as loop_e:
                logger.error(f"Error_encoding_{fmt}: {str(loop_e)}")
        
        return best_buffer, best_fmt

    @staticmethod
    def compress_to_target_size(
        image_bytes: bytes,
        target_size_kb: int | None = None,
        resize_width: int | None = None,
        resize_height: int | None = None,
        output_format: str = "auto",
        preset: str = "custom",
        smart_crop: bool = False,
        preserve_metadata: bool = False
    ) -> tuple[bytes, str]:
        
        t_kb, r_w, r_h, fmt_out, smart, strip_meta = PresetManager.get_preset_config(
            preset, target_size_kb, resize_width, resize_height, output_format, smart_crop, preserve_metadata
        )
        
        target_size_bytes = t_kb * 1024
        
        img = ImageEngine.load_from_buffer(image_bytes)
        
        if r_w or r_h:
            img = ImageEngine.resize(img, width=r_w, height=r_h, smart=smart)
            
        if fmt_out == "auto":
            best_buffer, best_fmt = OptimizationService._evaluate_candidates(img, ["webp", "avif", "jpeg", "png"], target_size_bytes, strip_meta)
            
            if not best_buffer:
                best_buffer = OptimizationService._binary_search_compress(img, "jpeg", target_size_bytes, strip_meta)
                best_fmt = "jpeg"
                
            return best_buffer, best_fmt
        else:
            best_buffer = OptimizationService._binary_search_compress(img, fmt_out, target_size_bytes, strip_meta)
            return best_buffer, fmt_out
