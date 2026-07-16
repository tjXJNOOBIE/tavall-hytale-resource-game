CREATE TABLE IF NOT EXISTS universal_kingdom_object_snapshot (
    object_type TEXT NOT NULL,
    object_id TEXT NOT NULL,
    scope_id TEXT,
    platform TEXT,
    sort_number INTEGER NOT NULL DEFAULT 0,
    payload_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (object_type, object_id)
);

CREATE INDEX IF NOT EXISTS idx_universal_kingdom_snapshot_type_sort
    ON universal_kingdom_object_snapshot (object_type, sort_number, object_id);

CREATE INDEX IF NOT EXISTS idx_universal_kingdom_snapshot_scope
    ON universal_kingdom_object_snapshot (object_type, scope_id, platform);

CREATE TABLE IF NOT EXISTS kingdom_clock_object_snapshot (
    object_type TEXT NOT NULL,
    object_id TEXT NOT NULL,
    kingdom_id TEXT,
    sort_number BIGINT NOT NULL DEFAULT 0,
    payload_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (object_type, object_id)
);

CREATE INDEX IF NOT EXISTS idx_kingdom_clock_snapshot_kingdom
    ON kingdom_clock_object_snapshot (object_type, kingdom_id, sort_number, object_id);

CREATE INDEX IF NOT EXISTS idx_kingdom_clock_snapshot_type_sort
    ON kingdom_clock_object_snapshot (object_type, sort_number, object_id);

CREATE TABLE IF NOT EXISTS companions (
    companion_id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    type TEXT NOT NULL,
    status TEXT NOT NULL,
    behavior_state TEXT NOT NULL,
    morale_state TEXT NOT NULL,
    level INTEGER NOT NULL,
    xp BIGINT NOT NULL,
    active_skin_id UUID,
    active_wall_section_id TEXT,
    skill_slots_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    calculated_stats_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_companions_player
    ON companions (player_id, created_at, companion_id);

CREATE INDEX IF NOT EXISTS idx_companions_status
    ON companions (status, updated_at);

CREATE TABLE IF NOT EXISTS companion_attributes (
    companion_id UUID PRIMARY KEY REFERENCES companions (companion_id) ON DELETE CASCADE,
    intel DOUBLE PRECISION NOT NULL,
    strength DOUBLE PRECISION NOT NULL,
    agility DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS companion_training_sessions (
    training_session_id UUID PRIMARY KEY,
    companion_id UUID NOT NULL REFERENCES companions (companion_id) ON DELETE CASCADE,
    player_id UUID NOT NULL,
    started_at BIGINT NOT NULL,
    expected_completed_at BIGINT NOT NULL,
    expected_xp BIGINT NOT NULL,
    claimed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_companion_training_active
    ON companion_training_sessions (companion_id, claimed);

CREATE TABLE IF NOT EXISTS companion_skill_upgrades (
    companion_id UUID NOT NULL REFERENCES companions (companion_id) ON DELETE CASCADE,
    skill_id UUID NOT NULL,
    skill_level INTEGER NOT NULL,
    cooldown_modifier DOUBLE PRECISION NOT NULL,
    power_modifier DOUBLE PRECISION NOT NULL,
    updated_at BIGINT NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    PRIMARY KEY (companion_id, skill_id)
);

CREATE TABLE IF NOT EXISTS companion_wall_assignments (
    companion_id UUID PRIMARY KEY REFERENCES companions (companion_id) ON DELETE CASCADE,
    player_id UUID NOT NULL,
    wall_section_id TEXT NOT NULL,
    defense_bonus DOUBLE PRECISION NOT NULL,
    updated_at BIGINT NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_companion_wall_player
    ON companion_wall_assignments (player_id, wall_section_id);
