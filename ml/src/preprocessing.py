import pandas as pd
import numpy as np

def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    """
    Cleans raw telemetry and feature dataset.
    Handles missing values, infinite TTC, and domain boundaries.
    """
    df = df.copy()

    # Replace inf or -inf in TTC with 999.0 (safe distance/no collision risk)
    df["ttc"] = df["ttc"].replace([np.inf, -np.inf], 999.0)
    df["ttc"] = df["ttc"].fillna(999.0)
    df["ttc"] = df["ttc"].apply(lambda x: 999.0 if x < 0 else x)

    # Fill any missing numerical values with median
    num_cols = df.select_dtypes(include=[np.number]).columns
    for col in num_cols:
        if df[col].isnull().sum() > 0:
            df[col] = df[col].fillna(df[col].median())

    return df

def validate_features(df: pd.DataFrame, expected_features: list) -> bool:
    """
    Validates presence of all required feature columns in dataset.
    """
    missing = [f for f in expected_features if f not in df.columns]
    if missing:
        raise ValueError(f"Dataset missing required feature columns: {missing}")
    return True
