package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.ArrayList;
import java.util.List;

public final class DiscordCommandTokenParser implements IDiscordCommandTokenParser, IDependencyInjectableConcrete {
    @Override
    public List<String> parseCommandTokens(String commandText) {
        if (commandText == null || commandText.isBlank()) {
            return List.of();
        }
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < commandText.length(); index++) {
            char character = commandText.charAt(index);
            if (character == '"') {
                quoted = !quoted;
                continue;
            }
            if (Character.isWhitespace(character) && !quoted) {
                addToken(tokens, current);
                continue;
            }
            current.append(character);
        }
        addToken(tokens, current);
        return List.copyOf(tokens);
    }

    private void addToken(ArrayList<String> tokens, StringBuilder current) {
        if (!current.isEmpty()) {
            tokens.add(current.toString());
            current.setLength(0);
        }
    }
}
