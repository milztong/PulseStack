-- Erstellt die stock_predictor Datenbank in der gemeinsamen Postgres-Instanz.
-- Die pulsestack Datenbank wird automatisch durch POSTGRES_DB angelegt.
-- Dieses Script wird beim ersten Start von Postgres ausgeführt (initdb.d).

SELECT 'CREATE DATABASE stock_predictor'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'stock_predictor')\gexec
