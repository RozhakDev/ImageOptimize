import os
import sys

from app.core.config import settings


def setup_vips():
    if sys.platform == "win32" and settings.VIPS_DLL_PATH:
        project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        dll_path = os.path.abspath(os.path.join(project_root, settings.VIPS_DLL_PATH))
        if os.path.exists(dll_path):
            os.environ["PATH"] = dll_path + os.pathsep + os.environ.get("PATH", "")
            if hasattr(os, "add_dll_directory"):
                os.add_dll_directory(dll_path)

setup_vips()
