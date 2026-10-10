CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bucket VARCHAR(255) NOT NULL,
    object_name VARCHAR(255) NOT NULL,
    size_bytes BIGINT CHECK (size_bytes >= 0),
    content_type VARCHAR(50),
    visibility VARCHAR(50), -- 'PUBLIC', 'PRIVATE'
    status VARCHAR(50),
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    CONSTRAINT uq_files_bucket_object UNIQUE (bucket, object_name)
);
CREATE INDEX idx_files_bucket_object_name ON files (bucket, object_name);


CREATE TABLE articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    content_html TEXT,
    status VARCHAR(50) NOT NULL, -- 'DRAFT', 'PUBLISHED', 'ARCHIVED'
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);


CREATE TABLE product_articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, --, 'REVIEW', 'BLOG_POST'
    display_order INT NOT NULL DEFAULT 0,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    CONSTRAINT uq_product_articles_product_article_type UNIQUE (product_id, article_id, type)
);
CREATE INDEX idx_product_articles_product ON product_articles(product_id);


CREATE TABLE product_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    file_id UUID NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, --'THUMBNAIL', 'GALLERY_IMAGE', 'DATASHEET', 'VIDEO'
    display_order INT NOT NULL DEFAULT 0,
    created TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID,
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID,
    CONSTRAINT uq_product_files_product_file_type UNIQUE (product_id, file_id, type)
);
CREATE INDEX idx_product_files_product ON product_files(product_id);