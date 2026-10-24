# SmartV2V – Smartphone-Based Vehicle-to-Vehicle Communication and ML-Based Collision Risk Detection System

## Overview
SmartV2V is a low-cost academic safety prototype enabling Vehicle-to-Vehicle (V2V) telemetry collection, real-time feature calculation, machine learning risk estimation, and interactive Android notifications.

## Project Structure
```
SmartV2V/
├── android/
│   └── SmartV2VApp/              # Android application (Kotlin, Jetpack Compose, Material 3)
├── backend/
│   └── smartv2v-server/          # Backend REST API (Kotlin, Ktor framework)
├── ml/                           # ML model, training, and inference API
│   ├── data/                     # Telemetry datasets
│   ├── models/                   # Saved ML models (.pkl)
│   ├── src/                      # Preprocessing, feature engineering, and training scripts
│   ├── app.py                    # FastAPI service for collision risk prediction
│   └── requirements.txt          # Python dependencies
├── database/
│   └── schema.sql                # PostgreSQL database schema (smartv2v_db)
├── stitch_smartnotify_android_ui_design/ # UI/UX Mockups & Specifications
├── README.md                     # Project documentation
└── .gitignore                    # Version control ignore definitions
```

## Technology Stack
- **Mobile**: Kotlin, Jetpack Compose, Material 3, Android SensorManager, Google Play Services Location, Ktor Client / Retrofit.
- **Backend**: Kotlin, Ktor REST API, Kotlin Coroutines, Exposed / JDBC.
- **Database**: PostgreSQL (`smartv2v_db`).
- **Machine Learning**: Python 3.10+, pandas, NumPy, scikit-learn, joblib, FastAPI, Uvicorn.
