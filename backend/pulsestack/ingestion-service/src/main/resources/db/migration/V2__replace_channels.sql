-- Replace all channels with the new curated list
DELETE FROM news_items;
DELETE FROM chat_messages;
DELETE FROM channels;

INSERT INTO channels (name, display_name, description) VALUES
    ('typescript',  'TypeScript',       'TypeScript, React, Node.js, tooling'),
    ('csharp',      'C# & .NET',        '.NET ecosystem, ASP.NET, Blazor'),
    ('devops',      'DevOps',           'Docker, Kubernetes, CI/CD, infrastructure'),
    ('databases',   'Databases',        'PostgreSQL, Redis, MongoDB, SQL'),
    ('webdev',      'Web Dev',          'Frontend, backend, CSS, JavaScript'),
    ('ai',          'AI & ML',          'Machine learning, LLMs, research papers'),
    ('datascience', 'Data Science',     'Pandas, Spark, SQL, analytics'),
    ('startups',    'Startups',         'Funding, launches, founder stories'),
    ('security',    'Security',         'Cybersecurity, vulnerabilities, tools'),
    ('opensource',  'Open Source',      'GitHub trending, new releases, projects'),
    ('gaming',      'Gaming',           'Game dev, industry news, releases'),
    ('finance',     'Finance',          'Markets, fintech, economics'),
    ('nba',         'NBA',              'Basketball news, games, players'),
    ('nfl',         'NFL',              'American football, games, teams'),
    ('baseball',    'Baseball',         'MLB news, games, players'),
    ('soccer',      'Soccer',           'Football/soccer news, leagues, transfers'),
    ('java',        'Java',             'Java ecosystem, Spring, JVM languages'),
    ('mobile',      'Mobile',           'iOS, Android, React Native, Flutter'),
    ('cloud',       'Cloud',            'AWS, GCP, Azure, serverless'),
    ('python',      'Python',           'Python, Django, FastAPI, data science'),
    ('science',     'Science',          'Research, physics, biology, space'),
    ('politics',    'Politics',         'World politics, elections, policy'),
    ('fitness',     'Fitness',          'Workout, nutrition, health'),
    ('movies',      'Movies & TV',      'Films, series, streaming, reviews'),
    ('music',       'Music',            'New releases, concerts, charts'),
    ('food',        'Food',             'Recipes, restaurants, food trends');
