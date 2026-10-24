import os
import sys
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

# Add src to path
sys.path.append(os.path.join(os.path.dirname(__file__), "src"))

from predict import CollisionRiskPredictor

app = FastAPI(
    title="SmartV2V Collision Risk ML API",
    description="RandomForest-based real-time V2V collision risk probability estimation service.",
    version="1.0.0"
)

# Global predictor instance
predictor = None

@app.on_event("startup")
def load_model_on_startup():
    global predictor
    model_path = os.path.join(os.path.dirname(__file__), "models", "v2v_collision_model.pkl")
    
    # Auto-train model if file does not exist yet
    if not os.path.exists(model_path):
        from train import train_and_evaluate_model
        train_and_evaluate_model()

    predictor = CollisionRiskPredictor(model_path=model_path)

class PredictRequest(BaseModel):
    distance: float = Field(..., description="Haversine distance in meters")
    relative_speed: float = Field(..., description="Relative speed in km/h or m/s")
    relative_acceleration: float = Field(..., description="Magnitude of relative acceleration")
    heading_difference: float = Field(..., description="Heading difference in degrees (0 to 180)")
    acceleration_magnitude: float = Field(..., description="Accelerometer magnitude m/s^2")
    gyroscope_magnitude: float = Field(..., description="Gyroscope magnitude rad/s")
    ttc: float = Field(..., description="Time to Collision in seconds")
    speed_a: float = Field(..., description="Speed of Vehicle A")
    speed_b: float = Field(..., description="Speed of Vehicle B")

class PredictResponse(BaseModel):
    prediction: int = Field(..., description="0 = Low/No Risk, 1 = Collision Risk")
    risk_probability: float = Field(..., description="Probability of collision risk (0.0 to 1.0)")
    risk_percentage: int = Field(..., description="Risk percentage (0 to 100)")
    risk_level: str = Field(..., description="Risk level category: LOW, MEDIUM, HIGH")

    class Config:
        json_schema_extra = {
            "example": {
                "prediction": 1,
                "risk_probability": 0.91,
                "risk_percentage": 91,
                "risk_level": "HIGH"
            }
        }

@app.get("/")
def health_check():
    return {"status": "online", "service": "SmartV2V ML API", "model_loaded": predictor is not None}

@app.post("/predict")
def predict_collision_risk(payload: PredictRequest):
    if predictor is None:
        raise HTTPException(status_code=500, detail="Model predictor is not initialized.")

    try:
        req_dict = payload.dict()
        result = predictor.predict_risk(req_dict)
        return {
            "prediction": result["prediction"],
            "risk_probability": result["risk_probability"],
            "risk_percentage": result["risk_percentage"],
            "risk_level": result["risk_level"]
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Inference error: {str(e)}")
