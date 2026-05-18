package org.tavall.control.identity;

import org.tavall.control.common.GamePlatform;

import java.util.List;
import java.util.Optional;

public interface PlatformAccountBindingRepository {
    PlatformAccountBinding savePlatformBinding(PlatformAccountBinding binding);

    Optional<PlatformAccountBinding> findPlatformBinding(GamePlatform platform, String platformAccountId);

    List<PlatformAccountBinding> findPlatformBindings(UniversalPlayerId universalPlayerId);
}
