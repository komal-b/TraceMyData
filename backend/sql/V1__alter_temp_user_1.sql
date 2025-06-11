ALTER TABLE temp_user
ADD COLUMN user_id UUID;

-- Add foreign key constraint to temp_user table
-- Ensure that the user_id column references the id column in the users table
-- and that it cascades on delete

ALTER TABLE temp_user
ADD CONSTRAINT fk_temp_user
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;