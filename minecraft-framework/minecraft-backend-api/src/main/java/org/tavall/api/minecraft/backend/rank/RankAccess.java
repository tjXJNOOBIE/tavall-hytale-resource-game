package org.tavall.api.minecraft.backend.rank;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RankAccess extends RankRepository {
    void createProfile(UUID playerId, String playerName);

    boolean playerExistsByUsername(String username);

    Optional<RankPlayerProfile> findPlayerProfileByUsername(String username);

    void setRankFromUsername(String username, String rank);

    void setRank(UUID uuid, String rank);

    void revokeRank(UUID uuid, String fallbackRankName);

    String getRank(UUID uuid);

    int getPowerLevel(UUID uuid);

    Set<String> getPermissions(UUID uuid);

    void setPermissions(UUID uuid, Set<String> permissions);

    boolean hasPermission(UUID uuid, String permission);

    boolean rankExists(String rankName);

    List<String> getRanks();

    String getAllRanks();
}
