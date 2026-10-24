# SmartV2V Cloud Deployment Guide (100% Free - No Credit Card Needed)

This guide provides step-by-step instructions to deploy the entire **SmartV2V** system (PostgreSQL Database, Python FastAPI ML Microservice, and Kotlin Ktor Backend) to free cloud hosting.

---

## Architecture Overview

```
                      +---------------------------------------+
                      |       Android App (Physical Phone)     |
                      |   Runs on 4G/5G/Wi-Fi anywhere        |
                      +-------------------+-------------------+
                                          |
                        HTTPS API Calls   | (https://smartv2v-backend.onrender.com)
                                          v
+-----------------------------------------------------------------------------------+
| RENDER.COM (Free Web Service)                                                     |
|                                                                                   |
|   Kotlin Ktor Backend Server                                                      |
|   * Port: $PORT                                                                   |
|   * Docker: backend/smartv2v-server/Dockerfile                                    |
+--------------------------+---------------------------------+----------------------+
                           |                                 |
              Internal API | (ML_SERVICE_URL)    JDBC SSL    | (DB_URL)
                           v                                 v
+------------------------------------+     +----------------------------------------+
| RENDER.COM (Free Web Service)      |     | NEON.TECH (Free Serverless PostgreSQL) |
|                                    |     |                                        |
|   Python FastAPI ML Microservice   |     |   smartv2v_db                          |
|   * Model: RandomForest            |     |   * Users, Vehicles, Telemetry, Risk   |
|   * Root: ml/                      |     |   * Free forever, 0.5 GB storage       |
+------------------------------------+     +----------------------------------------+
```

---

## Free Platforms Used

| Component | Platform | Free Tier Highlights |
| :--- | :--- | :--- |
| **Database** | [Neon.tech](https://neon.tech) | Free Serverless PostgreSQL, 0.5 GB storage, **No credit card required**. |
| **ML Service** | [Render.com](https://render.com) | Free Web Service (Python), 750 free hours/month, Free HTTPS SSL. |
| **Backend** | [Render.com](https://render.com) | Free Web Service (Docker), multi-stage Gradle build, Free HTTPS SSL. |
| **Mobile App** | Direct APK | [SmartV2V-app-debug.apk](./SmartV2V-app-debug.apk) installed on physical Android device. |

---

## Step 1: Create Free PostgreSQL Database on Neon.tech

1. Go to [https://neon.tech](https://neon.tech) and click **Sign Up** (Sign in with GitHub or Google).
2. Click **Create Project**:
   - **Project Name**: `smartv2v-db`
   - **Database Name**: `smartv2v_db` (or keep default `neondb`)
   - **Region**: Choose closest to you (e.g., *AWS Frankfurt / Singapore / Oregon*).
3. Once created, you will see your **Connection Details**:
   - Copy the connection string. It looks like:
     ```text
     postgresql://neondb_owner:npg_AbCd1234@ep-cool-fog-123456.us-east-2.aws.neon.tech/smartv2v_db?sslmode=require
     ```
4. Note down these 4 values:
   - **Host**: `ep-cool-fog-123456.us-east-2.aws.neon.tech`
   - **Database**: `smartv2v_db`
   - **Username**: `neondb_owner`
   - **Password**: `npg_AbCd1234`
   - **JDBC URL**:
     ```text
     jdbc:postgresql://ep-cool-fog-123456.us-east-2.aws.neon.tech/smartv2v_db?sslmode=require
     ```

*(Note: Neon tables are automatically created on first boot by Ktor's Exposed ORM, so no manual SQL schema import is needed!)*

---

## Step 2: Push Project to GitHub

If you have not pushed your project to GitHub yet, run these commands in PowerShell from the project root:

```powershell
cd "c:\Users\gkaza\Desktop\Final Project"

# Initialize git repository
git init
git add .
git commit -m "Initial commit of SmartV2V with cloud deployment Dockerfiles"

# Create a new repository on GitHub (https://github.com/new) named "smartv2v"
# Then link and push:
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/smartv2v.git
git push -u origin main
```

---

## Step 3: Deploy Python ML Service on Render

1. Log in to [https://render.com](https://render.com) with GitHub.
2. Click **New +** -> **Web Service**.
3. Select your `smartv2v` GitHub repository.
4. Configure the ML Service settings:
   - **Name**: `smartv2v-ml`
   - **Region**: Same region as your database.
   - **Root Directory**: `ml`
   - **Runtime**: `Python 3`
   - **Build Command**: `pip install -r requirements.txt`
   - **Start Command**: `uvicorn app:app --host 0.0.0.0 --port $PORT`
   - **Instance Type**: **Free** ($0/month)
5. Click **Create Web Service**.
6. Wait 1–2 minutes for Render to build. Once complete, copy your public service URL:
   - Example: `https://smartv2v-ml.onrender.com`

---

## Step 4: Deploy Kotlin Ktor Backend on Render

1. On Render dashboard, click **New +** -> **Web Service**.
2. Select your `smartv2v` GitHub repository again.
3. Configure the Backend Service settings:
   - **Name**: `smartv2v-backend`
   - **Region**: Same region as ML service.
   - **Root Directory**: `backend/smartv2v-server`
   - **Runtime**: `Docker` *(Render will automatically detect `backend/smartv2v-server/Dockerfile`)*
   - **Instance Type**: **Free** ($0/month)
4. Scroll down to **Environment Variables** and add the following 5 variables:

| Key | Value (Replace with your actual credentials) |
| :--- | :--- |
| `DB_URL` | `jdbc:postgresql://<NEON_HOST>/smartv2v_db?sslmode=require` |
| `DB_USER` | `<NEON_USERNAME>` |
| `DB_PASSWORD` | `<NEON_PASSWORD>` |
| `ML_SERVICE_URL` | `https://smartv2v-ml.onrender.com/predict` |
| `PORT` | `8080` |

5. Click **Create Web Service**.
6. Render will run the Gradle build and launch the Fat JAR.
7. Once deployed, copy your backend URL:
   - Example: `https://smartv2v-backend.onrender.com`

---

## Step 5: Connect Your Android Phone (From Anywhere!)

Now you can test your smartphone app anywhere in the world on mobile data (4G/5G) or Wi-Fi:

1. Install [SmartV2V-app-debug.apk](./SmartV2V-app-debug.apk) on your Android phone.
2. Open the app:
   - On the **Login** screen, tap the **⚙️ (Settings)** icon in the top right.
   - Enter your public backend URL:
     ```text
     https://smartv2v-backend.onrender.com
     ```
   - Tap **Save Configuration**.
   - Tap the **← Back Arrow** to return to the Login screen.
3. Register or Log in -> Start Telemetry Monitoring.
4. Your phone is now communicating with your cloud-hosted SmartV2V infrastructure!

---

## Bonus: Quick Free Tunnel for Instant Demos (Without GitHub Push)

If you need to demo the app to an examiner or test it on phone data **immediately without waiting for cloud builds**, you can use **Cloudflare Tunnel** (100% free, no account needed):

1. Download `cloudflared.exe` from Cloudflare or run with winget:
   ```powershell
   winget install --id Cloudflare.cloudflared
   ```
2. Start your local backend:
   ```powershell
   cd "c:\Users\gkaza\Desktop\Final Project\backend\smartv2v-server"
   & "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -jar build/libs/smartv2v-server-all.jar
   ```
3. In another terminal, open a public tunnel:
   ```powershell
   cloudflared tunnel --url http://localhost:8080
   ```
4. Cloudflare will give you an instant public HTTPS address (e.g., `https://random-name.trycloudflare.com`). Put that URL into your phone's app settings!
