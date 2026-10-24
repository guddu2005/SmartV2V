import time
import math
import sys
import requests

BASE_URL = "http://localhost:8080"

# Driver 2 Account Details for Laptop Vehicle
USER_EMAIL = "laptop_driver@smartv2v.com"
USER_PASSWORD = "Password123!"
USER_NAME = "Laptop Driver"
VEHICLE_NUMBER = "LAPTOP-V2"
DEVICE_ID = "laptop_simulation_device_02"

class VehicleSimulator:
    def __init__(self, base_url=BASE_URL):
        self.base_url = base_url
        self.token = None
        self.user_id = None
        self.vehicle_id = None

    def authenticate(self):
        print(f"[+] Authenticating Vehicle 2 on Laptop ({self.base_url})...")
        # Attempt Login
        login_url = f"{self.base_url}/api/auth/login"
        try:
            resp = requests.post(login_url, json={"email": USER_EMAIL, "password": USER_PASSWORD})
            if resp.status_code == 200 and resp.json().get("success"):
                data = resp.json()["data"]
                self.token = data["token"]
                self.user_id = data["user"]["id"]
                print(f"    -> Login successful as User ID: {self.user_id}")
            else:
                # Register if login fails
                print("    -> Account not found, registering new laptop driver...")
                reg_url = f"{self.base_url}/api/auth/register"
                reg_resp = requests.post(reg_url, json={
                    "name": USER_NAME,
                    "email": USER_EMAIL,
                    "password": USER_PASSWORD
                })
                if reg_resp.status_code == 201 and reg_resp.json().get("success"):
                    data = reg_resp.json()["data"]
                    self.token = data["token"]
                    self.user_id = data["user"]["id"]
                    print(f"    -> Registered successfully as User ID: {self.user_id}")
                else:
                    print(f"[-] Registration failed: {reg_resp.text}")
                    sys.exit(1)
        except requests.exceptions.ConnectionError:
            print(f"[-] ERROR: Cannot connect to Ktor backend at {self.base_url}.")
            print("    Please ensure your backend Fat JAR is running!")
            sys.exit(1)

        self.ensure_vehicle()

    def ensure_vehicle(self):
        headers = {"Authorization": f"Bearer {self.token}"}
        # Check existing vehicles
        my_vehicles_url = f"{self.base_url}/api/vehicles/my"
        resp = requests.get(my_vehicles_url, headers=headers)
        if resp.status_code == 200 and resp.json().get("success"):
            vehicles = resp.json().get("data", [])
            for v in vehicles:
                if v["vehicleNumber"] == VEHICLE_NUMBER:
                    self.vehicle_id = v["id"]
                    print(f"[+] Found existing Vehicle 2 (ID: {self.vehicle_id}, Number: {VEHICLE_NUMBER})")
                    return

        # Create Vehicle 2
        print("[+] Creating Vehicle 2...")
        create_url = f"{self.base_url}/api/vehicles"
        create_resp = requests.post(create_url, headers=headers, json={
            "vehicleNumber": VEHICLE_NUMBER,
            "deviceId": DEVICE_ID
        })
        if create_resp.status_code == 201 and create_resp.json().get("success"):
            self.vehicle_id = create_resp.json()["data"]["id"]
            print(f"[+] Vehicle 2 created successfully (ID: {self.vehicle_id})")
        else:
            print(f"[-] Failed to create vehicle: {create_resp.text}")
            sys.exit(1)

    def get_vehicle_1_latest(self):
        headers = {"Authorization": f"Bearer {self.token}"}
        url = f"{self.base_url}/api/telemetry/vehicle/1/latest"
        try:
            resp = requests.get(url, headers=headers, timeout=2)
            if resp.status_code == 200 and resp.json().get("success"):
                return resp.json().get("data")
        except Exception:
            pass
        return None

    def send_telemetry(self, lat, lon, speed, heading, ax=0.0, ay=0.0, az=9.8, gx=0.0, gy=0.0, gz=0.0):
        headers = {"Authorization": f"Bearer {self.token}"}
        url = f"{self.base_url}/api/telemetry/vehicle/{self.vehicle_id}"
        payload = {
            "latitude": lat,
            "longitude": lon,
            "speed": speed,
            "heading": heading,
            "accelerationX": ax,
            "accelerationY": ay,
            "accelerationZ": az,
            "gyroscopeX": gx,
            "gyroscopeY": gy,
            "gyroscopeZ": gz
        }
        try:
            resp = requests.post(url, headers=headers, json=payload, timeout=2)
            return resp.status_code == 201
        except Exception as e:
            print(f"[-] Transmission error: {e}")
            return False

    def run_simulation(self, mode):
        print("\n" + "=" * 60)
        print(f"[ STARTING VEHICLE 2 SIMULATION - MODE {mode} ]")
        print("    Press CTRL+C to stop simulation at any time.")
        print("=" * 60 + "\n")

        # Initial fallback coordinates (Hyderabad / Default)
        start_lat = 17.38504
        start_lon = 78.48667
        heading = 90.0
        step = 0

        while True:
            step += 1
            v1_data = self.get_vehicle_1_latest()

            if mode == 1:
                # Mode 1: Auto-Sync / Follow Vehicle 1 (Phone)
                if v1_data:
                    ref_lat = v1_data["latitude"]
                    ref_lon = v1_data["longitude"]
                    ref_speed = v1_data["speed"]
                    ref_heading = v1_data["heading"]

                    # Position Vehicle 2 ~25 meters behind Vehicle 1, closing in slightly
                    offset = (30.0 - (step % 15)) / 111000.0  # meters to lat conversion
                    lat = ref_lat - offset
                    lon = ref_lon
                    speed = max(ref_speed + 5.0, 15.0)  # slightly faster closing speed
                    heading = ref_heading
                    status_text = f"Following Phone (V1: lat={ref_lat:.5f}, lon={ref_lon:.5f})"
                else:
                    # Fallback moving pattern
                    lat = start_lat + (step * 0.00005)
                    lon = start_lon + (step * 0.00005)
                    speed = 18.0
                    heading = 45.0
                    status_text = "Phone V1 offline - using simulated track"

            elif mode == 2:
                # Mode 2: High Risk (Closing rapidly)
                if v1_data:
                    lat = v1_data["latitude"] + 0.00015 - (step * 0.000015) # ~15m away closing fast
                    lon = v1_data["longitude"]
                else:
                    lat = start_lat + 0.0001 - (step * 0.00001)
                    lon = start_lon
                speed = 28.5  # High speed
                heading = 180.0
                ax = -2.5     # Hard braking / rapid deceleration
                status_text = "[HIGH COLLISION RISK SCENARIO]"

            elif mode == 3:
                # Mode 3: Medium Risk (Moderate distance ~45m)
                if v1_data:
                    lat = v1_data["latitude"] + 0.00045
                    lon = v1_data["longitude"] + 0.0002
                else:
                    lat = start_lat + 0.00045
                    lon = start_lon + 0.0002
                speed = 14.0
                heading = 90.0
                status_text = "[MEDIUM RISK SCENARIO (~45m distance)]"

            elif mode == 4:
                # Mode 4: Safe / Low Risk (~150m away)
                if v1_data:
                    lat = v1_data["latitude"] + 0.0015
                    lon = v1_data["longitude"] + 0.0015
                else:
                    lat = start_lat + 0.0015
                    lon = start_lon + 0.0015
                speed = 10.0
                heading = 90.0
                status_text = "[SAFE / LOW RISK SCENARIO (~150m distance)]"

            ok = self.send_telemetry(lat, lon, speed, heading)
            symbol = "OK" if ok else "ERR"
            print(f"[{symbol}] Sec {step:03d} | V2 (ID: {self.vehicle_id}) -> Lat: {lat:.6f}, Lon: {lon:.6f}, Speed: {speed:.1f} km/h | {status_text}", flush=True)

            time.sleep(1.0)

def main():
    print("=" * 60)
    print("  SmartV2V - Vehicle 2 (Laptop) Telemetry Simulator")
    print("=" * 60)

    if len(sys.argv) > 1 and sys.argv[1].isdigit():
        mode = int(sys.argv[1])
    else:
        print("Select Simulation Mode:")
        print(" 1) Auto-Sync & Intercept Vehicle 1 (Follow Physical Phone)")
        print(" 2) Simulate HIGH COLLISION RISK (Closing Fast)")
        print(" 3) Simulate MEDIUM RISK (~45m distance)")
        print(" 4) Simulate SAFE / LOW RISK (~150m distance)")
        choice = input("\nEnter mode (1-4) [default: 1]: ").strip()
        mode = int(choice) if choice in ["1", "2", "3", "4"] else 1

    sim = VehicleSimulator()
    sim.authenticate()
    try:
        sim.run_simulation(mode)
    except KeyboardInterrupt:
        print("\n[!] Simulation stopped by user.")

if __name__ == "__main__":
    main()
