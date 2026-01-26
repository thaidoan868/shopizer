CREATE TABLE customer_profile(
    id  UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    gender  VARCHAR(10) NOT NULL,
    date_of_birth   DATE,
    language VARCHAR(5) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address VARCHAR(200) NOT NULL,
    avatar_original_url  VARCHAR(300),
    avatar_medium_url    VARCHAR(300),
    avatar_thumbnail_url    VARCHAR(300),

    CONSTRAINT uq_profile_user_id UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_profile_user_id ON customer_profile(user_id);