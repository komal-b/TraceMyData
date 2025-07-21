import psycopg2
import json
import csv
import os
import re
from typing import Dict, Any, List
from dotenv import load_dotenv

# --- Load Environment Variables ---
load_dotenv()

DB_CONFIG = {
    "dbname": os.getenv("POSTGRES_DB"),
    "user": os.getenv("POSTGRES_USER"),
    "password": os.getenv("POSTGRES_PASSWORD"),
    "host": os.getenv("POSTGRES_HOST", "db"),  # Docker hostname
    "port": os.getenv("POSTGRES_PORT", 5432)
}

TABLE_METADATA = "website_metadata"
TABLE_TRACKERS = "tracker_detection"
OUTPUT_CSV = "output/privacy_dataset.csv"
TRACKER_RISK_FILE = "data/domain_summary.json"
KEYWORD_FILE = "data/metadata_keywords.json"

# --- Load Metadata Keywords from JSON ---
def load_metadata_keywords(path: str) -> Dict[str, List[str]]:
    with open(path, "r") as file:
        return json.load(file)

# --- Load Tracker Risk Scores ---
def load_tracker_scores(path: str) -> Dict[str, float]:
    with open(path, "r") as file:
        data = json.load(file)
    scores = {
        domain: 40 * details.get("fp", 0) +
                30 * details.get("cookies", 0) +
                30 * details.get("prevalence", 0)
        for domain, details in data.items()
    }
    return scores

# --- Extract domains from request URLs ---
def extract_domains_from_urls(urls_str: str) -> List[str]:
    urls = re.findall(r'https?://([^/\s]+)', urls_str)
    return list(set(urls))

# --- Compute Metadata Risk ---
def compute_metadata_risk(scan: Dict[str, Any], keyword_categories: Dict[str, List[str]]) -> int:
    combined_text = f"{scan.get('description', '')} {scan.get('og_title', '')} {scan.get('og_description', '')}".lower()
    score = 0
    for category, keywords in keyword_categories.items():
        if any(keyword.lower() in combined_text for keyword in keywords):
            score += 10  # You can assign different weights per category if needed
    return score

# --- Process Individual Scan Entry ---
def process_scan(scan: Dict[str, Any], tracker_scores: Dict[str, float], keyword_categories: Dict[str, List[str]]) -> Dict[str, Any]:
    detected = scan.get("detected_trackers", [])
    tracker_risk = sum(tracker_scores.get(t, 0) for t in detected)
    num_trackers = len(detected)

    metadata_risk = compute_metadata_risk(scan, keyword_categories)

    if scan.get("og_image"):
        tracker_risk += 5

    final_score = tracker_risk + metadata_risk

    if final_score < 30:
        label = "Low"
    elif final_score < 70:
        label = "Medium"
    else:
        label = "High"

    return {
        "url": scan.get("url"),
        "numTrackers": num_trackers,
        "trackerRisk": round(tracker_risk, 2),
        "metadataRisk": metadata_risk,
        "externalOgImage": bool(scan.get("og_image")),
        "finalScore": round(final_score, 2),
        "privacyLabel": label
    }

# --- Save Dataset as CSV ---
def save_dataset(rows: List[Dict[str, Any]], output_path: str):
    if not rows:
        return
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=rows[0].keys())
        writer.writeheader()
        writer.writerows(rows)

# --- Main Build Function ---
def build_dataset():
    tracker_scores = load_tracker_scores(TRACKER_RISK_FILE)
    keyword_categories = load_metadata_keywords(KEYWORD_FILE)
    dataset = []
    conn = None
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()

        cursor.execute(f"""
            SELECT id, url, description, og_title, og_description, og_image
            FROM {TABLE_METADATA}
        """)
        metadata_rows = cursor.fetchall()

        for row in metadata_rows:
            metadata_id = row[0]
            scan = {
                "url": row[1],
                "description": row[2],
                "og_title": row[3],
                "og_description": row[4],
                "og_image": row[5]
            }

            cursor.execute(f"""
                SELECT request_urls FROM {TABLE_TRACKERS} WHERE metadata_id = %s
            """, (metadata_id,))
            tracker_rows = cursor.fetchall()
            raw_urls_list = []
            for r in tracker_rows:
                try:
                    urls = json.loads(r[0])  # assuming r[0] is a JSON string like '["url1", "url2"]'
                    if isinstance(urls, list):
                        raw_urls_list.extend(urls)
                except Exception as e:
                    print(f"⚠️ Failed to parse request_urls JSON: {e}")
                    continue

            raw_urls = " ".join(raw_urls_list)
            scan["detected_trackers"] = extract_domains_from_urls(raw_urls)

            processed = process_scan(scan, tracker_scores, keyword_categories)
            dataset.append(processed)

        save_dataset(dataset, OUTPUT_CSV)
        print(f"✅ Dataset saved to {OUTPUT_CSV} with {len(dataset)} entries.")

    except Exception as e:
        print(f"❌ Database error: {e}")
    finally:
        if conn:
            cursor.close()
            conn.close()

# --- Entry Point ---
if __name__ == "__main__":
    build_dataset()
