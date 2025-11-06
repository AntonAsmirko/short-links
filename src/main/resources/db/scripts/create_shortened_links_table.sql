CREATE TABLE shortened_links (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    original_id INTEGER NOT NULL,
    shortened TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    num_queries INTEGER NOT NULL,
    queries_limit INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (original_id) REFERENCES links(id) ON DELETE CASCADE
);