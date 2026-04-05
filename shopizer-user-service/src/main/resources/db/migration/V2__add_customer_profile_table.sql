CREATE TABLE customer_profile(
    id  UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    username VARCHAR(200) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL,
    realm VARCHAR(30) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    gender  VARCHAR(10),
    date_of_birth   DATE,
    language VARCHAR(5),
    phone_number VARCHAR(20),
    address VARCHAR(300),
    avatar_meta JSONB,

    CONSTRAINT uq_profile_user_id UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_profile_user_id ON customer_profile(user_id);