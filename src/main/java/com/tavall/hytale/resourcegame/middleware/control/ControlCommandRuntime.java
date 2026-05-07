package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetRepository;
import com.tavall.hytale.resourcegame.middleware.citizen.CitizenControlSystem;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockControlSystem;
import com.tavall.hytale.resourcegame.middleware.companion.CompanionService;
import com.tavall.hytale.resourcegame.middleware.healing.HealingInventoryRepository;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRepository;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountBindingRepository;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerAccountRepository;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRepository;

public record ControlCommandRuntime(
        ControlCommandDispatchHandler dispatchHandler,
        FrontendCommandIngressHandler frontendCommandIngressHandler,
        ControlCommandParsingHandler parsingHandler,
        ControlCommandRegistry commandRegistry,
        PlatformCommandFanoutHandler fanoutHandler,
        ControlCommandAuditLogRepository auditLogRepository,
        ControlCommandResultRepository resultRepository,
        ControlOperatorRepository operatorRepository,
        ControlPlatformFanoutRetryRepository fanoutRetryRepository,
        ScheduledControlCommandRepository scheduledCommandRepository,
        ControlCommandSchedulingHandler schedulingHandler,
        ControlPlaneMaintenanceWorker maintenanceWorker,
        ControlCommandCompensationHandler compensationHandler,
        UniversalPlayerAccountRepository accountRepository,
        PlatformAccountBindingRepository platformAccountBindingRepository,
        GlobalAssetRepository globalAssetRepository,
        UniversalKingdomSimulationSystem kingdomSimulationSystem,
        KingdomClockControlSystem kingdomClockSystem,
        CitizenControlSystem citizenControlSystem,
        CompanionService companionService,
        TroopRepository troopRepository,
        TroopHealingRepository troopHealingRepository,
        HealingInventoryRepository healingInventoryRepository
) {
}
