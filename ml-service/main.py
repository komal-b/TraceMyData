# main.py
import keyword
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import joblib
import logging
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from starlette.requests import Request
import os
from train_model import train_model
import json
import re
import math


# ----- Logging Setup -----
logger = logging.getLogger("ml-risk-api")
logger.setLevel(logging.INFO)
if not logger.handlers:
    handler = logging.StreamHandler()
    formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
    handler.setFormatter(formatter)
    handler.setLevel(logging.INFO)
    logger.addHandler(handler)

MODEL_PATH = "model/model.pkl"


# Train if model not found
if not os.path.exists(MODEL_PATH):
    logger.warning("⚠️ Model file not found. Training model...")
    train_model()

with open("data/metadata_keywords.json") as f:
    keyword_categories = json.load(f)

with open("data/domain_summary.json", "r") as f:
    domain_summary = json.load(f)
# ----- Load Model and Label Encoder -----
try:
    model, label_encoder = joblib.load(MODEL_PATH)
    logger.info(f"✅ Model loaded. Classes: {label_encoder.classes_}")
except Exception as e:
    logger.error(f"❌ Failed to load model: {e}")
    raise

# ----- FastAPI Setup -----
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ----- Pydantic Models -----
class WebsiteFeatures(BaseModel):
    url: str
    numTrackers: int
    trackerRisk: float
    metadataRisk: int
    externalOgImage: bool
    finalScore: float

class RiskPrediction(BaseModel):
    url: str
    risk_score: float
    risk_label: str 


# ----- Exception Handler -----
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    logger.error(f"Validation error for request: {request.url}")
    logger.error(f"Details: {exc.errors()}")
    return JSONResponse(
        status_code=422,
        content={"detail": exc.errors()},
    )

def extract_tags_from_summary(url: str) -> List[str]:
    matched_tags = []
    try:
        # Normalize input URL
        normalized_url = url.lower().strip().replace("https://", "").replace("http://", "").rstrip("/")

        # Search in domain summary
        domain_data = next(
            (item for item in domain_summary
             if isinstance(item, dict) and
             item.get("url") and
             item.get("url").lower().strip().replace("https://", "").replace("http://", "").rstrip("/") == normalized_url),
            None
        )

        if not domain_data:
            logger.warning(f"❌ No domain summary found for {url}")
            return []

        combined_text = f"{domain_data.get('title', '')} {domain_data.get('description', '')}".lower()

        for tag, keywords in keyword.items():
            for keyword_str in keywords:
                if re.search(rf"\b{re.escape(keyword_str)}\b", combined_text):
                    matched_tags.append(tag)
                    break

    except Exception as e:
        logger.error(f"⚠️ Error while extracting tags for {url}: {e}")

    return matched_tags


    return matched_tags
def calculate_risk_percent(score: float, label: str) -> float:
    if label == "Low":
        # Scale between 0–25%
        scaled = min(score / 500, 1.0)
        return round(scaled * 25, 2)

    elif label == "Medium":
        # Score should be between 500–1500 → scale to 25–60%
        scaled = min(max((score - 500) / 1000, 0.0), 1.0)
        return round(25 + scaled * 35, 2)

    elif label == "High":
        # Score should be between 1500–3500 → scale to 60–100%
        scaled = min(max((score - 1500) / 2000, 0.0), 1.0)
        return round(60 + scaled * 40, 2)

    return 0.0

def predict(features: WebsiteFeatures) -> RiskPrediction:
    try:
        X = [[
            features.numTrackers,
            features.trackerRisk,
            features.metadataRisk,
            int(features.externalOgImage),
            features.finalScore
        ]]
        logger.info(f"🔍 Predicting for: {features.url} | Features: {X}")

        pred = model.predict(X)[0]
        
        label = label_encoder.inverse_transform([pred])[0]

        percentage_score = calculate_risk_percent(features.finalScore, label)

        tags = extract_tags_from_summary(features.url)

        return RiskPrediction(
            url=features.url,
            risk_score=percentage_score,
            risk_label=label
        )

    except Exception as e:
        logger.error(f"❌ Prediction error: {e}")
        raise HTTPException(status_code=500, detail="Prediction failed")

# ----- API Endpoint -----
@app.post("/predict-risk/batch", response_model=List[RiskPrediction])
def predict_batch(inputs: List[WebsiteFeatures]):
    logger.info(f"📦 Received {len(inputs)} websites for risk prediction")
    results = [predict(feature) for feature in inputs]
    logger.info("✅ Batch prediction complete.")
    return results
