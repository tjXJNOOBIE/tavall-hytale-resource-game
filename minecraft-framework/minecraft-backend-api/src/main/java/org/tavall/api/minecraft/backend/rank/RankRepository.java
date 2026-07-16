package org.tavall.api.minecraft.backend.rank;

import java.util.List;
import java.util.Optional;

public interface RankRepository {
    List<RankDefinition> findRankDefinitions();

    Optional<RankDefinition> findRankDefinition(String rankName);

    RankDefinition saveRankDefinition(RankDefinition definition);

    List<RankPlayerProfile> findPlayerProfiles();

    Optional<RankPlayerProfile> findPlayerProfile(String platformAccountId);

    Optional<RankPlayerProfile> findPlayerProfileByDisplayName(String displayName);

    RankPlayerProfile savePlayerProfile(RankPlayerProfile profile);
}
