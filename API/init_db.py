from app.infrastructure.database import Base, engine
from app.domain.models.history import OptimizationHistory

Base.metadata.create_all(bind=engine)
