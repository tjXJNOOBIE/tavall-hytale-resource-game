package org.tavall.control.runtime;

import org.tavall.control.authority.AuthorizationAuditRepository;
import org.tavall.control.authority.AuthorityRepository;
import org.tavall.control.authority.IControlAuthorizationHandler;
import org.tavall.control.authority.PermissionPolicyRepository;
import org.tavall.control.asset.GlobalAssetRepository;
import org.tavall.control.citizen.CitizenControlSystem;
import org.tavall.control.clock.KingdomClockControlSystem;
import org.tavall.control.companion.CompanionHandler;
import org.tavall.control.healing.HealingInventoryRepository;
import org.tavall.control.healing.TroopHealingRepository;
import org.tavall.control.identity.PlatformAccountBindingRepository;
import org.tavall.control.identity.UniversalPlayerAccountRepository;
import org.tavall.control.kingdom.UniversalKingdomSimulationSystem;
import org.tavall.control.troop.TroopRepository;

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
        AuthorityRepository authorityRepository,
        PermissionPolicyRepository permissionPolicyRepository,
        AuthorizationAuditRepository authorizationAuditRepository,
        IControlAuthorizationHandler authorizationHandler,
        UniversalPlayerAccountRepository accountRepository,
        PlatformAccountBindingRepository platformAccountBindingRepository,
        GlobalAssetRepository globalAssetRepository,
        UniversalKingdomSimulationSystem kingdomSimulationSystem,
        KingdomClockControlSystem kingdomClockSystem,
        CitizenControlSystem citizenControlSystem,
        CompanionHandler companionHandler,
        TroopRepository troopRepository,
        TroopHealingRepository troopHealingRepository,
        HealingInventoryRepository healingInventoryRepository
) {
}
