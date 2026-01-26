CREATE TABLE customer_profile(
    id  UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    gender  VARCHAR(10) NOT NULL,
    date_of_birth   DATE,
    language VARCHAR(5) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address VARCHAR(200) NOT NULL,
    avatar_original_url  VARCHAR(300),
    avatar_medium_url    VARCHAR(300),
    avatar_thumbnail_url    VARCHAR(300)
);