ALTER TABLE shop_product
    ADD CONSTRAINT fk_shop_product_on_default_sku
    FOREIGN KEY (default_sku_id) REFERENCES shop_sku (id);