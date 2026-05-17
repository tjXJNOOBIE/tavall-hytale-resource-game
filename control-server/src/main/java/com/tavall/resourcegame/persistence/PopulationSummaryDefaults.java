package com.tavall.resourcegame.persistence;

import com.tavall.resourcegame.domain.AgingState;
import com.tavall.resourcegame.domain.CitizenMetaData;
import com.tavall.resourcegame.domain.TroopMetaData;

import java.time.Instant;

/**
 * Defaults used when loading persisted state without rich metadata.
 */
public final class PopulationSummaryDefaults {
    private PopulationSummaryDefaults() {
    }

    public static CitizenMetaData citizenMetaData() {
        return CitizenMetaData.defaults();
    }

    public static TroopMetaData troopMetaData() {
        return TroopMetaData.defaults();
    }

    public static AgingState agingState() {
        return AgingState.defaults(Instant.now());
    }
}
