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

CREATE TABLE customer_profile (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    -- CustomerProfile fields
    user_id          UUID,
    keycloak_user_id UUID        NOT NULL,
    language         VARCHAR(50) NOT NULL DEFAULT 'en',
    phone_number     VARCHAR(50),
    address          VARCHAR(255),
    date_of_birth    DATE,
    gender           VARCHAR(20),

    CONSTRAINT fk_customer_profile_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX idx_customer_profile_user_id
    ON customer_profile (user_id)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX idx_customer_profile_keycloak_user_id
    ON customer_profile (keycloak_user_id);

CREATE TABLE store_manager_profile (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    -- StoreManagerProfile fields
    user_id          UUID,
    keycloak_user_id UUID        NOT NULL,
    work_phone       VARCHAR(50),

    CONSTRAINT fk_store_manager_profile_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX idx_store_manager_profile_user_id
    ON store_manager_profile (user_id)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX idx_store_manager_profile_keycloak_user_id
    ON store_manager_profile (keycloak_user_id);

CREATE TABLE super_admin_profile (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    -- SuperAdminProfile fields
    user_id          UUID,
    keycloak_user_id UUID        NOT NULL,
    work_phone       VARCHAR(50),

    CONSTRAINT fk_super_admin_profile_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX idx_super_admin_profile_user_id
    ON super_admin_profile (user_id)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX idx_super_admin_profile_keycloak_user_id
    ON super_admin_profile (keycloak_user_id);

CREATE TABLE support_agent_profile (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    -- SupportAgentProfile fields
    user_id          UUID,
    keycloak_user_id UUID        NOT NULL,
    shift            VARCHAR(50),
    work_phone       VARCHAR(50),
    support_phone    VARCHAR(50),

    CONSTRAINT fk_support_agent_profile_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX idx_support_agent_profile_user_id
    ON support_agent_profile (user_id)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX idx_support_agent_profile_keycloak_user_id
    ON support_agent_profile (keycloak_user_id);

CREATE TABLE warehouse_staff_profile (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    -- WarehouseStaffProfile fields
    user_id          UUID,
    keycloak_user_id UUID        NOT NULL,
    shift            VARCHAR(50),
    work_phone       VARCHAR(50),

    CONSTRAINT fk_warehouse_staff_profile_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX idx_warehouse_staff_profile_user_id
    ON warehouse_staff_profile (user_id)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX idx_warehouse_staff_profile_keycloak_user_id
    ON warehouse_staff_profile (keycloak_user_id);