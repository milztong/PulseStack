-- =============================================================
-- PulseStack – Initiales Datenbankschema
-- Flyway verwaltet alle Änderungen – niemals direkt in der DB!
-- =============================================================

-- Channels: die 25 festen Themen-Kanäle
CREATE TABLE channels (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50)  NOT NULL UNIQUE,   -- z.B. "java", "ai", "finance"
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- News-Items: alle Inhalte aus Reddit, YouTube, GitHub, NewsAPI
CREATE TABLE news_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id     VARCHAR(255) NOT NULL,       -- Original-ID von der Quelle
    source          VARCHAR(50)  NOT NULL,        -- REDDIT | YOUTUBE | GITHUB | NEWSAPI
    channel_id      UUID NOT NULL REFERENCES channels(id),
    title           VARCHAR(1000) NOT NULL,
    url             VARCHAR(2000) NOT NULL,
    thumbnail_url   VARCHAR(2000),               -- Nur Top-3 pro Snapshot
    author          VARCHAR(255),
    score           INTEGER,                      -- Upvotes / Stars / Views
    published_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    fetched_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Duplikate verhindern: pro Quelle ist external_id eindeutig
    CONSTRAINT uq_news_items_source_external UNIQUE (source, external_id)
);

-- Chat-Nachrichten
CREATE TABLE chat_messages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_id  UUID         NOT NULL REFERENCES channels(id),
    sender_name VARCHAR(100) NOT NULL,
    content     VARCHAR(4000) NOT NULL,
    sent_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indizes für häufige Queries
CREATE INDEX idx_news_items_channel_id     ON news_items(channel_id);
CREATE INDEX idx_news_items_source         ON news_items(source);
CREATE INDEX idx_news_items_fetched_at     ON news_items(fetched_at DESC);
CREATE INDEX idx_chat_messages_channel_id  ON chat_messages(channel_id);
CREATE INDEX idx_chat_messages_sent_at     ON chat_messages(sent_at DESC);

-- Seed: die 25 Basis-Channels anlegen
INSERT INTO channels (name, display_name, description) VALUES
    ('java',        'Java',         'Java ecosystem, Spring, JVM languages'),
    ('python',      'Python',       'Python, Django, FastAPI, data science'),
    ('ai',          'AI & ML',      'Machine learning, LLMs, research papers'),
    ('webdev',      'Web Dev',      'Frontend, backend, CSS, JavaScript'),
    ('devops',      'DevOps',       'Docker, Kubernetes, CI/CD, infrastructure'),
    ('finance',     'Finance',      'Markets, crypto, fintech, economics'),
    ('security',    'Security',     'Cybersecurity, vulnerabilities, tools'),
    ('opensource',  'Open Source',  'GitHub trending, new releases, projects'),
    ('rust',        'Rust',         'Rust language, crates, systems programming'),
    ('typescript',  'TypeScript',   'TypeScript, React, Node.js, tooling'),
    ('cloud',       'Cloud',        'AWS, GCP, Azure, serverless'),
    ('linux',       'Linux',        'Linux kernel, distros, CLI tools'),
    ('gaming',      'Gaming',       'Game dev, industry news, releases'),
    ('science',     'Science',      'Research, physics, biology, space'),
    ('startups',    'Startups',     'Funding, launches, founder stories'),
    ('datascience', 'Data Science', 'Pandas, Spark, SQL, analytics'),
    ('mobile',      'Mobile',       'iOS, Android, React Native, Flutter'),
    ('blockchain',  'Blockchain',   'Web3, DeFi, smart contracts'),
    ('ux',          'UX & Design',  'UI design, user research, accessibility'),
    ('golang',      'Go',           'Go language, concurrency, microservices'),
    ('databases',   'Databases',    'PostgreSQL, Redis, MongoDB, SQL'),
    ('networking',  'Networking',   'TCP/IP, protocols, distributed systems'),
    ('csharp',      'C# & .NET',    '.NET ecosystem, ASP.NET, Blazor'),
    ('embedded',    'Embedded',     'IoT, Arduino, Raspberry Pi, firmware'),
    ('general',     'General',      'Tech news, industry trends, misc');
