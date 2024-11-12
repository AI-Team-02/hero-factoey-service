EXPLAIN (ANALYZE true, COSTS true, FORMAT JSON)
SELECT * FROM item WHERE name like '불 검%';



CREATE INDEX idx_item_name ON item(name);
DROP INDEX IF EXISTS idx_item_name;

CREATE INDEX idx_item_name ON item(name text_pattern_ops);

