package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetRepository;
import com.tavall.hytale.resourcegame.middleware.healing.HealingInventoryRepository;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRepository;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountBindingRepository;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerAccountRepository;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRepository;

public record ControlCommandRuntime(
        ControlCommandDispatchHandler dispatchHandler,
        ControlCommandParsingHandler parsingHandler,
        ControlCommandRegistry commandRegistry,
        PlatformCommandFanoutHandler fanoutHandler,
        ControlCommandAuditLogRepository auditLogRepository,
        ControlCommandResultRepository resultRepository,
        ControlOperatorRepository operatorRepository,
        UniversalPlayerAccountRepository accountRepository,
        PlatformAccountBindingRepository platformAccountBindingRepository,
        GlobalAssetRepository globalAssetRepository,
        TroopRepository troopRepository,
        TroopHealingRepository troopHealingRepository,
        HealingInventoryRepository healingInventoryRepository
) {
}
