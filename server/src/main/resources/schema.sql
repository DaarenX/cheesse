CREATE TABLE IF NOT EXISTS games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fen VARCHAR(255),
    pgn TEXT,
    join_token VARCHAR(36) NOT NULL UNIQUE,
    white_player_id BIGINT,
    white_player_token VARCHAR(36),
    black_player_id BIGINT,
    black_player_token VARCHAR(36)
);
