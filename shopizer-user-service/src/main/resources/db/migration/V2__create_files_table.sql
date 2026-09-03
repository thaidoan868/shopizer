CREATE TABLE files (
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

    CONSTRAINT uq_files_bucket_object_name UNIQUE (bucket, object_name)
);

CREATE INDEX idx_files_bucket_object_name ON files (bucket, object_name);
