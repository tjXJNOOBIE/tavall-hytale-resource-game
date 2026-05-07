CREATE TABLE IF NOT EXISTS distributed_nodes (
    node_id TEXT PRIMARY KEY,
    node_type TEXT NOT NULL,
    hostname TEXT NOT NULL,
    environment TEXT NOT NULL,
    public_address TEXT,
    private_address TEXT,
    process_id BIGINT NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ NOT NULL,
    last_heartbeat_at TIMESTAMPTZ NOT NULL,
    capabilities_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    status TEXT NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_distributed_nodes_status
    ON distributed_nodes (status, last_heartbeat_at);

CREATE INDEX IF NOT EXISTS idx_distributed_nodes_environment
    ON distributed_nodes (environment, node_type);

CREATE TABLE IF NOT EXISTS live_config_entries (
    config_id UUID PRIMARY KEY,
    config_key TEXT NOT NULL,
    config_type TEXT NOT NULL,
    value_json JSONB NOT NULL DEFAULT 'null'::jsonb,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    environment TEXT NOT NULL DEFAULT 'local',
    version BIGINT NOT NULL DEFAULT 1,
    updated_by TEXT NOT NULL DEFAULT 'system',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    description TEXT NOT NULL DEFAULT '',
    rollout_strategy_json JSONB NOT NULL DEFAULT '{"strategyType":"GLOBAL","targetIds":[],"percentage":100,"metadata":{}}'::jsonb,
    UNIQUE (config_key, environment)
);

CREATE INDEX IF NOT EXISTS idx_live_config_entries_environment
    ON live_config_entries (environment, config_type, config_key);

CREATE TABLE IF NOT EXISTS global_gui_definitions (
    gui_id UUID PRIMARY KEY,
    gui_key TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL DEFAULT '',
    layout_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    version BIGINT NOT NULL DEFAULT 1,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by TEXT NOT NULL DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_global_gui_definitions_enabled
    ON global_gui_definitions (enabled, gui_key);
