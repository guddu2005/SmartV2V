import os
import math
import joblib
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, confusion_matrix, roc_auc_score, accuracy_score, recall_score, precision_score, f1_score

from preprocessing import clean_data, validate_features

FEATURE_COLUMNS = [
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

def generate_synthetic_v2v_dataset(num_samples: int = 5000, seed: int = 42) -> pd.DataFrame:
    """
    Generates a physically grounded V2V collision risk dataset with realistic sensor noise and scenarios.
    """
    np.random.seed(seed)
    data = []

    for _ in range(num_samples):
        # Scenario types: 0 = normal cruising, 1 = tailgating/sudden braking, 2 = head-on approach, 3 = intersection angle
        scenario = np.random.choice([0, 1, 2, 3], p=[0.45, 0.25, 0.15, 0.15])

        if scenario == 0:  # Safe cruising
            distance = np.random.uniform(40.0, 200.0)
            speed_a = np.random.uniform(30.0, 90.0)
            speed_b = np.random.uniform(30.0, 90.0)
            heading_diff = np.random.uniform(0.0, 15.0)
            accel_mag = np.random.uniform(9.6, 10.2)  # Normal gravity ~9.8 m/s^2
            gyro_mag = np.random.uniform(0.0, 0.2)
        elif scenario == 1:  # Rear-end risk (tailgating + high speed difference)
            distance = np.random.uniform(5.0, 30.0)
            speed_a = np.random.uniform(60.0, 110.0)
            speed_b = np.random.uniform(20.0, 60.0)
            heading_diff = np.random.uniform(0.0, 25.0)
            accel_mag = np.random.uniform(11.0, 16.0)  # Hard braking
            gyro_mag = np.random.uniform(0.1, 0.8)
        elif scenario == 2:  # Head-on collision risk
            distance = np.random.uniform(10.0, 50.0)
            speed_a = np.random.uniform(40.0, 90.0)
            speed_b = np.random.uniform(40.0, 90.0)
            heading_diff = np.random.uniform(160.0, 180.0)
            accel_mag = np.random.uniform(9.8, 14.0)
            gyro_mag = np.random.uniform(0.1, 1.2)
        else:  # Intersection conflict angle risk
            distance = np.random.uniform(8.0, 35.0)
            speed_a = np.random.uniform(30.0, 70.0)
            speed_b = np.random.uniform(30.0, 70.0)
            heading_diff = np.random.uniform(70.0, 110.0)
            accel_mag = np.random.uniform(10.0, 15.0)
            gyro_mag = np.random.uniform(0.3, 1.5)

        rel_speed = abs(speed_a - speed_b)
        rel_accel = np.random.uniform(0.1, 5.0) if scenario != 0 else np.random.uniform(0.0, 1.0)

        # Calculate TTC
        speed_a_ms = speed_a / 3.6
        speed_b_ms = speed_b / 3.6
        rad_diff = math.radians(heading_diff)
        closing_speed = abs(speed_a_ms - speed_b_ms * math.cos(rad_diff))

        if closing_speed > 0.05 and distance > 0:
            ttc = distance / closing_speed
        else:
            ttc = 999.0

        # Ground truth label logic (1 = collision risk, 0 = low risk)
        # Collision risk if TTC <= 4.0 seconds OR (distance < 15m and closing_speed > 5 m/s)
        is_risk = 1 if (ttc <= 4.0 or (distance < 15.0 and rel_speed > 15.0)) else 0

        data.append({
            "distance": round(distance, 2),
            "relative_speed": round(rel_speed, 2),
            "relative_acceleration": round(rel_accel, 2),
            "heading_difference": round(heading_diff, 2),
            "acceleration_magnitude": round(accel_mag, 2),
            "gyroscope_magnitude": round(gyro_mag, 2),
            "ttc": round(ttc, 2),
            "speed_a": round(speed_a, 2),
            "speed_b": round(speed_b, 2),
            "risk_label": is_risk
        })

    return pd.DataFrame(data)

def train_and_evaluate_model():
    dataset_path = os.path.join(os.path.dirname(__file__), "..", "data", "v2v_dataset.csv")
    model_path = os.path.join(os.path.dirname(__file__), "..", "models", "v2v_collision_model.pkl")

    os.makedirs(os.path.dirname(dataset_path), exist_ok=True)
    os.makedirs(os.path.dirname(model_path), exist_ok=True)

    if not os.path.exists(dataset_path):
        print(f"Dataset not found at {dataset_path}. Generating synthetic baseline dataset...")
        df = generate_synthetic_v2v_dataset(num_samples=5000)
        df.to_csv(dataset_path, index=False)
        print(f"Saved dataset to {dataset_path}")
    else:
        print(f"Loading dataset from {dataset_path}...")
        df = pd.read_csv(dataset_path)

    # Clean & validate data
    df = clean_data(df)
    validate_features(df, FEATURE_COLUMNS)

    X = df[FEATURE_COLUMNS]
    y = df["risk_label"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    # Train Random Forest Classifier with class weighting to prioritize risk class recall
    clf = RandomForestClassifier(
        n_estimators=150,
        max_depth=12,
        class_weight="balanced",
        random_state=42,
        n_jobs=-1
    )
    clf.fit(X_train, y_train)

    # Predictions & probabilities
    y_pred = clf.predict(X_test)
    y_proba = clf.predict_proba(X_test)[:, 1]

    # Metrics
    acc = accuracy_score(y_test, y_pred)
    prec = precision_score(y_test, y_pred)
    rec = recall_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)
    roc_auc = roc_auc_score(y_test, y_proba)
    cm = confusion_matrix(y_test, y_pred)

    print("\n================ MODEL EVALUATION METRICS ================")
    print(f"Accuracy : {acc * 100:.2f}%")
    print(f"Precision: {prec * 100:.2f}%")
    print(f"Recall   : {rec * 100:.2f}%  <-- Priority metric for safety")
    print(f"F1-Score : {f1 * 100:.2f}%")
    print(f"ROC-AUC  : {roc_auc:.4f}")
    print("\nConfusion Matrix:\n", cm)
    print("\nClassification Report:\n", classification_report(y_test, y_pred))

    # Feature Importance
    print("\nFeature Importances:")
    importances = clf.feature_importances_
    for feat, imp in zip(FEATURE_COLUMNS, importances):
        print(f"  - {feat:25s}: {imp:.4f}")

    # Save model artifact
    joblib.dump(clf, model_path)
    print(f"\nModel successfully saved to {os.path.abspath(model_path)}")

if __name__ == "__main__":
    train_and_evaluate_model()
