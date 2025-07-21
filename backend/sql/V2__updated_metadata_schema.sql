DROP TABLE IF EXISTS user_metadata;
DROP TABLE IF EXISTS website_metadata;
CREATE TABLE website_metadata (
    id SERIAL PRIMARY KEY,
    url TEXT NOT NULL UNIQUE,
    title TEXT,
    description TEXT,
    og_title TEXT,
    og_description TEXT,
    og_image TEXT,
    favicon TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE user_metadata (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    metadata_id INTEGER NOT NULL,
    fetched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (metadata_id) REFERENCES website_metadata(id) ON DELETE CASCADE
);
CREATE INDEX idx_user_metadata_user_id ON user_metadata (user_id);
CREATE INDEX idx_user_metadata_metadata_id ON user_metadata (metadata_id);
CREATE INDEX idx_website_metadata_url ON website_metadata (url);
