CREATE TABLE IF NOT EXISTS games (
    id BIGSERIAL PRIMARY KEY,
    fen VARCHAR(255) NOT NULL DEFAULT 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1',
    pgn TEXT NOT NULL DEFAULT '',
    join_token UUID NOT NULL UNIQUE,
    white_player_id BIGINT,
    white_player_token UUID,
    black_player_id BIGINT,
    black_player_token UUID
);

-- Move history (start position + space-separated UCI moves), needed for threefold repetition.
-- Games created before these columns existed start their history at their current position.
ALTER TABLE games ADD COLUMN IF NOT EXISTS start_fen VARCHAR(255);
UPDATE games SET start_fen = fen WHERE start_fen IS NULL;
ALTER TABLE games ALTER COLUMN start_fen SET DEFAULT 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
ALTER TABLE games ALTER COLUMN start_fen SET NOT NULL;
ALTER TABLE games ADD COLUMN IF NOT EXISTS move_history TEXT NOT NULL DEFAULT '';
