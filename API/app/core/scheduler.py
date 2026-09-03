import logging
import os
import time

from apscheduler.schedulers.background import BackgroundScheduler

from app.core.config import settings

logger = logging.getLogger(__name__)


class GarbageCollector:
    @staticmethod
    def clean_storage():
        retention_seconds = settings.CLEANUP_RETENTION_MINUTES * 60
        now = time.time()
        
        for sub_dir in ["originals", "optimized"]:
            target_dir = os.path.join(settings.STORAGE_DIR, sub_dir)
            if not os.path.exists(target_dir):
                continue
                
            for filename in os.listdir(target_dir):
                filepath = os.path.join(target_dir, filename)
                if os.path.isfile(filepath):
                    file_mtime = os.path.getmtime(filepath)
                    if now - file_mtime > retention_seconds:
                        try:
                            os.remove(filepath)
                            logger.info(f"SYS_GC_EVICTED: Purged expired asset {filepath}")
                        except Exception as e:
                            logger.error(f"SYS_CRITICAL_FAULT: Garbage collection failed - {str(e)}")


def init_scheduler():
    if not settings.ENABLE_CLEANUP_CRON:
        return None
        
    scheduler = BackgroundScheduler()
    scheduler.add_job(GarbageCollector.clean_storage, 'interval', minutes=10)
    scheduler.start()
    return scheduler
