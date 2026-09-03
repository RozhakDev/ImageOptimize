from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    PROJECT_NAME: str = "ImageOptimize"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"
    
    MAX_IMAGE_PIXELS: int = 50000000
    MAX_PAYLOAD_SIZE_BYTES: int = 20971520
    DATABASE_URL: str = "sqlite:///./imageoptimize.db"
    DEFAULT_QUALITY_LOW: int = 10
    DEFAULT_QUALITY_HIGH: int = 95
    VIPS_DLL_PATH: str = ""
    
    STORAGE_DIR: str = "./storage"
    ENABLE_CLEANUP_CRON: bool = True
    CLEANUP_RETENTION_MINUTES: int = 60

    class Config:
        env_file = ".env"

settings = Settings()
