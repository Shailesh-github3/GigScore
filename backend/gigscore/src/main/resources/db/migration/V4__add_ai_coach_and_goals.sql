CREATE TABLE user_goal (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    target_score INT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_user_goal_user FOREIGN KEY (user_id) REFERENCES user (user_id)
);

CREATE INDEX idx_user_goal_user ON user_goal (user_id, status);

CREATE TABLE ai_interaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_path VARCHAR(255),
    response TEXT,
    created_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_interaction_user FOREIGN KEY (user_id) REFERENCES user (user_id)
);

CREATE INDEX idx_ai_interaction_user ON ai_interaction (user_id, created_at);
