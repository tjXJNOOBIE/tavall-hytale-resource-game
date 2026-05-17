package com.tavall.resourcegame.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.domain.BuildingConstructionStage;
import com.tavall.resourcegame.domain.BuildingType;
import com.tavall.resourcegame.resources.ResourceType;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ResourceGameAssetPipelineTest {
    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    private static final Path UI_PAGE_ROOT = RESOURCE_ROOT.resolve(Path.of("Common", "UI", "Custom", "Pages"));
    private static final Path UI_TEXTURE_ROOT = RESOURCE_ROOT.resolve(Path.of("Common", "UI", "Custom", "Textures", "ResourceGame"));
    private static final Path MODEL_ROOT = RESOURCE_ROOT.resolve(Path.of("Common", "Models", "ResourceGame"));
    private static final Path PREFAB_ROOT = RESOURCE_ROOT.resolve(Path.of("Common", "Prefabs", "ResourceGame"));
    private static final Path SERVER_PREFAB_ROOT = RESOURCE_ROOT.resolve(Path.of("Server", "Prefabs", "ResourceGame"));
    private static final Path MODEL_MANIFEST_PATH = MODEL_ROOT.resolve("resource-game-model-assets.json");
    private static final Pattern UI_IMAGE_ASSET_REFERENCE = Pattern.compile("src=\"(Textures/ResourceGame/[^\"]+)\"");
    private static final Pattern UI_BACKGROUND_ASSET_REFERENCE = Pattern.compile("url\\('(\\.\\./Textures/ResourceGame/[^']+)'\\)");
    private static final Pattern RESERVED_HYUI_BUTTON_CLASS = Pattern.compile(
            "<button[^>]*class=\"(?:[^\"]*\\s)?(?:back-button|custom-button|custom-textbutton|action-button|toggle-button|item-slot-button|native-tab-button)(?:\\s[^\"]*)?\"",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern RUNTIME_UNSAFE_HYUI_BUTTON_CLASS = Pattern.compile(
            "<button[^>]*class=\"(?:[^\"]*\\s)?(?:secondary-button|primary-button|confirm-button|danger-button|nav-button|back-button|custom-button|custom-textbutton|action-button|toggle-button|item-slot-button|native-tab-button)(?:\\s[^\"]*)?\"",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern RUNTIME_RAW_BUTTON_CLASS = Pattern.compile(
            "<button[^>]*class=\"(?:[^\"]*\\s)?raw-button(?:\\s[^\"]*)?\"",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern UNSTYLED_HYUI_BUTTON = Pattern.compile("<button\\b(?![^>]*\\bclass=)", Pattern.CASE_INSENSITIVE);
    private static final Pattern UI_ACTION_ROW_BLOCK = Pattern.compile(
            "<div[^>]*class=\"[^\"]*action-row[^\"]*\"[^>]*>(.*?)</div>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern UI_BUTTON_TAG = Pattern.compile("<button\\b", Pattern.CASE_INSENSITIVE);
    private static final int LEGACY_MODEL_COUNT = 7;
    private static final int MAX_BUILDING_LEVEL = 30;
    private static final List<String> INTERIOR_PROP_KEYS = List.of(
            "citizen_anchor",
            "troop_anchor",
            "worker_platform",
            "exit_portal",
            "hologram_pedestal",
            "placement_selector_valid",
            "placement_selector_blocked",
            "castle_radius_marker",
            "node_radius_marker"
    );
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void generatedUiManifestTracksPackagedAssets() throws IOException {
        Path manifestPath = UI_TEXTURE_ROOT.resolve("resource-game-ui-assets.json");
        assertTrue(Files.isRegularFile(manifestPath), "UI asset manifest is missing");

        JsonNode manifest = OBJECT_MAPPER.readTree(manifestPath.toFile());
        assertEquals("resource-game-ui-assets/v1", manifest.path("schema").asText());
        assertEquals("scripts/generate-resource-game-assets.py", manifest.path("generatedBy").asText());
        assertTrue(manifest.path("uiTextures").size() >= 40, "Expected the generated UI texture set");

        for (JsonNode assetNode : manifest.path("uiTextures")) {
            Path assetPath = RESOURCE_ROOT.resolve(assetNode.path("path").asText()).normalize();
            assertTrue(Files.isRegularFile(assetPath), () -> "Generated UI asset is missing: " + assetPath);
            BufferedImage image = ImageIO.read(assetPath.toFile());
            assertNotNull(image, () -> "Generated UI asset is not a readable image: " + assetPath);
            assertEquals(assetNode.path("width").asInt(), image.getWidth(), () -> "Width drifted for " + assetPath);
            assertEquals(assetNode.path("height").asInt(), image.getHeight(), () -> "Height drifted for " + assetPath);
            if (!assetPath.toString().contains("\\fonts\\") && !assetPath.toString().contains("/fonts/")) {
                assertMirrored(image, assetPath);
            }
        }
    }

    @Test
    void customUiPagesReferencePackagedTextureAssets() throws IOException {
        List<Path> pages = Files.list(UI_PAGE_ROOT)
                .filter(path -> path.getFileName().toString().endsWith(".html"))
                .sorted()
                .toList();

        assertFalse(pages.isEmpty(), "No HyUI pages were found");
        for (Path page : pages) {
            String content = Files.readString(page);
            assertTrue(
                    content.contains("url('../Textures/ResourceGame/panels/"),
                    () -> "Page is not using a generated panel texture: " + page
            );
            assertTrue(
                    content.contains("<img"),
                    () -> "Page is not using a generated icon image: " + page
            );
            assertFalse(
                    content.contains("src=\"../Textures/ResourceGame/"),
                    () -> "HyUI img paths must be relative to UI/Custom because HyUI prefixes AssetImage paths: " + page
            );
            assertTrue(
                    content.contains("url('../Textures/ResourceGame/buttons/"),
                    () -> "Page is not using generated button assets: " + page
            );
            assertFalse(
                    RESERVED_HYUI_BUTTON_CLASS.matcher(content).find(),
                    () -> "Page uses a reserved HyUI button class that changes the parsed control type: " + page
            );
            assertFalse(
                    UNSTYLED_HYUI_BUTTON.matcher(content).find(),
                    () -> "Page has an unclassified button that HyUI renders as a plain text button: " + page
            );
            assertTrue(
                    content.contains(".page-overlay { anchor-width: 1080; anchor-height: 660;"),
                    () -> "Page needs the full-screen-safe overlay contract: " + page
            );
            assertTrue(
                    content.contains(".container { anchor-width: 880;"),
                    () -> "Page needs the full-screen-safe content width: " + page
            );
            assertTrue(
                    content.contains("button { anchor-width: 190; anchor-height: 38;"),
                    () -> "Page buttons need a fixed reachable hitbox: " + page
            );
            assertActionRowsStayReachable(content, page);
            assertDecoratedButtonsUseProductionChrome(content, page);

            int referenceCount = 0;
            Matcher imageMatcher = UI_IMAGE_ASSET_REFERENCE.matcher(content);
            while (imageMatcher.find()) {
                referenceCount++;
                Path referencedAsset = RESOURCE_ROOT.resolve(Path.of("Common", "UI", "Custom", imageMatcher.group(1))).normalize();
                assertTrue(Files.isRegularFile(referencedAsset), () -> "UI image asset reference is missing: " + referencedAsset + " in " + page);
            }
            Matcher backgroundMatcher = UI_BACKGROUND_ASSET_REFERENCE.matcher(content);
            while (backgroundMatcher.find()) {
                referenceCount++;
                Path referencedAsset = page.getParent().resolve(backgroundMatcher.group(1)).normalize();
                assertTrue(Files.isRegularFile(referencedAsset), () -> "UI asset reference is missing: " + referencedAsset + " in " + page);
            }
            assertTrue(referenceCount >= 3, () -> "Expected panel, icon, and button references in " + page);
        }
    }

    @Test
    void generatedModelManifestTracksPackagedLevelAssets() throws IOException {
        assertTrue(Files.isRegularFile(MODEL_MANIFEST_PATH), "Model asset manifest is missing");

        JsonNode manifest = OBJECT_MAPPER.readTree(MODEL_MANIFEST_PATH.toFile());
        int expectedBuildingLevelCount = BuildingType.values().length * MAX_BUILDING_LEVEL;
        int expectedConstructionCount = BuildingType.values().length * BuildingConstructionStage.values().length;
        int expectedResourceNodeCount = ResourceType.values().length * 2;
        int expectedAssetCount = LEGACY_MODEL_COUNT
                + MAX_BUILDING_LEVEL
                + expectedConstructionCount
                + expectedBuildingLevelCount
                + expectedResourceNodeCount
                + INTERIOR_PROP_KEYS.size();

        assertEquals("resource-game-model-assets/v1", manifest.path("schema").asText());
        assertEquals("scripts/generate-resource-game-assets.py", manifest.path("generatedBy").asText());
        assertEquals(MAX_BUILDING_LEVEL, manifest.path("maxBuildingLevel").asInt());
        assertEquals(expectedAssetCount, manifest.path("assets").size());
        assertCategoryCount(manifest, "legacy-overview", LEGACY_MODEL_COUNT);
        assertCategoryCount(manifest, "castle-level", MAX_BUILDING_LEVEL);
        assertCategoryCount(manifest, "building-construction", expectedConstructionCount);
        assertCategoryCount(manifest, "building-level", expectedBuildingLevelCount);
        assertCategoryCount(manifest, "resource-node", expectedResourceNodeCount);
        assertCategoryCount(manifest, "interior-prop", INTERIOR_PROP_KEYS.size());

        for (JsonNode assetNode : manifest.path("assets")) {
            Path modelPath = Path.of(assetNode.path("model").asText());
            Path recipePath = Path.of(assetNode.path("prefabRecipe").asText());
            assertTrue(Files.isRegularFile(modelPath), () -> "Manifest model is missing: " + modelPath);
            assertTrue(Files.isRegularFile(recipePath), () -> "Manifest prefab recipe is missing: " + recipePath);
        }
    }

    @Test
    void buildingLevelModelsCoverEveryBuildingLevel() throws IOException {
        for (BuildingType buildingType : BuildingType.values()) {
            assertEquals(MAX_BUILDING_LEVEL, buildingType.maxLevel(), () -> "Unexpected max level for " + buildingType);
            String buildingKey = buildingType.shortKey();
            for (int level = 1; level <= MAX_BUILDING_LEVEL; level++) {
                String levelKey = levelKey(level);
                assertModelAndRecipe(
                        MODEL_ROOT.resolve(Path.of("buildings", buildingKey, levelKey + ".bbmodel")),
                        "resource_game:buildings/" + buildingKey + "/" + levelKey,
                        PREFAB_ROOT.resolve(Path.of("buildings", buildingKey, levelKey + ".resource-prefab.json")),
                        "prefabs/resource_game/buildings/" + buildingKey + "/" + levelKey + ".prefab.json"
                );
            }
        }
    }

    @Test
    void castleLevelModelsCoverEveryCastleLevel() throws IOException {
        for (int level = 1; level <= MAX_BUILDING_LEVEL; level++) {
            String levelKey = levelKey(level);
            assertModelAndRecipe(
                    MODEL_ROOT.resolve(Path.of("castles", "castle_keep", levelKey + ".bbmodel")),
                    "resource_game:castles/castle_keep/" + levelKey,
                    PREFAB_ROOT.resolve(Path.of("castles", "castle_keep", levelKey + ".resource-prefab.json")),
                    "prefabs/resource_game/castles/castle_keep/" + levelKey + ".prefab.json"
            );
        }
    }

    @Test
    void constructionStageModelsCoverEveryBuilding() throws IOException {
        for (BuildingType buildingType : BuildingType.values()) {
            String buildingKey = buildingType.shortKey();
            for (BuildingConstructionStage constructionStage : BuildingConstructionStage.values()) {
                String stageKey = constructionStage.name().toLowerCase(Locale.ROOT);
                assertModelAndRecipe(
                        MODEL_ROOT.resolve(Path.of("buildings", buildingKey, "construction", stageKey + ".bbmodel")),
                        "resource_game:buildings/" + buildingKey + "/construction/" + stageKey,
                        PREFAB_ROOT.resolve(Path.of("buildings", buildingKey, "construction", stageKey + ".resource-prefab.json")),
                        "prefabs/resource_game/buildings/" + buildingKey + "/construction/" + stageKey + ".prefab.json"
                );
            }
        }
    }

    @Test
    void resourceNodeAndInteriorPropModelsCoverRuntimePlaceholders() throws IOException {
        for (ResourceType resourceType : ResourceType.values()) {
            String resourceKey = resourceType.name().toLowerCase(Locale.ROOT);
            for (String stateKey : List.of("full", "depleted")) {
                assertModelAndRecipe(
                        MODEL_ROOT.resolve(Path.of("nodes", resourceKey, stateKey + ".bbmodel")),
                        "resource_game:nodes/" + resourceKey + "/" + stateKey,
                        PREFAB_ROOT.resolve(Path.of("nodes", resourceKey, stateKey + ".resource-prefab.json")),
                        "prefabs/resource_game/nodes/" + resourceKey + "/" + stateKey + ".prefab.json"
                );
            }
        }

        for (String propKey : INTERIOR_PROP_KEYS) {
            assertModelAndRecipe(
                    MODEL_ROOT.resolve(Path.of("props", propKey + ".bbmodel")),
                    "resource_game:props/" + propKey,
                    PREFAB_ROOT.resolve(Path.of("props", propKey + ".resource-prefab.json")),
                    "prefabs/resource_game/props/" + propKey + ".prefab.json"
            );
        }
    }

    @Test
    void blockbenchModelsAndPrefabRecipesArePackaged() throws IOException {
        List<String> modelNames = List.of(
                "castle_keep",
                "farmstead",
                "lumber_mill",
                "iron_works",
                "barracks",
                "workshop",
                "resource_node"
        );

        for (String modelName : modelNames) {
            Path modelPath = MODEL_ROOT.resolve(modelName + ".bbmodel");
            assertTrue(Files.isRegularFile(modelPath), () -> "Blockbench model is missing: " + modelPath);
            JsonNode model = OBJECT_MAPPER.readTree(modelPath.toFile());
            assertEquals("resource_game:" + modelName, model.path("model_identifier").asText());
            assertTrue(model.path("elements").size() >= 5, () -> "Model has too few elements: " + modelPath);
            assertModelTextureIsPackaged(modelPath, model);

            Path recipePath = PREFAB_ROOT.resolve(modelName + ".resource-prefab.json");
            assertTrue(Files.isRegularFile(recipePath), () -> "Prefab recipe is missing: " + recipePath);
            JsonNode recipe = OBJECT_MAPPER.readTree(recipePath.toFile());
            assertEquals("resource-game-prefab-recipe/v1", recipe.path("schema").asText());
            assertEquals("resource_game:" + modelName, recipe.path("name").asText());
            assertTrue(recipe.path("hytalePrefabTarget").asText().endsWith(modelName + ".prefab.json"));
            assertTrue(recipe.path("blocks").size() >= 20, () -> "Prefab recipe is too small: " + recipePath);
        }
    }

    @Test
    void farmsteadNativeHytaleModelVariantsArePackaged() throws IOException {
        int previousVisibleNodes = 0;
        for (int level = 1; level <= 5; level++) {
            Path modelPath = RESOURCE_ROOT.resolve(Path.of("Common", "Items", "ResourceGame", "Farmstead", titleLevelKey(level), "Model.blockymodel"));
            Path serverModelPath = RESOURCE_ROOT.resolve(Path.of("Server", "Models", "ResourceGame", "Farmstead", titleLevelKey(level) + ".json"));
            JsonNode model = assertNativeFarmsteadModel(modelPath, titleLevelKey(level));
            int visibleNodes = countVisibleBoxNodes(model);
            assertTrue(visibleNodes > previousVisibleNodes, () -> "Farmstead level model should reveal more pieces than the previous level: " + modelPath);
            previousVisibleNodes = visibleNodes;
            assertNativeServerModel(serverModelPath, "Items/ResourceGame/Farmstead/" + titleLevelKey(level) + "/Model.blockymodel");
        }

        int previousConstructionNodes = 0;
        for (String stageKey : List.of("Foundation", "Scaffolding", "Shell", "Complete")) {
            Path modelPath = RESOURCE_ROOT.resolve(Path.of("Common", "Items", "ResourceGame", "Farmstead", "Construction", stageKey, "Model.blockymodel"));
            Path serverModelPath = RESOURCE_ROOT.resolve(Path.of("Server", "Models", "ResourceGame", "Farmstead", "Construction", stageKey + ".json"));
            JsonNode model = assertNativeFarmsteadModel(modelPath, stageKey);
            int visibleNodes = countVisibleBoxNodes(model);
            assertTrue(visibleNodes > previousConstructionNodes, () -> "Farmstead construction model should reveal more pieces than the previous stage: " + modelPath);
            previousConstructionNodes = visibleNodes;
            assertNativeServerModel(serverModelPath, "Items/ResourceGame/Farmstead/Construction/" + stageKey + "/Model.blockymodel");
        }
    }

    @Test
    void farmsteadNativeHytaleModelAssetsArePackaged() throws IOException {
        Path modelPath = RESOURCE_ROOT.resolve(Path.of("Common", "Items", "ResourceGame", "Farmstead", "Model.blockymodel"));
        Path texturePath = RESOURCE_ROOT.resolve(Path.of("Common", "Items", "ResourceGame", "Farmstead", "Texture.png"));
        Path serverModelPath = RESOURCE_ROOT.resolve(Path.of("Server", "Models", "ResourceGame", "Farmstead.json"));

        assertTrue(Files.isRegularFile(modelPath), () -> "Farmstead Hytale block model is missing: " + modelPath);
        assertTrue(Files.isRegularFile(texturePath), () -> "Farmstead Hytale texture is missing: " + texturePath);
        assertTrue(Files.isRegularFile(serverModelPath), () -> "Farmstead server model asset is missing: " + serverModelPath);

        JsonNode serverModel = OBJECT_MAPPER.readTree(serverModelPath.toFile());
        assertEquals("Items/ResourceGame/Farmstead/Model.blockymodel", serverModel.path("Model").asText());
        assertEquals("Items/ResourceGame/Farmstead/Texture.png", serverModel.path("Texture").asText());
        assertTrue(serverModel.path("HitBox").path("Max").path("Y").asDouble() >= 6.0D, "Farmstead hitbox should fit the prop height");
    }

    @Test
    void farmsteadInteractionAssetsWireSecondaryClickToCustomInteraction() throws IOException {
        Path rootInteractionPath = RESOURCE_ROOT.resolve(Path.of("Server", "Item", "RootInteractions", "Tavall_Open_Farmstead_Menu.json"));
        Path interactionPath = RESOURCE_ROOT.resolve(Path.of("Server", "Item", "Interactions", "Tavall_Open_Farmstead_Menu_Interaction.json"));

        assertTrue(Files.isRegularFile(rootInteractionPath), () -> "Farmstead root interaction is missing: " + rootInteractionPath);
        assertTrue(Files.isRegularFile(interactionPath), () -> "Farmstead interaction asset is missing: " + interactionPath);

        JsonNode rootInteraction = OBJECT_MAPPER.readTree(rootInteractionPath.toFile());
        JsonNode interaction = OBJECT_MAPPER.readTree(interactionPath.toFile());
        assertEquals("Tavall_Open_Farmstead_Menu_Interaction", rootInteraction.path("Interactions").get(0).asText());
        assertEquals("tavall:open_farmstead_menu", interaction.path("Type").asText());
    }

    private static void assertCategoryCount(JsonNode manifest, String category, int expectedCount) {
        assertEquals(expectedCount, manifest.path("categoryCounts").path(category).asInt(), () -> "Unexpected model manifest count for " + category);
    }

    private static void assertActionRowsStayReachable(String content, Path page) {
        Matcher rowMatcher = UI_ACTION_ROW_BLOCK.matcher(content);
        while (rowMatcher.find()) {
            int buttonCount = 0;
            Matcher buttonMatcher = UI_BUTTON_TAG.matcher(rowMatcher.group(1));
            while (buttonMatcher.find()) {
                buttonCount++;
            }
            assertTrue(buttonCount <= 4, () -> "Action row has too many buttons for reliable live-client reachability: " + page);
        }
    }

    private static void assertDecoratedButtonsUseProductionChrome(String content, Path page) {
        String decoratedContent = HyUiPageMarkupDecorator.decorate(content);
        assertFalse(
                RUNTIME_UNSAFE_HYUI_BUTTON_CLASS.matcher(decoratedContent).find(),
                () -> "Decorated page uses a HyUI button class that rejects Resource Game action bindings: " + page
        );
        assertTrue(
                RUNTIME_RAW_BUTTON_CLASS.matcher(decoratedContent).find(),
                () -> "Decorated page should promote text buttons to raw image-backed Resource Game button chrome: " + page
        );
        assertTrue(
                decoratedContent.contains("rg-button-label"),
                () -> "Decorated page should render labels inside Resource Game image-backed button content: " + page
        );
        assertTrue(
                decoratedContent.contains("background-image: url('../Textures/ResourceGame/buttons/"),
                () -> "Decorated page should include generated button textures: " + page
        );
        Matcher backgroundMatcher = UI_BACKGROUND_ASSET_REFERENCE.matcher(decoratedContent);
        while (backgroundMatcher.find()) {
            Path referencedAsset = page.getParent().resolve(backgroundMatcher.group(1)).normalize();
            assertTrue(Files.isRegularFile(referencedAsset), () -> "Decorated UI asset reference is missing: " + referencedAsset + " in " + page);
        }
    }

    private static void assertModelAndRecipe(
            Path modelPath,
            String expectedModelIdentifier,
            Path recipePath,
            String expectedPrefabTarget
    ) throws IOException {
        assertTrue(Files.isRegularFile(modelPath), () -> "Blockbench model is missing: " + modelPath);
        JsonNode model = OBJECT_MAPPER.readTree(modelPath.toFile());
        assertEquals(expectedModelIdentifier, model.path("model_identifier").asText(), () -> "Unexpected model identifier for " + modelPath);
        assertTrue(model.path("elements").size() >= 1, () -> "Model has no elements: " + modelPath);
        assertModelTextureIsPackaged(modelPath, model);

        assertTrue(Files.isRegularFile(recipePath), () -> "Prefab recipe is missing: " + recipePath);
        JsonNode recipe = OBJECT_MAPPER.readTree(recipePath.toFile());
        assertEquals("resource-game-prefab-recipe/v1", recipe.path("schema").asText(), () -> "Unexpected prefab recipe schema for " + recipePath);
        assertEquals(expectedPrefabTarget, recipe.path("hytalePrefabTarget").asText(), () -> "Unexpected Hytale prefab target for " + recipePath);
        assertTrue(recipe.path("blocks").size() >= 1, () -> "Prefab recipe has no blocks: " + recipePath);
    }

    private static void assertModelTextureIsPackaged(Path modelPath, JsonNode model) throws IOException {
        assertTrue(model.path("textures").size() >= 1, () -> "Model has no textures: " + modelPath);
        JsonNode textureNode = model.path("textures").get(0);
        String textureReference = textureNode.path("source").asText(textureNode.path("relative_path").asText(textureNode.path("path").asText()));
        if (textureReference.startsWith("data:image/")) {
            assertTrue(textureReference.contains(";base64,"), () -> "Embedded Blockbench texture is not base64 encoded: " + modelPath);
            assertTrue(textureReference.length() > 128, () -> "Embedded Blockbench texture is too short: " + modelPath);
            return;
        }
        Path texturePath = modelPath.getParent().resolve(textureReference).normalize();
        assertTrue(Files.isRegularFile(texturePath), () -> "Blockbench texture is missing: " + texturePath);
        BufferedImage texture = ImageIO.read(texturePath.toFile());
        assertNotNull(texture, () -> "Blockbench texture is not readable: " + texturePath);
        assertMirrored(texture, texturePath);
    }

    private static String levelKey(int level) {
        return String.format(Locale.ROOT, "level_%02d", level);
    }

    private static String titleLevelKey(int level) {
        return String.format(Locale.ROOT, "Level_%02d", level);
    }

    private static JsonNode assertNativeFarmsteadModel(Path modelPath, String expectedVariant) throws IOException {
        assertTrue(Files.isRegularFile(modelPath), () -> "Farmstead native Hytale model variant is missing: " + modelPath);
        JsonNode model = OBJECT_MAPPER.readTree(modelPath.toFile());
        assertEquals(expectedVariant, model.path("variant").path("name").asText(), () -> "Unexpected farmstead model variant marker for " + modelPath);
        assertTrue(countVisibleBoxNodes(model) >= 1, () -> "Farmstead model variant has no visible boxes: " + modelPath);
        return model;
    }

    private static void assertNativeServerModel(Path serverModelPath, String expectedModelPath) throws IOException {
        assertTrue(Files.isRegularFile(serverModelPath), () -> "Farmstead native server model variant is missing: " + serverModelPath);
        JsonNode serverModel = OBJECT_MAPPER.readTree(serverModelPath.toFile());
        assertEquals(expectedModelPath, serverModel.path("Model").asText(), () -> "Unexpected farmstead model pointer for " + serverModelPath);
        assertEquals("Items/ResourceGame/Farmstead/Texture.png", serverModel.path("Texture").asText());
    }

    private static int countVisibleBoxNodes(JsonNode node) {
        int total = 0;
        JsonNode shape = node.path("shape");
        if ("box".equals(shape.path("type").asText()) && shape.path("visible").asBoolean(false)) {
            total++;
        }
        for (JsonNode child : node.path("children")) {
            total += countVisibleBoxNodes(child);
        }
        for (JsonNode root : node.path("nodes")) {
            total += countVisibleBoxNodes(root);
        }
        return total;
    }

    private static boolean hasNegativeBlockCoordinate(JsonNode prefab, String coordinateName) {
        for (JsonNode block : prefab.path("blocks")) {
            if (block.path(coordinateName).asInt() < 0) {
                return true;
            }
        }
        return false;
    }

    private static void assertMirrored(BufferedImage image, Path path) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                String message = "Symmetry mismatch in " + path + " at " + x + "," + y;
                assertEquals(pixel, image.getRGB(width - 1 - x, y), message);
                assertEquals(pixel, image.getRGB(x, height - 1 - y), message);
            }
        }
    }
}
