package org.tavall.minecraft.server;

import org.tavall.api.minecraft.ui.UiPageType;

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
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.CASTLE_CITIZENS);
    }
}
