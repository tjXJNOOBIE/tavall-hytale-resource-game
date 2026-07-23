package org.tavall.control.api;

import org.tavall.control.ControlServerDomain;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.identity.PlatformAccountBinding;
import org.tavall.control.identity.UniversalPlayerAccount;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.api.minecraft.player.PlayerDataRequest;
import org.tavall.api.minecraft.player.PlayerDataResponse;
import org.tavall.api.minecraft.player.PlayerPlatformBindingView;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PlayerDataApi implements ControlServerDomain, IDependencyInjectableConcrete {
    public PlayerDataResponse inspect(PlayerDataRequest request, Instant now) {
        if (request == null) {
            return PlayerDataResponse.unavailable(null, "Player data request was null.");
        }

        ControlCommandRuntime runtime = getControlCommandRuntime();
        UniversalPlayerId playerId = UniversalPlayerId.of(request.playerId());
        Optional<UniversalPlayerAccount> account = runtime.accountRepository().findAccount(playerId);
        List<PlayerPlatformBindingView> bindings = runtime.platformAccountBindingRepository().findPlatformBindings(playerId)
                .stream()
                .map(this::toView)
                .toList();
        String displayName = account.map(UniversalPlayerAccount::displayName)
                .filter(value -> value != null && !value.isBlank())
                .orElse(request.playerName());

        Map<String, String> metadata = new LinkedHashMap<String, String>(request.context());
        metadata.put("serverId", request.serverId());
        metadata.put("worldName", request.worldName() == null ? "" : request.worldName());
        metadata.put("bindingCount", String.valueOf(bindings.size()));
        metadata.put("accountExists", String.valueOf(account.isPresent()));
        account.ifPresent(value -> metadata.putAll(value.metadata()));

        return new PlayerDataResponse(
                request.requestId(),
                true,
                account.isPresent() ? "Player profile loaded." : "Player account not found.",
                request.playerId(),
                displayName,
                account.isPresent(),
                account.map(UniversalPlayerAccount::disabled).orElse(false),
                account.flatMap(UniversalPlayerAccount::primaryEmail).orElse(""),
                account.map(UniversalPlayerAccount::createdAt).orElse(null),
                account.map(UniversalPlayerAccount::updatedAt).orElse(null),
                account.map(this::accountLevel).orElse(0),
                account.map(this::accountExperience).orElse(0),
                account.map(this::accountTotalExperience).orElse(0L),
                bindings,
                metadata
        );
    }

    private PlayerPlatformBindingView toView(PlatformAccountBinding binding) {
        return new PlayerPlatformBindingView(
                binding.platform().name(),
                binding.platformAccountId(),
                binding.platformDisplayName(),
                binding.verified(),
                binding.linkedAt(),
                binding.lastSeenAt(),
                binding.metadata()
        );
    }

    private int accountLevel(UniversalPlayerAccount account) {
        return parseInt(account.metadata().get("accountLevel"), 0);
    }

    private int accountExperience(UniversalPlayerAccount account) {
        return parseInt(account.metadata().get("accountExperience"), 0);
    }

    private long accountTotalExperience(UniversalPlayerAccount account) {
        return parseLong(account.metadata().get("accountTotalExperience"), 0L);
    }

    private int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
