package org.tavall.control.persistence;

import org.tavall.control.domain.AgingState;
import org.tavall.control.domain.CitizenMetaData;
import org.tavall.control.domain.TroopMetaData;

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
