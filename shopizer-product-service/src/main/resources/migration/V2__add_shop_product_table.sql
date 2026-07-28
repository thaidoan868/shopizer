CREATE TABLE shop_product (
    id            UUID PRIMARY KEY,
    created       TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by    UUID,
    modified      TIMESTAMP WITH TIME ZONE,
    modified_by   UUID,
    url_key       VARCHAR(255),
    name          VARCHAR(255),
    description   TEXT,
    manufacturer  VARCHAR(255),
    default_sku_id UUID,
    deleted       BOOLEAN NOT NULL DEFAULT FALSE
    );