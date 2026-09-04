CREATE TABLE file_metas (
    -- BaseEntity fields
    id               UUID PRIMARY KEY,
    created          TIMESTAMP WITH TIME ZONE,
    created_by       UUID,
    modified         TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,

    -- File fields
    bucket           VARCHAR(255) NOT NULL,
    object_name      VARCHAR(255) NOT NULL,
    size_bytes       BIGINT,
    content_type     VARCHAR(50),
    visibility       VARCHAR(50),
    status           VARCHAR(50),

    CONSTRAINT uq_file_metas UNIQUE (bucket, object_name)
);

CREATE INDEX idx_file_metas_bucket_object_name ON file_metas (bucket, object_name);
