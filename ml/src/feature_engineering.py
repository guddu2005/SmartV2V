import math
import numpy as np
import pandas as pd

def compute_haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """
    Computes Haversine distance in meters between two lat/lon coordinates.
    """
    R = 6371000.0  # Earth radius in meters
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = (math.sin(dlat / 2.0) ** 2 +
         math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) *
         math.sin(dlon / 2.0) ** 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c

def compute_heading_difference(h1: float, h2: float) -> float:
    """
    Computes absolute angular difference between two vehicle headings (0 to 180 degrees).
    """
    diff = abs(h1 - h2) % 360.0
    return 360.0 - diff if diff > 180.0 else diff

def compute_vector_magnitude(x: float, y: float, z: float) -> float:
    """
    Computes 3D vector magnitude for accelerometer or gyroscope.
    """
    return math.sqrt(x * x + y * y + z * z)

def compute_ttc(distance: float, speed_a_kmh: float, speed_b_kmh: float, heading_diff: float) -> float:
    """
    Computes Time to Collision (TTC) in seconds.
    TTC = distance / closing_speed
    Returns 999.0 for infinite / safe scenarios.
    """
    speed_a_ms = speed_a_kmh / 3.6
    speed_b_ms = speed_b_kmh / 3.6
    rad_diff = math.radians(heading_diff)
    
    closing_speed = abs(speed_a_ms - speed_b_ms * math.cos(rad_diff))
    if closing_speed <= 0.01 or distance <= 0.0:
        return 999.0
    
    ttc = distance / closing_speed
    return 999.0 if math.isinf(ttc) or math.isnan(ttc) or ttc > 999.0 else max(0.0, ttc)

def extract_v2v_features(telem_a: dict, telem_b: dict) -> dict:
    """
    Extracts complete set of 9 V2V kinematic features required by the Random Forest model.
    """
    distance = compute_haversine_distance(
        telem_a["latitude"], telem_a["longitude"],
        telem_b["latitude"], telem_b["longitude"]
    )
    rel_speed = abs(telem_a["speed"] - telem_b["speed"])
    accel_mag_a = compute_vector_magnitude(telem_a["acceleration_x"], telem_a["acceleration_y"], telem_a["acceleration_z"])
    accel_mag_b = compute_vector_magnitude(telem_b["acceleration_x"], telem_b["acceleration_y"], telem_b["acceleration_z"])
    rel_accel = abs(accel_mag_a - accel_mag_b)
    heading_diff = compute_heading_difference(telem_a["heading"], telem_b["heading"])
    gyro_mag_a = compute_vector_magnitude(telem_a["gyroscope_x"], telem_a["gyroscope_y"], telem_a["gyroscope_z"])
    gyro_mag_b = compute_vector_magnitude(telem_b["gyroscope_x"], telem_b["gyroscope_y"], telem_b["gyroscope_z"])
    combined_gyro_mag = (gyro_mag_a + gyro_mag_b) / 2.0
    ttc = compute_ttc(distance, telem_a["speed"], telem_b["speed"], heading_diff)

    return {
        "distance": distance,
        "relative_speed": rel_speed,
        "relative_acceleration": rel_accel,
        "heading_difference": heading_diff,
        "acceleration_magnitude": accel_mag_a,
        "gyroscope_magnitude": combined_gyro_mag,
        "ttc": ttc,
        "speed_a": telem_a["speed"],
        "speed_b": telem_b["speed"]
    }
