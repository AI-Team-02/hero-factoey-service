CREATE INDEX idx_fts ON item USING gin (to_tsvector('simple', name));


-- 단어를 확실히 구분해서 찾기 때문에, 찾지 못함
EXPLAIN (ANALYZE true, COSTS true, FORMAT JSON)
SELECT name FROM item
WHERE to_tsvector('simple', name) @@ plainto_tsquery('simple', '불 검');

EXPLAIN (ANALYZE true, COSTS true, FORMAT JSON)
SELECT name FROM item
WHERE to_tsvector('simple', name) @@ plainto_tsquery('simple', '불 검의');


SELECT plainto_tsquery('simple', '불 검');


SELECT to_tsquery('simple', '불:* & 검:*');

SELECT to_tsvector('simple', '불 검의 날개 검사');