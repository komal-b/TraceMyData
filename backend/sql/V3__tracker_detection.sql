DROP TABLE IF EXISTS tracker_detection;
DROP INDEX IF EXISTS idx_tracker_detection_website;
CREATE TABLE tracker_detection (
    id SERIAL PRIMARY KEY,
    metadata_id INTEGER NOT NULL,
    detection_date TIMESTAMP NOT NULL,
    tracker_name VARCHAR(255) NOT NULL,
    risk_level VARCHAR(50) NOT NULL, -- e.g., High, Medium, Low
    request_urls TEXT, -- JSON array as string
    script_snippets TEXT, -- optional, can be null
    FOREIGN KEY (metadata_id) REFERENCES website_metadata(id) ON DELETE CASCADE
);


CREATE INDEX idx_tracker_detection_website ON tracker_detection(metadata_id);
