-- Entkoppelt den Suchbegriff vom Channel-Namen.
-- Ohne Eintrag fällt ein Channel weiterhin auf seinen Namen zurück (z. B. "java" -> /r/java).
CREATE TABLE channel_source_queries (
    channel_id   UUID         NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    source       VARCHAR(32)  NOT NULL,
    search_query TEXT         NOT NULL,
    PRIMARY KEY (channel_id, source)
);

-- Der ai-Channel lieferte über q=ai vor allem AI-generierte Bilder.
-- Stattdessen gezielt auf die Anbieter eingrenzen.
-- (Reddit ist deaktiviert, da der API-Zugang abgelehnt wurde.)
INSERT INTO channel_source_queries (channel_id, source, search_query)
SELECT id, 'NEWSAPI', '"OpenAI" OR "Anthropic" OR "ChatGPT" OR "Claude AI" OR "Google Gemini" OR "DeepSeek" OR "Mistral AI"'
FROM channels WHERE name = 'ai';

INSERT INTO channel_source_queries (channel_id, source, search_query)
SELECT id, 'YOUTUBE', 'OpenAI|Anthropic|ChatGPT|"Claude AI"|"Google Gemini"|DeepSeek'
FROM channels WHERE name = 'ai';

INSERT INTO channel_source_queries (channel_id, source, search_query)
SELECT id, 'GITHUB', 'llm OR chatgpt OR anthropic OR openai OR deepseek'
FROM channels WHERE name = 'ai';

-- Bisherige Items des ai-Channels entfernen, damit der Slop sofort verschwindet.
DELETE FROM news_items WHERE channel_id = (SELECT id FROM channels WHERE name = 'ai');

UPDATE channels
SET description = 'LLM-Anbieter: OpenAI, Anthropic, Google, DeepSeek'
WHERE name = 'ai';
