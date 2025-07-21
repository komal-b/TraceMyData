DROP TABLE IF EXISTS privacy_risk_score;
DROP INDEX IF EXISTS idx_privacy_risk_score;
CREATE TABLE privacy_risk_score (
    id SERIAL PRIMARY KEY,
    metadata_id INTEGER NOT NULL,
    detection_date TIMESTAMP NOT NULL,
    risk_score FLOAT NOT NULL, -- e.g., 0.0 to 1.0
    risk_label VARCHAR(50) NOT NULL, -- e.g., High, Medium, Low
    risk_tags TEXT, -- JSON array as string
    FOREIGN KEY (metadata_id) REFERENCES website_metadata(id) ON DELETE CASCADE
);

CREATE INDEX idx_privacy_risk_score ON privacy_risk_score(metadata_id);
