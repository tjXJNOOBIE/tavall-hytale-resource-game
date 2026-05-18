package org.tavall.control.companion;

import org.tavall.control.runtime.ControlCommandValidationException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WisdomWellService implements ICompanionDomain {
    public WisdomWellService() {
    }

    public WisdomWellService(CompanionRepository repository, CompanionSkillService skillService) {
        registerCompanionRepository(repository);
        registerCompanionSkillService(skillService);
    }

    public CompanionWisdomUpgrade upgradeCompanionAbility(UUID ownerPlayerId, UUID companionId, UUID skillId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        CompanionSkillService skillService = getCompanionSkillService();
        CompanionSkill skill = skillService.findSkill(skillId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion skill was not found."));
        if (!skillService.canUseSkill(companion, skill)) {
            throw new ControlCommandValidationException("Companion is not eligible for this skill.");
        }
        CompanionRepository repository = getCompanionRepository();
        CompanionWisdomUpgrade current = repository.findWisdomUpgrade(companionId, skillId)
                .orElse(new CompanionWisdomUpgrade(companionId, skillId, 1, 1.0d, 1.0d, nowEpochMillis, Map.of()));
        if (current.skillLevel() >= skill.maxSkillLevel()) {
            throw new ControlCommandValidationException("Companion skill is already at max level.");
        }
        int nextLevel = current.skillLevel() + 1;
        CompanionWisdomUpgrade upgraded = new CompanionWisdomUpgrade(companionId, skillId, nextLevel, calculateAbilityCooldownReduction(nextLevel), 1.0d + nextLevel * 0.08d, nowEpochMillis, Map.of("cost", Integer.toString(calculateAbilityUpgradeCost(skill, current.skillLevel()))));
        return repository.saveWisdomUpgrade(upgraded);
    }

    public int calculateAbilityUpgradeCost(CompanionSkill skill, int currentSkillLevel) {
        return skill.resourceCost() * Math.max(1, currentSkillLevel + 1);
    }

    public double calculateAbilityCooldownReduction(int skillLevel) {
        return Math.max(0.65d, 1.0d - (skillLevel - 1) * 0.035d);
    }

    public CompanionWisdomUpgrade applyWisdomSpeedup(UUID ownerPlayerId, UUID companionId, UUID skillId, long nowEpochMillis) {
        ownedCompanion(ownerPlayerId, companionId);
        CompanionRepository repository = getCompanionRepository();
        CompanionWisdomUpgrade current = repository.findWisdomUpgrade(companionId, skillId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion skill has not been upgraded yet."));
        return repository.saveWisdomUpgrade(new CompanionWisdomUpgrade(companionId, skillId, current.skillLevel(), Math.max(0.6d, current.cooldownModifier() - 0.02d), current.powerModifier(), nowEpochMillis, current.metadata()));
    }

    public List<CompanionSkill> getAvailableWisdomUpgrades(UUID ownerPlayerId, UUID companionId) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        return getCompanionSkillService().availableSkillsFor(companion);
    }

    private CompanionData ownedCompanion(UUID ownerPlayerId, UUID companionId) {
        CompanionData companion = getCompanionRepository().findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        if (!companion.ownerPlayerId().equals(ownerPlayerId)) {
            throw new ControlCommandValidationException("Companion does not belong to this player.");
        }
        return companion;
    }
}
