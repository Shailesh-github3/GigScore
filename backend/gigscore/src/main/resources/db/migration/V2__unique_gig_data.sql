ALTER TABLE gig_data ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE gig_data ADD CONSTRAINT uk_gig_data_user_platform UNIQUE (user_id, platform);
