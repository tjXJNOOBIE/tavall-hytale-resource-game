CREATE TABLE IF NOT EXISTS citizen_records (
    citizen_id UUID PRIMARY KEY,
    owner_player_id UUID NOT NULL,
    kingdom_id TEXT NOT NULL,
    display_name TEXT NOT NULL,
    born_at_epoch_millis BIGINT NOT NULL,
    age_stage TEXT NOT NULL,
    status TEXT NOT NULL,
    job_type TEXT NOT NULL,
    health_state TEXT NOT NULL,
    morale_state TEXT NOT NULL,
    housing_state TEXT NOT NULL,
    nutrition_state TEXT NOT NULL,
    training_state TEXT NOT NULL,
    troop_link_state TEXT NOT NULL,
    strength INTEGER NOT NULL,
    endurance INTEGER NOT NULL,
    agility INTEGER NOT NULL,
    discipline INTEGER NOT NULL,
    intelligence INTEGER NOT NULL,
    morale_resilience INTEGER NOT NULL,
    work_efficiency INTEGER NOT NULL,
    combat_potential INTEGER NOT NULL,
    created_at_epoch_millis BIGINT NOT NULL,
    updated_at_epoch_millis BIGINT NOT NULL,
    last_age_stage_processed_at_epoch_millis BIGINT NOT NULL,
    metadata_json TEXT NOT NULL DEFAULT '{}'
);

CREATE INDEX IF NOT EXISTS idx_citizen_records_owner_player_id ON citizen_records(owner_player_id);
CREATE INDEX IF NOT EXISTS idx_citizen_records_kingdom_id ON citizen_records(kingdom_id);
CREATE INDEX IF NOT EXISTS idx_citizen_records_status ON citizen_records(status);
CREATE INDEX IF NOT EXISTS idx_citizen_records_job_type ON citizen_records(job_type);
CREATE INDEX IF NOT EXISTS idx_citizen_records_age_stage ON citizen_records(age_stage);
CREATE INDEX IF NOT EXISTS idx_citizen_records_updated_at ON citizen_records(updated_at_epoch_millis);
