package com.tavall.hytale.resourcegame.middleware.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class KdControlCommandTranslationHandler {
    private static final Set<String> KNOWN_KD_CATEGORIES = Set.of(
            "ui",
            "data",
            "castle",
            "interior",
            "citizens",
            "troops",
            "resources",
            "buildings",
            "nodes",
            "place",
            "focus",
            "interact",
            "scan",
            "account",
            "hologram",
            "entity",
            "bootstrap",
            "scene",
            "tick",
            "tutorial"
    );

    public Optional<String> translateKdCommand(String normalizedInput) {
        ArrayList<String> tokens = tokenize(normalizedInput);
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        String root = tokens.getFirst().toLowerCase(Locale.ROOT);
        if (!root.equals("kd") && !root.equals("kingdom")) {
            return Optional.of(normalizedInput);
        }
        if (tokens.size() < 2) {
            return Optional.empty();
        }
        String category = tokens.get(1).toLowerCase(Locale.ROOT);
        return switch (category) {
            case "tick" -> translateTick(tokens);
            case "resources" -> translateResources(tokens);
            case "troops" -> translateTroops(tokens);
            case "account" -> translateAccount(tokens);
            default -> Optional.empty();
        };
    }

    public String category(String normalizedInput) {
        ArrayList<String> tokens = tokenize(normalizedInput);
        if (tokens.size() < 2) {
            return "root";
        }
        String category = tokens.get(1).toLowerCase(Locale.ROOT);
        return KNOWN_KD_CATEGORIES.contains(category) ? category : "unknown";
    }

    private Optional<String> translateTick(List<String> tokens) {
        if (tokens.size() == 2) {
            return Optional.of("tick healing 1");
        }
        if (tokens.get(2).equalsIgnoreCase("healing")) {
            String count = tokens.size() >= 4 ? tokens.get(3) : "1";
            return Optional.of("tick healing " + count);
        }
        if (tokens.get(2).equalsIgnoreCase("run")) {
            String count = tokens.size() >= 4 ? tokens.get(3) : "1";
            return Optional.of("tick healing " + count);
        }
        return Optional.empty();
    }

    private Optional<String> translateResources(List<String> tokens) {
        if (tokens.size() >= 6 && (tokens.get(2).equalsIgnoreCase("give") || tokens.get(2).equalsIgnoreCase("grant"))) {
            return Optional.of("resource give " + tokens.get(3) + " " + tokens.get(4) + " " + tokens.get(5));
        }
        return Optional.empty();
    }

    private Optional<String> translateTroops(List<String> tokens) {
        if (tokens.size() >= 4 && tokens.get(2).equalsIgnoreCase("debug")) {
            return Optional.of("troop debug " + tokens.get(3));
        }
        if (tokens.size() >= 6 && tokens.get(2).equalsIgnoreCase("wound")) {
            return Optional.of("troop wound " + tokens.get(3) + " " + tokens.get(4) + " " + tokens.get(5));
        }
        if (tokens.size() >= 6 && tokens.get(2).equalsIgnoreCase("heal")) {
            StringBuilder command = new StringBuilder("troop heal ")
                    .append(tokens.get(3))
                    .append(' ')
                    .append(tokens.get(4))
                    .append(' ')
                    .append(tokens.get(5));
            if (tokens.size() >= 7) {
                command.append(' ').append(tokens.get(6));
            }
            if (tokens.size() >= 8) {
                command.append(' ').append(tokens.get(7));
            }
            return Optional.of(command.toString());
        }
        return Optional.empty();
    }

    private Optional<String> translateAccount(List<String> tokens) {
        if (tokens.size() >= 4 && tokens.get(2).equalsIgnoreCase("debug")) {
            return Optional.of("player debug " + tokens.get(3));
        }
        return Optional.empty();
    }

    private ArrayList<String> tokenize(String input) {
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);
            if (character == '"') {
                quoted = !quoted;
                continue;
            }
            if (Character.isWhitespace(character) && !quoted) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(character);
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
