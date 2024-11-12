CREATE EXTENSION pg_trgm;
CREATE INDEX idx_trgm ON item USING gin (name gin_trgm_ops);


SELECT show_trgm('안녕하세요');
