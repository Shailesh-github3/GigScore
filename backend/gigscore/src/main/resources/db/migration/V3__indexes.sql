-- Composite index for fast user activity lookups ordered by timestamp
CREATE INDEX idx_activity_user_timestamp ON activity (user_id, timestamp);

-- Composite index for fast score history lookups ordered by recorded_at
CREATE INDEX idx_score_history_user_recorded ON score_history (user_id, recorded_at);
