package org.tavall.control.companion;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CompanionSkillHandler {
    private final Map<UUID, CompanionSkill> skillsById;
    private final Map<String, CompanionSkill> skillsByNameKey;

    public CompanionSkillHandler() {
        List<CompanionSkill> skills = List.of(
                skill("arcane-mend", "Arcane Mend", SkillAffinity.ARCANE, Set.of(CompanionType.HEALER), 5, 18, 10, CompanionSkillTargetType.ALLY, CompanionSkillEffectType.HEAL),
                skill("morale-pulse", "Morale Pulse", SkillAffinity.ARCANE, Set.of(CompanionType.HEALER), 10, 30, 12, CompanionSkillTargetType.TROOP_GROUP, CompanionSkillEffectType.MORALE_BOOST),
                skill("cleanse-wound", "Cleanse Wound", SkillAffinity.ARCANE, Set.of(CompanionType.HEALER), 20, 45, 18, CompanionSkillTargetType.ALLY, CompanionSkillEffectType.HEAL),
                skill("ground-slam", "Ground Slam", SkillAffinity.EARTH, Set.of(CompanionType.BRAWLER), 5, 16, 8, CompanionSkillTargetType.AREA, CompanionSkillEffectType.DAMAGE),
                skill("taunting-roar", "Taunting Roar", SkillAffinity.EARTH, Set.of(CompanionType.BRAWLER), 10, 24, 10, CompanionSkillTargetType.ENEMY, CompanionSkillEffectType.DEBUFF),
                skill("break-guard", "Break Guard", SkillAffinity.EARTH, Set.of(CompanionType.BRAWLER), 20, 32, 14, CompanionSkillTargetType.ENEMY, CompanionSkillEffectType.DEBUFF),
                skill("stone-guard", "Stone Guard", SkillAffinity.EARTH, Set.of(CompanionType.BRUTE), 1, 20, 8, CompanionSkillTargetType.CASTLE_WALL, CompanionSkillEffectType.SIEGE_DEFENSE),
                skill("wall-hold", "Wall Hold", SkillAffinity.EARTH, Set.of(CompanionType.BRUTE), 10, 36, 12, CompanionSkillTargetType.CASTLE_WALL, CompanionSkillEffectType.SHIELD),
                skill("iron-body", "Iron Body", SkillAffinity.EARTH, Set.of(CompanionType.BRUTE), 20, 42, 16, CompanionSkillTargetType.SELF, CompanionSkillEffectType.BUFF),
                skill("arcane-lance", "Arcane Lance", SkillAffinity.ARCANE, Set.of(CompanionType.ARCANE), 5, 14, 9, CompanionSkillTargetType.ENEMY, CompanionSkillEffectType.DAMAGE),
                skill("mana-snare", "Mana Snare", SkillAffinity.ARCANE, Set.of(CompanionType.ARCANE), 10, 26, 12, CompanionSkillTargetType.ENEMY, CompanionSkillEffectType.DEBUFF),
                skill("shield-pierce", "Shield Pierce", SkillAffinity.ARCANE, Set.of(CompanionType.ARCANE), 20, 34, 16, CompanionSkillTargetType.ENEMY, CompanionSkillEffectType.DAMAGE)
        );
        this.skillsById = skills.stream().collect(Collectors.toUnmodifiableMap(CompanionSkill::skillId, Function.identity()));
        this.skillsByNameKey = skills.stream().collect(Collectors.toUnmodifiableMap(skill -> key(skill.name()), Function.identity()));
    }

    public Optional<CompanionSkill> findSkill(UUID skillId) {
        return Optional.ofNullable(skillsById.get(skillId));
    }

    public Optional<CompanionSkill> findSkillByName(String nameOrId) {
        if (nameOrId == null || nameOrId.isBlank()) {
            return Optional.empty();
        }
        try {
            return findSkill(UUID.fromString(nameOrId));
        } catch (IllegalArgumentException ignored) {
            return Optional.ofNullable(skillsByNameKey.get(key(nameOrId)));
        }
    }

    public List<CompanionSkill> availableSkillsFor(CompanionData companionData) {
        return skillsById.values().stream()
                .filter(skill -> skill.requiredCompanionTypes().contains(companionData.type()))
                .filter(skill -> companionData.level() >= skill.requiredLevel())
                .sorted((left, right) -> Integer.compare(left.requiredLevel(), right.requiredLevel()))
                .toList();
    }

    public boolean canUseSkill(CompanionData companionData, CompanionSkill skill) {
        return skill.requiredCompanionTypes().contains(companionData.type()) && companionData.level() >= skill.requiredLevel();
    }

    private CompanionSkill skill(
            String key,
            String name,
            SkillAffinity affinity,
            Set<CompanionType> types,
            int requiredLevel,
            int cooldownSeconds,
            int resourceCost,
            CompanionSkillTargetType targetType,
            CompanionSkillEffectType effectType
    ) {
        return new CompanionSkill(UUID.nameUUIDFromBytes(("companion-skill:" + key).getBytes(StandardCharsets.UTF_8)), name, affinity, types, requiredLevel, cooldownSeconds, resourceCost, 1, 10, targetType, effectType);
    }

    private String key(String value) {
        return value.toLowerCase().replace("_", "-").replace(" ", "-");
    }
}
