# SmartV2V Machine Learning Service

This service provides real-time Vehicle-to-Vehicle (V2V) collision risk estimation using a Random Forest Classifier hosted on a FastAPI web service.

## Architecture
```
ml/
├── data/
│   └── v2v_dataset.csv       # Telemetry dataset
├── models/
│   └── v2v_collision_model.pkl # Trained Random Forest model
├── src/
│   ├── preprocessing.py      # Cleaning and missing value handling
│   ├── feature_engineering.py# V2V kinematic feature extraction
│   ├── train.py              # Model training script
│   └── predict.py            # Model inference class
├── app.py                    # FastAPI application
├── requirements.txt          # Dependencies
└── README.md
```

## Features Used
1. `distance` (meters)
2. `relative_speed` (km/h)
3. `relative_acceleration` (m/s²)
4. `heading_difference` (degrees, 0–180)
5. `acceleration_magnitude` (m/s²)
6. `gyroscope_magnitude` (rad/s)
7. `ttc` (Time-to-Collision in seconds)
8. `speed_a` (km/h)
9. `speed_b` (km/h)

## Risk Classification Thresholds
- **LOW**: 0.00 – 0.29 risk probability
- **MEDIUM**: 0.30 – 0.69 risk probability
- **HIGH**: 0.70 – 1.00 risk probability

## Running the Service
```bash
cd ml
pip install -r requirements.txt
python src/train.py
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```
