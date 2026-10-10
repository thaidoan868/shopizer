CREATE TABLE system_dictionaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    dict_values TEXT[],
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);
CREATE TABLE attribute_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    code VARCHAR(50),
    attributes JSONB,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

CREATE TABLE warehouses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    location JSONB,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    brand VARCHAR(100),
    description TEXT,
    attributes JSONB,
    is_serialized BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(25),
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

CREATE TABLE skus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku_code VARCHAR(100) UNIQUE,
    barcode VARCHAR(100) UNIQUE NOT NULL,
    price NUMERIC(15, 2) NOT NULL CHECK (price > 0),
    cost NUMERIC(15, 2) NOT NULL CHECK (cost > 0),
    status VARCHAR(25) NOT NULL,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT false
);

-- Postgres does not automatically index foreign keys. This is critical for ON DELETE CASCADE performance.
CREATE INDEX idx_skus_product_id ON skus(product_id);