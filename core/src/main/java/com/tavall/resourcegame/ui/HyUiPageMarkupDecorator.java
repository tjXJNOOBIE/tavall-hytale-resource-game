package com.tavall.resourcegame.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies Resource Game production chrome to HyUI documents before HyUI parses them.
 */
public final class HyUiPageMarkupDecorator {
    private static final Pattern BUTTON_BLOCK = Pattern.compile("<button\\b([^>]*)>(.*?)</button>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ATTRIBUTE = Pattern.compile("([A-Za-z0-9:_-]+)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern STYLE_END = Pattern.compile("</style>", Pattern.CASE_INSENSITIVE);
    private static final String RESOURCE_PREFIX = "Common/UI/Custom/";
    private static final String BUTTON_STYLE = "background-image: url('../Textures/ResourceGame/buttons/%s') 64 16;";
    private static final String LABEL_STYLE = "color: #fff1c2; font-size: 15; font-weight: bold; text-transform: uppercase; text-align: center; vertical-align: middle; white-space: nowrap; outline-color: #2d261f;";
    private static final String DISABLED_LABEL_STYLE = "color: #8f9696; font-size: 15; font-weight: bold; text-transform: uppercase; text-align: center; vertical-align: middle; white-space: nowrap; outline-color: #2d261f;";
    private static final String PRODUCTION_STYLE = """
            .container-title { anchor-width: 840; anchor-height: 32; padding: 5; background-image: url('../Textures/ResourceGame/ornaments/ui_divider_section_gold.png'); }
            button { anchor-width: 196; anchor-height: 42; padding: 0; background-color: #00000000; }
            .rg-button-label { anchor-width: 196; anchor-height: 42; padding: 0; color: #fff1c2; font-size: 15; font-weight: bold; text-transform: uppercase; text-align: center; vertical-align: middle; white-space: nowrap; outline-color: #2d261f; }
            .rg-primary-button .rg-button-label { color: #fff8d8; }
            .rg-confirm-button .rg-button-label { color: #fff8d8; }
            .rg-danger-button .rg-button-label { color: #fff8d8; }
            .rg-nav-button .rg-button-label { color: #fff1c2; }
            """;
    private static final Map<String, ButtonAssetSet> BUTTON_ASSETS = createButtonAssets();

    private HyUiPageMarkupDecorator() {
    }

    public static String load(String resourcePath) {
        String normalizedPath = normalizeResourcePath(resourcePath);
        ClassLoader classLoader = HyUiPageMarkupDecorator.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(normalizedPath)) {
            if (stream == null) {
                throw new IllegalArgumentException("HyUI resource was not found: " + normalizedPath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read HyUI resource: " + normalizedPath, exception);
        }
    }

    public static String decorate(String html) {
        String decoratedHtml = decorateButtons(Objects.requireNonNull(html, "html"));
        return injectProductionStyle(decoratedHtml);
    }

    public static String loadDecorated(String resourcePath) {
        return decorate(load(resourcePath));
    }

    private static String normalizeResourcePath(String resourcePath) {
        String normalized = Objects.requireNonNull(resourcePath, "resourcePath").trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith(RESOURCE_PREFIX)) {
            return normalized;
        }
        return RESOURCE_PREFIX + normalized;
    }

    private static String decorateButtons(String html) {
        Matcher matcher = BUTTON_BLOCK.matcher(html);
        StringBuilder builder = new StringBuilder(html.length() + 512);
        while (matcher.find()) {
            Map<String, String> attributes = parseAttributes(matcher.group(1));
            ensureButtonClass(attributes);
            applyButtonAssets(attributes);
            String replacement = renderButtonTag(attributes) + renderButtonContent(attributes, matcher.group(2)) + "</button>";
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static Map<String, String> parseAttributes(String rawAttributes) {
        Map<String, String> attributes = new LinkedHashMap<>();
        Matcher matcher = ATTRIBUTE.matcher(rawAttributes == null ? "" : rawAttributes);
        while (matcher.find()) {
            attributes.put(matcher.group(1), matcher.group(2));
        }
        return attributes;
    }

    private static void ensureButtonClass(Map<String, String> attributes) {
        String classValue = attributes.getOrDefault("class", "").trim();
        if (classValue.isBlank()) {
            attributes.put("class", "raw-button rg-secondary-button");
            return;
        }
        String sanitizedClassValue = removeClasses(
                classValue,
                "custom-textbutton",
                "custom-button",
                "back-button",
                "action-button",
                "toggle-button",
                "item-slot-button",
                "native-tab-button"
        );
        sanitizedClassValue = addResourceGameSemanticClasses(sanitizedClassValue);
        if (!hasClass(sanitizedClassValue, "raw-button")) {
            sanitizedClassValue = "raw-button " + sanitizedClassValue;
        }
        attributes.put("class", sanitizedClassValue.trim());
    }

    private static void applyButtonAssets(Map<String, String> attributes) {
        String classValue = attributes.getOrDefault("class", "");
        ButtonAssetSet assetSet = resolveButtonAssetSet(classValue);
        mergeInlineStyle(attributes, background(assetSet.normal()) + " " + LABEL_STYLE);
    }

    private static ButtonAssetSet resolveButtonAssetSet(String classValue) {
        if (hasClass(classValue, "rg-primary-button") || hasClass(classValue, "primary-button")) {
            return BUTTON_ASSETS.get("primary");
        }
        if (hasClass(classValue, "rg-confirm-button") || hasClass(classValue, "confirm-button")) {
            return BUTTON_ASSETS.get("confirm");
        }
        if (hasClass(classValue, "rg-danger-button") || hasClass(classValue, "danger-button")) {
            return BUTTON_ASSETS.get("danger");
        }
        return BUTTON_ASSETS.get("secondary");
    }

    private static boolean hasClass(String classValue, String expectedClass) {
        String normalizedExpectedClass = expectedClass.toLowerCase(Locale.ROOT);
        for (String token : classValue.toLowerCase(Locale.ROOT).split("\\s+")) {
            if (normalizedExpectedClass.equals(token)) {
                return true;
            }
        }
        return false;
    }

    private static String removeClasses(String classValue, String... removedClasses) {
        Map<String, Boolean> removedClassLookup = new LinkedHashMap<>();
        for (String removedClass : removedClasses) {
            removedClassLookup.put(removedClass.toLowerCase(Locale.ROOT), Boolean.TRUE);
        }
        StringBuilder builder = new StringBuilder(classValue.length());
        for (String token : classValue.split("\\s+")) {
            if (token.isBlank() || removedClassLookup.containsKey(token.toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(token);
        }
        return builder.toString();
    }

    private static String background(String filename) {
        return BUTTON_STYLE.formatted(filename);
    }

    private static String addResourceGameSemanticClasses(String classValue) {
        String result = classValue == null ? "" : classValue.trim();
        result = addSemanticClass(result, "primary-button", "rg-primary-button");
        result = addSemanticClass(result, "secondary-button", "rg-secondary-button");
        result = addSemanticClass(result, "confirm-button", "rg-confirm-button");
        result = addSemanticClass(result, "danger-button", "rg-danger-button");
        result = addSemanticClass(result, "nav-button", "rg-nav-button");
        result = removeClasses(result, "primary-button", "secondary-button", "confirm-button", "danger-button", "nav-button");
        if (!hasClass(result, "rg-primary-button")
                && !hasClass(result, "rg-confirm-button")
                && !hasClass(result, "rg-danger-button")
                && !hasClass(result, "rg-nav-button")
                && !hasClass(result, "rg-secondary-button")) {
            result = (result + " rg-secondary-button").trim();
        }
        return result;
    }

    private static String addSemanticClass(String classValue, String legacyClass, String resourceGameClass) {
        if (!hasClass(classValue, legacyClass) || hasClass(classValue, resourceGameClass)) {
            return classValue;
        }
        return (classValue + " " + resourceGameClass).trim();
    }

    private static void mergeInlineStyle(Map<String, String> attributes, String style) {
        String currentStyle = attributes.getOrDefault("style", "").trim();
        attributes.put("style", currentStyle.isBlank() ? style : currentStyle + " " + style);
    }

    private static String renderButtonTag(Map<String, String> attributes) {
        StringBuilder builder = new StringBuilder("<button");
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            builder.append(' ')
                    .append(entry.getKey())
                    .append("=\"")
                    .append(entry.getValue())
                    .append('"');
        }
        builder.append('>');
        return builder.toString();
    }

    private static String renderButtonContent(Map<String, String> attributes, String content) {
        String labelStyle = hasClass(attributes.getOrDefault("class", ""), "disabled")
                ? DISABLED_LABEL_STYLE
                : LABEL_STYLE;
        String trimmedContent = content == null ? "" : content.trim();
        if (trimmedContent.contains("<")) {
            return trimmedContent;
        }
        return "<p class=\"rg-button-label\" style=\"" + labelStyle + "\">" + trimmedContent + "</p>";
    }

    private static String injectProductionStyle(String html) {
        Matcher matcher = STYLE_END.matcher(html);
        if (!matcher.find()) {
            return "<style>\n" + PRODUCTION_STYLE + "</style>\n" + html;
        }
        return matcher.replaceFirst(Matcher.quoteReplacement(PRODUCTION_STYLE + "</style>"));
    }

    private static Map<String, ButtonAssetSet> createButtonAssets() {
        Map<String, ButtonAssetSet> assets = new LinkedHashMap<>();
        assets.put("primary", new ButtonAssetSet(
                "ui_button_primary_normal.png",
                "ui_button_primary_hover.png",
                "ui_button_primary_pressed.png",
                "ui_button_primary_disabled.png"
        ));
        assets.put("secondary", new ButtonAssetSet(
                "ui_button_secondary_normal.png",
                "ui_button_secondary_hover.png",
                "ui_button_secondary_pressed.png",
                "ui_button_secondary_disabled.png"
        ));
        assets.put("confirm", new ButtonAssetSet(
                "ui_button_confirm_normal.png",
                "ui_button_confirm_hover.png",
                "ui_button_confirm_pressed.png",
                "ui_button_confirm_disabled.png"
        ));
        assets.put("danger", new ButtonAssetSet(
                "ui_button_danger_normal.png",
                "ui_button_danger_hover.png",
                "ui_button_danger_pressed.png",
                "ui_button_danger_disabled.png"
        ));
        return Map.copyOf(assets);
    }

    private record ButtonAssetSet(String normal, String hover, String pressed, String disabled) {
    }
}
