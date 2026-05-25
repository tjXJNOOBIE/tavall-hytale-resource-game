CREATE TABLE IF NOT EXISTS minecraft_rank_definitions (
    rank_name TEXT PRIMARY KEY,
    power_level INTEGER NOT NULL,
    permissions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS minecraft_player_rank_profiles (
    platform_account_id TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    rank_name TEXT NOT NULL REFERENCES minecraft_rank_definitions(rank_name) ON UPDATE CASCADE,
    power_level INTEGER NOT NULL,
    permissions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO minecraft_rank_definitions (rank_name, power_level, permissions_json, created_at, updated_at)
VALUES
    ('Member', 100, '[]'::jsonb, NOW(), NOW()),
    ('VIP+', 250, '[]'::jsonb, NOW(), NOW()),
    ('God', 1000, '[]'::jsonb, NOW(), NOW())
ON CONFLICT (rank_name) DO UPDATE SET
    power_level = EXCLUDED.power_level,
    permissions_json = EXCLUDED.permissions_json,
    updated_at = EXCLUDED.updated_at;
