-- ...existing code...
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS sku INTEGER;

CREATE UNIQUE INDEX IF NOT EXISTS product_sku_idx
    ON product (sku);

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS description VARCHAR(500);