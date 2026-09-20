CREATE TABLE user (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    password VARCHAR(255),
    role ENUM ('LENDER','WORKER') NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_user_email UNIQUE (email)
);

CREATE TABLE gig_data (
    gig_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    platform VARCHAR(255),
    total_earnings DECIMAL(12,2),
    jobs_completed INT,
    avg_rating DOUBLE,
    PRIMARY KEY (gig_id),
    CONSTRAINT fk_gig_data_user FOREIGN KEY (user_id) REFERENCES user (user_id)
);

CREATE TABLE gig_score (
    gig_score_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    score DOUBLE,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    PRIMARY KEY (gig_score_id),
    CONSTRAINT uk_gig_score_user UNIQUE (user_id),
    CONSTRAINT fk_gig_score_user FOREIGN KEY (user_id) REFERENCES user (user_id)
);

CREATE TABLE score_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    score DOUBLE,
    score_version INT,
    recorded_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_score_history_user FOREIGN KEY (user_id) REFERENCES user (user_id)
);

CREATE TABLE activity (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    action VARCHAR(255),
    platform VARCHAR(255),
    amount DECIMAL(12,2),
    rating DOUBLE,
    `timestamp` TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES user (user_id)
);
