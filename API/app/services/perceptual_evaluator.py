import pyvips


class PerceptualEvaluator:
    @staticmethod
    def evaluate_de00(original: pyvips.Image, compressed: pyvips.Image) -> float:
        try:
            if original.hasalpha():
                original = original.flatten(background=[255, 255, 255])
            if compressed.hasalpha():
                compressed = compressed.flatten(background=[255, 255, 255])
            lab1 = original.colourspace("lab")
            lab2 = compressed.colourspace("lab")
            diff = lab1.dE00(lab2)
            return diff.avg()
        except Exception:
            return 999.0
