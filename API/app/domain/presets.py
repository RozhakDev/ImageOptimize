class PresetManager:
    @staticmethod
    def get_preset_config(preset: str, t_kb: int | None, r_w: int | None, r_h: int | None, fmt: str, smart: bool, preserve_meta: bool) -> tuple[int, int | None, int | None, str, bool, bool]:
        strip_meta = not preserve_meta
        if preset == "balanced":
            return t_kb or 300, r_w, r_h, "auto", smart, True
        if preset == "high_quality":
            return t_kb or 1024, r_w, r_h, "auto", smart, False
        if preset == "maximum_compression":
            return t_kb or 100, r_w, r_h, "auto", smart, True
        if preset == "web":
            return t_kb or 200, r_w, r_h, "webp", smart, True
        if preset == "thumbnail":
            return t_kb or 50, r_w or 200, r_h or 200, "auto", True, True
        return t_kb or 500, r_w, r_h, fmt, smart, strip_meta
