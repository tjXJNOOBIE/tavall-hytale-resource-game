CREATE TABLE IF NOT EXISTS universal_player_account (
    universal_player_id UUID PRIMARY KEY,
    display_name TEXT NOT NULL,
    primary_email TEXT,
    disabled BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS auth_identity (
    auth_identity_id UUID PRIMARY KEY,
    universal_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    provider TEXT NOT NULL,
    provider_subject TEXT NOT NULL,
    email TEXT,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT auth_identity_provider_subject_unique UNIQUE (provider, provider_subject)
);

CREATE TABLE IF NOT EXISTS platform_account_binding (
    binding_id UUID PRIMARY KEY,
    universal_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    platform TEXT NOT NULL,
    platform_account_id TEXT NOT NULL,
    platform_display_name TEXT NOT NULL DEFAULT '',
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    linked_at TIMESTAMPTZ NOT NULL,
    last_seen_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT platform_account_binding_unique UNIQUE (platform, platform_account_id)
);

CREATE TABLE IF NOT EXISTS account_session (
    session_id UUID PRIMARY KEY,
    universal_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    platform TEXT,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS passwordless_email_challenge (
    challenge_id UUID PRIMARY KEY,
    email TEXT NOT NULL,
    challenge_token_hash TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS platform_link_challenge (
    challenge_id UUID PRIMARY KEY,
    universal_player_id UUID REFERENCES universal_player_account(universal_player_id),
    platform TEXT NOT NULL,
    platform_account_id TEXT,
    short_code_hash TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS two_factor_enrollment (
    enrollment_id UUID PRIMARY KEY,
    universal_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    method TEXT NOT NULL,
    secret_encrypted_or_protected TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    verified_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS two_factor_recovery_code (
    recovery_code_id UUID PRIMARY KEY,
    universal_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    code_hash TEXT NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS global_asset (
    global_asset_id TEXT PRIMARY KEY,
    asset_type TEXT NOT NULL,
    display_name TEXT NOT NULL,
    description TEXT,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS platform_asset_version (
    platform_asset_version_id UUID PRIMARY KEY,
    global_asset_id TEXT NOT NULL REFERENCES global_asset(global_asset_id),
    platform TEXT NOT NULL,
    asset_reference TEXT NOT NULL,
    version INTEGER NOT NULL,
    content_hash TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS platform_asset_version_one_active_idx
    ON platform_asset_version (global_asset_id, platform)
    WHERE active;

CREATE TABLE IF NOT EXISTS guild_kingdom (
    guild_id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    tag TEXT NOT NULL,
    owner_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    state TEXT NOT NULL,
    treasury_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    tax_policy_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    active_buffs_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS guild_member_profile (
    guild_id UUID NOT NULL REFERENCES guild_kingdom(guild_id),
    universal_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    authority_tier TEXT NOT NULL,
    explicit_permissions_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    job_titles_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    joined_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (guild_id, universal_player_id)
);

CREATE TABLE IF NOT EXISTS castle (
    castle_id UUID PRIMARY KEY,
    owner_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    guild_id UUID REFERENCES guild_kingdom(guild_id),
    location_json JSONB NOT NULL,
    castle_type TEXT NOT NULL,
    level INTEGER NOT NULL,
    state TEXT NOT NULL,
    resource_generators_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    defensive_stats_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    global_asset_id TEXT NOT NULL REFERENCES global_asset(global_asset_id),
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS resource_node (
    node_id UUID PRIMARY KEY,
    node_type TEXT NOT NULL,
    location_json JSONB NOT NULL,
    owner_guild_id UUID REFERENCES guild_kingdom(guild_id),
    owner_player_id UUID REFERENCES universal_player_account(universal_player_id),
    production_rate INTEGER NOT NULL,
    current_stored_amount INTEGER NOT NULL,
    contested BOOLEAN NOT NULL DEFAULT FALSE,
    depleted BOOLEAN NOT NULL DEFAULT FALSE,
    global_asset_id TEXT NOT NULL REFERENCES global_asset(global_asset_id),
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS guild_tax_transaction (
    transaction_id UUID PRIMARY KEY,
    guild_id UUID NOT NULL REFERENCES guild_kingdom(guild_id),
    universal_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    resource_type TEXT,
    coin_amount BIGINT,
    resource_amount INTEGER,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS guild_petition (
    petition_id UUID PRIMARY KEY,
    guild_id UUID NOT NULL REFERENCES guild_kingdom(guild_id),
    creator_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    petition_type TEXT NOT NULL,
    message TEXT NOT NULL,
    support_count INTEGER NOT NULL,
    funding_amount BIGINT NOT NULL,
    state TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS propaganda_campaign (
    campaign_id UUID PRIMARY KEY,
    guild_id UUID NOT NULL REFERENCES guild_kingdom(guild_id),
    creator_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    message TEXT NOT NULL,
    funding_amount BIGINT NOT NULL,
    target_scope TEXT NOT NULL,
    reach_score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS troop (
    troop_id UUID PRIMARY KEY,
    owner_player_id UUID REFERENCES universal_player_account(universal_player_id),
    owner_guild_id UUID REFERENCES guild_kingdom(guild_id),
    troop_type TEXT NOT NULL,
    tier INTEGER NOT NULL,
    health INTEGER NOT NULL,
    status TEXT NOT NULL,
    location_json JSONB NOT NULL,
    assigned_castle_id UUID REFERENCES castle(castle_id),
    global_asset_id TEXT NOT NULL REFERENCES global_asset(global_asset_id),
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT troop_tier_range CHECK (tier >= 1 AND tier <= 10)
);

CREATE TABLE IF NOT EXISTS healing_inventory_resource (
    universal_player_id UUID NOT NULL REFERENCES universal_player_account(universal_player_id),
    global_asset_id TEXT NOT NULL REFERENCES global_asset(global_asset_id),
    amount INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (universal_player_id, global_asset_id),
    CONSTRAINT healing_inventory_resource_non_negative CHECK (amount >= 0)
);

CREATE TABLE IF NOT EXISTS healing_facility (
    facility_id UUID PRIMARY KEY,
    owner_player_id UUID REFERENCES universal_player_account(universal_player_id),
    owner_guild_id UUID REFERENCES guild_kingdom(guild_id),
    castle_id UUID REFERENCES castle(castle_id),
    building_level INTEGER NOT NULL,
    state TEXT NOT NULL,
    global_asset_id TEXT NOT NULL REFERENCES global_asset(global_asset_id),
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT healing_facility_level_range CHECK (building_level >= 1 AND building_level <= 30)
);

CREATE TABLE IF NOT EXISTS troop_wound (
    wound_id UUID PRIMARY KEY,
    troop_id UUID NOT NULL REFERENCES troop(troop_id),
    wound_type TEXT NOT NULL,
    severity TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    healed_at TIMESTAMPTZ,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS troop_healing_plan (
    healing_plan_id UUID PRIMARY KEY,
    troop_id UUID NOT NULL REFERENCES troop(troop_id),
    wound_id UUID NOT NULL REFERENCES troop_wound(wound_id),
    healing_mode TEXT NOT NULL,
    selected_recipe_id TEXT,
    facility_id UUID REFERENCES healing_facility(facility_id),
    started_at TIMESTAMPTZ NOT NULL,
    completes_at TIMESTAMPTZ NOT NULL,
    state TEXT NOT NULL,
    required_resources_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    consumed_resources_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS trade_route (
    route_id UUID PRIMARY KEY,
    source_castle_id UUID NOT NULL REFERENCES castle(castle_id),
    destination_castle_id UUID REFERENCES castle(castle_id),
    destination_node_id UUID REFERENCES resource_node(node_id),
    owner_guild_id UUID NOT NULL REFERENCES guild_kingdom(guild_id),
    cargo_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    state TEXT NOT NULL,
    security_level INTEGER NOT NULL,
    travel_progress DOUBLE PRECISION NOT NULL,
    global_asset_id TEXT NOT NULL REFERENCES global_asset(global_asset_id),
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS control_operator (
    operator_id UUID PRIMARY KEY,
    universal_player_id UUID REFERENCES universal_player_account(universal_player_id),
    display_name TEXT NOT NULL,
    role TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS control_command_result (
    command_id UUID PRIMARY KEY,
    state TEXT NOT NULL,
    success BOOLEAN NOT NULL,
    message TEXT NOT NULL,
    platform_results_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    changed_object_ids_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    validation_errors_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS control_command_audit_log (
    audit_log_id UUID PRIMARY KEY,
    command_id UUID NOT NULL,
    command_type TEXT NOT NULL,
    issued_by TEXT NOT NULL,
    operator_role TEXT NOT NULL DEFAULT 'OPERATOR',
    issued_from TEXT NOT NULL,
    target_scope TEXT NOT NULL,
    target_platforms_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    arguments_redacted_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    dry_run BOOLEAN NOT NULL DEFAULT FALSE,
    result_state TEXT NOT NULL,
    success BOOLEAN NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS control_platform_fanout_result (
    fanout_result_id UUID PRIMARY KEY,
    command_id UUID NOT NULL,
    command_type TEXT NOT NULL,
    platform TEXT NOT NULL,
    success BOOLEAN NOT NULL,
    message TEXT NOT NULL,
    frontend_event_ids_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    projection_ids_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    changed_object_ids_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    retry_state TEXT NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    next_attempt_at TIMESTAMPTZ,
    last_attempt_at TIMESTAMPTZ,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS control_command_schedule (
    schedule_id UUID PRIMARY KEY,
    command_id UUID NOT NULL,
    command_type TEXT NOT NULL,
    issued_by TEXT NOT NULL,
    operator_role TEXT NOT NULL DEFAULT 'OPERATOR',
    issued_from TEXT NOT NULL,
    target_scope TEXT NOT NULL,
    target_platforms_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    arguments_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    dry_run BOOLEAN NOT NULL DEFAULT FALSE,
    run_at TIMESTAMPTZ NOT NULL,
    state TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    dispatched_at TIMESTAMPTZ,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS universal_player_account_email_idx ON universal_player_account (primary_email);
CREATE INDEX IF NOT EXISTS platform_account_binding_player_idx ON platform_account_binding (universal_player_id);
CREATE INDEX IF NOT EXISTS account_session_player_idx ON account_session (universal_player_id);
CREATE INDEX IF NOT EXISTS passwordless_email_challenge_email_idx ON passwordless_email_challenge (email);
CREATE INDEX IF NOT EXISTS passwordless_email_challenge_expiry_idx ON passwordless_email_challenge (expires_at);
CREATE INDEX IF NOT EXISTS platform_link_challenge_expiry_idx ON platform_link_challenge (expires_at);
CREATE INDEX IF NOT EXISTS two_factor_enrollment_player_idx ON two_factor_enrollment (universal_player_id);
CREATE INDEX IF NOT EXISTS two_factor_recovery_player_idx ON two_factor_recovery_code (universal_player_id);
CREATE INDEX IF NOT EXISTS guild_member_profile_player_idx ON guild_member_profile (universal_player_id);
CREATE INDEX IF NOT EXISTS castle_owner_idx ON castle (owner_player_id);
CREATE INDEX IF NOT EXISTS castle_guild_idx ON castle (guild_id);
CREATE INDEX IF NOT EXISTS resource_node_owner_guild_idx ON resource_node (owner_guild_id);
CREATE INDEX IF NOT EXISTS resource_node_owner_player_idx ON resource_node (owner_player_id);
CREATE INDEX IF NOT EXISTS guild_tax_transaction_guild_idx ON guild_tax_transaction (guild_id);
CREATE INDEX IF NOT EXISTS guild_petition_guild_idx ON guild_petition (guild_id);
CREATE INDEX IF NOT EXISTS propaganda_campaign_guild_idx ON propaganda_campaign (guild_id);
CREATE INDEX IF NOT EXISTS troop_owner_guild_idx ON troop (owner_guild_id);
CREATE INDEX IF NOT EXISTS healing_inventory_resource_asset_idx ON healing_inventory_resource (global_asset_id);
CREATE INDEX IF NOT EXISTS healing_facility_owner_player_idx ON healing_facility (owner_player_id);
CREATE INDEX IF NOT EXISTS healing_facility_owner_guild_idx ON healing_facility (owner_guild_id);
CREATE INDEX IF NOT EXISTS healing_facility_castle_idx ON healing_facility (castle_id);
CREATE INDEX IF NOT EXISTS troop_wound_troop_idx ON troop_wound (troop_id);
CREATE INDEX IF NOT EXISTS troop_wound_active_idx ON troop_wound (troop_id) WHERE healed_at IS NULL;
CREATE INDEX IF NOT EXISTS troop_healing_plan_troop_idx ON troop_healing_plan (troop_id);
CREATE INDEX IF NOT EXISTS troop_healing_plan_wound_idx ON troop_healing_plan (wound_id);
CREATE INDEX IF NOT EXISTS troop_healing_plan_active_idx ON troop_healing_plan (troop_id) WHERE state IN ('PENDING', 'ACTIVE');
CREATE INDEX IF NOT EXISTS troop_healing_plan_completion_idx ON troop_healing_plan (completes_at) WHERE state = 'ACTIVE';
CREATE INDEX IF NOT EXISTS trade_route_owner_guild_idx ON trade_route (owner_guild_id);
CREATE INDEX IF NOT EXISTS control_operator_role_idx ON control_operator (role) WHERE enabled;
CREATE INDEX IF NOT EXISTS control_command_result_started_idx ON control_command_result (started_at DESC);
CREATE INDEX IF NOT EXISTS control_command_result_state_idx ON control_command_result (state);
CREATE INDEX IF NOT EXISTS control_command_audit_command_idx ON control_command_audit_log (command_id);
CREATE INDEX IF NOT EXISTS control_command_audit_type_idx ON control_command_audit_log (command_type);
CREATE INDEX IF NOT EXISTS control_command_audit_issued_by_idx ON control_command_audit_log (issued_by);
CREATE INDEX IF NOT EXISTS control_command_audit_created_idx ON control_command_audit_log (created_at DESC);
CREATE INDEX IF NOT EXISTS control_command_audit_success_idx ON control_command_audit_log (success);
CREATE INDEX IF NOT EXISTS control_platform_fanout_command_idx ON control_platform_fanout_result (command_id);
CREATE INDEX IF NOT EXISTS control_platform_fanout_platform_idx ON control_platform_fanout_result (platform);
CREATE INDEX IF NOT EXISTS control_platform_fanout_retry_idx ON control_platform_fanout_result (retry_state, next_attempt_at) WHERE retry_state IN ('PENDING', 'RETRYING');
CREATE INDEX IF NOT EXISTS control_command_schedule_due_idx ON control_command_schedule (state, run_at) WHERE state = 'PENDING';
CREATE INDEX IF NOT EXISTS control_command_schedule_command_idx ON control_command_schedule (command_id);
