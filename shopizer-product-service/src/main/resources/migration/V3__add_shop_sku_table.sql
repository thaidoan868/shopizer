CREATE TABLE shop_sku (
    id                 UUID PRIMARY KEY,
    created            TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by         UUID,
    modified           TIMESTAMP WITH TIME ZONE,
    modified_by        UUID,
    url_key            VARCHAR(255),
    name               VARCHAR(255),
    description        TEXT,
    sale_price         NUMERIC(19, 4) NOT NULL,
    cost               NUMERIC(19, 4) NOT NULL,
    currency           VARCHAR(3),
    is_salable         BOOLEAN NOT NULL DEFAULT TRUE,
    discontinued       BOOLEAN NOT NULL DEFAULT FALSE,
    product_id         UUID REFERENCES shop_product (id),
    available_quantity INTEGER,
    deleted            BOOLEAN NOT NULL DEFAULT FALSE
);