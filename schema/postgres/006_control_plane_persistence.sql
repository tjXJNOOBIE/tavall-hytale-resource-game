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
