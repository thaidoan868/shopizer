CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku_id UUID NOT NULL REFERENCES skus(id) ON DELETE RESTRICT,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    quantity_total INT NOT NULL DEFAULT 0,
    quantity_defective INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    quantity_sold INT NOT NULL DEFAULT 0,
    quantity_returned INT NOT NULL DEFAULT 0,
    quantity_available INT GENERATED ALWAYS AS (quantity_total - quantity_defective - quantity_reserved) STORED,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    UNIQUE(sku_id, warehouse_id)
);


CREATE TABLE inventory_serials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku_id UUID NOT NULL REFERENCES skus(id) ON DELETE RESTRICT,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    serial_number VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL, -- 'AVAILABLE', 'RESERVED', 'SOLD', 'DEFECTIVE', 'RETURNED'
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    UNIQUE(sku_id, serial_number)
);
CREATE INDEX idx_inventory_serials_sku_status ON inventory_serials(sku_id, status);


CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku_id UUID NOT NULL REFERENCES skus(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    transaction_type VARCHAR(50) NOT NULL, -- 'RECEIPT', 'INTERNAL_OUTBOUND', 'RETURN', 'SALE', 'ADJUSTMENT'
    quantity_total_change INT NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_id UUID NOT NULL,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);
CREATE INDEX idx_inv_tx_sku_warehouse ON inventory_transactions(sku_id, warehouse_id);


CREATE TABLE inventory_serial_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku_id UUID NOT NULL REFERENCES skus(id),
    serial_number VARCHAR(100) NOT NULL,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    transaction_type VARCHAR(50) NOT NULL , -- 'RECEIPT', 'INTERNAL_OUTBOUND', 'RETURN', 'SALE',
    status_change VARCHAR(50) NOT NULL , -- 'AVAILABLE', 'RESERVED', 'SOLD', 'DEFECTIVE', 'RETURNED'
    reference_type VARCHAR(50) NOT NULL,
    reference_id UUID NOT NULL,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);
CREATE INDEX idx_inv_serial_tx_serial_id ON inventory_serial_transactions(inventory_serial_id);
