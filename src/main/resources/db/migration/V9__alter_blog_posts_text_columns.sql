-- Flyway Migration V9: Expand blog_posts and event_images columns to TEXT
-- Prevents "value too long for type character varying(255)" when saving long CDN/image URLs, titles, or slugs.

ALTER TABLE blog_posts ALTER COLUMN thumbnail TYPE TEXT;
ALTER TABLE blog_posts ALTER COLUMN title TYPE TEXT;
ALTER TABLE blog_posts ALTER COLUMN slug TYPE TEXT;
ALTER TABLE blog_posts ALTER COLUMN meta_title TYPE TEXT;
ALTER TABLE blog_posts ALTER COLUMN summary TYPE TEXT;
ALTER TABLE blog_posts ALTER COLUMN content TYPE TEXT;
ALTER TABLE blog_posts ALTER COLUMN meta_description TYPE TEXT;

ALTER TABLE event_images ALTER COLUMN image_url TYPE TEXT;
