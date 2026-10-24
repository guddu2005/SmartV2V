import os
import joblib
import pandas as pd
from feature_engineering import extract_v2v_features
from preprocessing import clean_data

class CollisionRiskPredictor:
    def __init__(self, model_path: str = None):
        if model_path is None:
            model_path = os.path.join(os.path.dirname(__file__), "..", "models", "v2v_collision_model.pkl")
        
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"Model file not found at {model_path}. Run train.py first.")
        
        self.model = joblib.load(model_path)
        self.feature_columns = [
            "distance",
            "relative_speed",
            "relative_acceleration",
            "heading_difference",
            "acceleration_magnitude",
            "gyroscope_magnitude",
            "ttc",
            "speed_a",
            "speed_b"
        ]

    def predict_risk(self, feature_dict: dict) -> dict:
        """
        Predicts collision risk probability and risk level from a feature dictionary.
        """
        df = pd.DataFrame([feature_dict])
        df = clean_data(df)
        X = df[self.feature_columns]

        proba = float(self.model.predict_proba(X)[0][1])
        prediction = 1 if proba >= 0.50 else 0
        risk_pct = int(round(proba * 100))

        # Risk level determination based on experimental threshold ranges
        if proba >= 0.70:
            risk_level = "HIGH"
        elif proba >= 0.30:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"

        return {
            "prediction": prediction,
            "risk_probability": round(proba, 4),
            "risk_percentage": risk_pct,
            "risk_level": risk_level
        }

if __name__ == "__main__":
    predictor = CollisionRiskPredictor()
    sample_features = {
        "distance": 18.5,
        "relative_speed": 8.2,
        "relative_acceleration": 2.5,
        "heading_difference": 3.2,
        "acceleration_magnitude": 12.8,
        "gyroscope_magnitude": 0.7,
        "ttc": 2.25,
        "speed_a": 65.0,
        "speed_b": 35.0
    }
    result = predictor.predict_risk(sample_features)
    print("Sample Risk Prediction:", result)
