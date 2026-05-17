package com.tavall.resourcegame.persistence;

import com.tavall.resourcegame.domain.PlayerGameState;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

/**
 * Persistence contract for player game state.
 */
public interface PlayerGameStateStore {
    Optional<PlayerGameState> findByProfileId(long profileId) throws SQLException;

    PlayerGameState upsert(PlayerGameState state, Instant now) throws SQLException;
}
