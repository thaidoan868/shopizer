CREATE TABLE users (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    -- User fields
    realm            VARCHAR(255) NOT NULL DEFAULT 'shopizer',
    keycloak_user_id UUID         NOT NULL,
    username         VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    first_name       VARCHAR(255) NOT NULL,
    last_name        VARCHAR(255) NOT NULL,
    avatar_meta      JSONB
);

CREATE UNIQUE INDEX idx_users_keycloak_user_id ON users (keycloak_user_id);
CREATE UNIQUE INDEX idx_users_username ON users (username);
CREATE UNIQUE INDEX idx_users_email ON users (email);

CREATE TABLE customer_profiles (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    -- CustomerProfile fields
    user_id          UUID NOT NULL,
    language         VARCHAR(50) NOT NULL DEFAULT 'en',
    phone_number     VARCHAR(50),
    address          VARCHAR(255),
    date_of_birth    DATE,
    gender           VARCHAR(20),

    CONSTRAINT fk_customer_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_customer_profiles_user_id
    ON customer_profiles (user_id)
    WHERE user_id IS NOT NULL;


CREATE TABLE employee_profiles (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    user_id          UUID NOT NULL,
    work_phone       VARCHAR(50),
    shift            VARCHAR(50),


    CONSTRAINT fk_employee_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_employee_profiles_user_id
    ON employee_profiles (user_id);