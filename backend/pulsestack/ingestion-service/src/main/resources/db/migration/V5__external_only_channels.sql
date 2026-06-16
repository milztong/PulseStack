ALTER TABLE channels ADD COLUMN external_only BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE channels SET external_only = TRUE WHERE name = 'predictions';

-- Bisherige Items aus der regulären Ingestion im predictions-Channel entfernen
DELETE FROM news_items WHERE channel_id = (SELECT id FROM channels WHERE name = 'predictions');
