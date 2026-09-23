CREATE TABLE import_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    supplier_name VARCHAR(255) NOT NULL,
    reference_code VARCHAR(100),
    status VARCHAR(50) NOT NULL, -- 'PENDING', 'COMPLETED', 'CANCELLED'
    received_at TIMESTAMPTZ,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

CREATE TABLE import_receipt_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_id UUID NOT NULL REFERENCES import_receipts(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES skus(id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_cost NUMERIC(15, 2) CHECK (unit_cost > 0),
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);
CREATE INDEX idx_import_receipt_lines_receipt ON import_receipt_lines(receipt_id);

CREATE TABLE import_receipt_line_serials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    import_receipt_line_id UUID NOT NULL REFERENCES import_receipt_lines(id) ON DELETE CASCADE,
    serial_number VARCHAR(255) NOT NULL,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    UNIQUE(import_receipt_line_id, serial_number)
);

CREATE TABLE internal_outbound_shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    destination_warehouse_id UUID REFERENCES warehouses(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL), -- 'DRAFT', 'SHIPPED', 'COMPLETED', 'CANCELLED'
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

CREATE TABLE internal_outbound_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL REFERENCES internal_outbound_shipments(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES skus(id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0),
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);
CREATE INDEX idx_internal_outbound_lines_shipment ON internal_outbound_lines(shipment_id);


CREATE TABLE internal_outbound_line_serials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internal_outbound_line_id UUID NOT NULL REFERENCES internal_outbound_lines(id) ON DELETE CASCADE,
    serial_number VARCHAR(255) NOT NULL,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    UNIQUE(internal_outbound_line_id, serial_number)
);

CREATE TABLE return_shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    vendor_name VARCHAR(255) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SHIPPED', 'COMPLETED', 'CANCELLED')),
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);
CREATE INDEX idx_return_shipments_warehouse ON return_shipments(warehouse_id);

CREATE TABLE return_shipment_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_id UUID NOT NULL REFERENCES return_shipments(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES skus(id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0),
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);
CREATE INDEX idx_return_shipment_lines_return ON return_shipment_lines(return_id);


CREATE TABLE return_shipment_line_serials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_shipment_line_id UUID NOT NULL REFERENCES return_shipment_lines(id) ON DELETE CASCADE,
    serial_number VARCHAR(255) NOT NULL,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    UNIQUE(return_shipment_line_id, serial_number)
);