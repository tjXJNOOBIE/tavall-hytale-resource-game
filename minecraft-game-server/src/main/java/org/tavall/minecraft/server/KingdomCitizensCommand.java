package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.List;
import java.util.Optional;

public final class KingdomCitizensCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "citizens";
    }

    @Override
    protected String usage() {
        return "Usage: /kd citizens [summary|spawn|migrate|list|debug|age|ageall|setstage|setjob|clearjob|train|promote|demote|health|morale|nutrition|housing|maintenance|refresh-cache|refresh-displays]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("summary", "spawn", "migrate", "list", "debug", "age", "ageall", "setstage", "setjob", "clearjob", "train", "promote", "demote", "health", "morale", "nutrition", "housing", "maintenance", "refresh-cache", "refresh-displays");
    }

    @Override
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.CASTLE_CITIZENS);
    }
}
