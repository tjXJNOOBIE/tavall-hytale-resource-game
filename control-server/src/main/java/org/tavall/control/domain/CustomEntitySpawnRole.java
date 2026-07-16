package org.tavall.control.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Debug-spawnable in-world NPC roles for building and citizen interaction flows.
 */
public enum CustomEntitySpawnRole {
    FARMSTEAD_STEWARD("Farmstead Steward", "Outlander_Peon", null, null, List.of("steward", "farmstead_steward", "farm_steward")),
    FARMER("Farmstead Farmer", "Outlander_Peon", BuildingType.FARMSTEAD, null, List.of("farmer", "farm", "farmstead")),
    LUMBERJACK("Lumber Mill Foreman", "Feran_Civilian", BuildingType.LUMBER_MILL, null, List.of("lumberjack", "lumber", "woodcutter", "lumber_mill")),
    IRONMASTER("Iron Works Smith", "Goblin_Miner", BuildingType.IRON_WORKS, null, List.of("ironmaster", "smith", "iron", "iron_works")),
    BARRACKS_CAPTAIN("Barracks Captain", "Trork_Guard", BuildingType.BARRACKS, null, List.of("captain", "guard", "barracks")),
    WORKSHOP_ENGINEER("Workshop Engineer", "Goblin_Scrapper", BuildingType.WORKSHOP, null, List.of("engineer", "workshop")),
    IDLE_CITIZEN("Idle Citizen", "Kweebec_Seedling", null, CitizenJobType.IDLE, List.of("idle", "citizen")),
    GATHERER("Gatherer", "Outlander_Peon", null, CitizenJobType.GATHERER, List.of("gatherer")),
    HUNTER("Hunter", "Feran_Civilian", null, CitizenJobType.HUNTER, List.of("hunter")),
    COOK("Cook", "Feran_Civilian", null, CitizenJobType.COOK, List.of("cook")),
    MINER("Miner", "Goblin_Miner", null, CitizenJobType.MINER, List.of("miner")),
    BLACKSMITH("Blacksmith", "Goblin_Scrapper", null, CitizenJobType.BLACKSMITH, List.of("blacksmith")),
    ARCHITECT("Architect", "Kweebec_Elder", null, CitizenJobType.ARCHITECT, List.of("architect")),
    GRUNT_BUILDER("Grunt Builder", "Outlander_Peon", null, CitizenJobType.GRUNT_BUILDER, List.of("grunt_builder", "builder_grunt")),
    BUILDER("Builder", "Outlander_Peon", null, CitizenJobType.BUILDER, List.of("builder")),
    TRAINEE("Trainee", "Trork_Guard", null, CitizenJobType.TRAINEE, List.of("trainee")),
    SOLDIER("Soldier", "Trork_Guard", null, CitizenJobType.SOLDIER, List.of("soldier"));

    private final String displayName;
    private final String preferredNpcRoleName;
    private final BuildingType buildingType;
    private final CitizenJobType citizenJobType;
    private final List<String> aliases;

    CustomEntitySpawnRole(
            String displayName,
            String preferredNpcRoleName,
            BuildingType buildingType,
            CitizenJobType citizenJobType,
            List<String> aliases
    ) {
        this.displayName = displayName;
        this.preferredNpcRoleName = preferredNpcRoleName;
        this.buildingType = buildingType;
        this.citizenJobType = citizenJobType;
        this.aliases = List.copyOf(aliases);
    }

    public String displayName() {
        return displayName;
    }

    public String preferredNpcRoleName() {
        return preferredNpcRoleName;
    }

    public BuildingType buildingType() {
        return buildingType;
    }

    public CitizenJobType citizenJobType() {
        return citizenJobType;
    }

    public boolean buildingRole() {
        return buildingType != null;
    }

    public boolean citizenRole() {
        return citizenJobType != null;
    }

    public static CustomEntitySpawnRole parse(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String normalized = normalize(token);
        for (CustomEntitySpawnRole role : values()) {
            if (normalize(role.name()).equals(normalized) || role.aliases.stream().anyMatch(alias -> normalize(alias).equals(normalized))) {
                return role;
            }
        }
        return null;
    }

    public static String commandChoices() {
        return String.join("|", Arrays.stream(values()).map(role -> role.aliases.getFirst()).toList());
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
    }
}
