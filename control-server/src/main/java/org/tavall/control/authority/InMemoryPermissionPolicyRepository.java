package org.tavall.control.authority;

import org.tavall.control.runtime.ControlPermission;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryPermissionPolicyRepository implements PermissionPolicyRepository {
    private final Map<CloudCommandType, ControlPermissionPolicy> policiesByCommandType;

    public InMemoryPermissionPolicyRepository() {
        EnumMap<CloudCommandType, ControlPermissionPolicy> policies = new EnumMap<>(CloudCommandType.class);
        put(policies, CloudCommandType.INSTALL_WORKLOAD, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.WORKLOAD_CREATE, false, false, true);
        put(policies, CloudCommandType.NODE_HEARTBEAT_SEND, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.NODE_HEARTBEAT, false, false, true);
        put(policies, CloudCommandType.NODE_STATUS_UPDATE, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.NODE_UPDATE_STATUS, false, false, true);
        put(policies, CloudCommandType.START_WORKLOAD, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.WORKLOAD_START, false, false, true);
        put(policies, CloudCommandType.STOP_WORKLOAD, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.WORKLOAD_STOP, false, false, true);
        put(policies, CloudCommandType.RESTART_WORKLOAD, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.WORKLOAD_RESTART, false, false, true);
        put(policies, CloudCommandType.DELETE_WORKLOAD, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.WORKLOAD_DELETE, true, false, false);
        put(policies, CloudCommandType.MIGRATE_WORKLOAD, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.WORKLOAD_MIGRATE, false, false, true);
        put(policies, CloudCommandType.SCALE_WORKLOAD, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.WORKLOAD_SCALE, false, false, true);
        put(policies, CloudCommandType.OPEN_PORT, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.PORT_ALLOCATE, false, false, true);
        put(policies, CloudCommandType.CLOSE_PORT, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.PORT_RELEASE, false, false, true);
        put(policies, CloudCommandType.RUN_HEALTH_CHECK, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.WORKLOAD_READ, false, false, true);
        put(policies, CloudCommandType.COLLECT_LOGS, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.WORKLOAD_READ, false, false, true);
        put(policies, CloudCommandType.APPLY_FIREWALL_RULE, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.FIREWALL_RULE_APPLY, true, false, false);
        put(policies, CloudCommandType.APPLY_FIREWALL_RULES, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.FIREWALL_RULE_APPLY, true, false, false);
        put(policies, CloudCommandType.APPLY_PROXY_ROUTE, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.PROXY_ROUTE_APPLY, false, false, true);
        put(policies, CloudCommandType.APPLY_PROXY_CONFIG, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.PROXY_ROUTE_APPLY, false, false, true);
        put(policies, CloudCommandType.RUN_BACKUP, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.BACKUP_CREATE, false, false, true);
        put(policies, CloudCommandType.RESTORE_BACKUP, ControlAuthorityLevel.C4_CLUSTER_GOVERNOR, ControlPermission.BACKUP_RESTORE, true, false, false);
        put(policies, CloudCommandType.UPDATE_AGENT, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.NODE_UPDATE_STATUS, false, false, true);
        put(policies, CloudCommandType.DRAIN_NODE, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.NODE_DRAIN, true, false, false);
        put(policies, CloudCommandType.CANCEL_COMMAND, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.COMMAND_CANCEL, false, false, true);
        put(policies, CloudCommandType.RUN_REGIONAL_BOT_TEST, ControlAuthorityLevel.C2_REGIONAL_OPERATOR, ControlPermission.REGION_BOT_TEST_RUN, false, false, true);
        put(policies, CloudCommandType.REGION_FAILOVER, ControlAuthorityLevel.C3_REGIONAL_SUPERVISOR, ControlPermission.REGION_FAILOVER_EXECUTE, true, false, false);
        put(policies, CloudCommandType.CLUSTER_CREATE, ControlAuthorityLevel.C4_CLUSTER_GOVERNOR, ControlPermission.CLUSTER_CREATE, true, false, false);
        put(policies, CloudCommandType.CLUSTER_DESTROY, ControlAuthorityLevel.C4_CLUSTER_GOVERNOR, ControlPermission.CLUSTER_DESTROY, true, false, false);
        put(policies, CloudCommandType.CLUSTER_SCALE, ControlAuthorityLevel.C4_CLUSTER_GOVERNOR, ControlPermission.CLUSTER_SCALE, false, false, true);
        put(policies, CloudCommandType.CLUSTER_MIGRATE, ControlAuthorityLevel.C4_CLUSTER_GOVERNOR, ControlPermission.CLUSTER_MIGRATE, true, false, false);
        put(policies, CloudCommandType.SHARD_ROUTE_MANAGE, ControlAuthorityLevel.C4_CLUSTER_GOVERNOR, ControlPermission.SHARD_ROUTE_MANAGE, false, false, true);
        put(policies, CloudCommandType.GLOBAL_SECRET_ROTATE, ControlAuthorityLevel.C5_GLOBAL_AUTHORITY, ControlPermission.GLOBAL_SECRET_ROTATE, true, false, false);
        put(policies, CloudCommandType.GLOBAL_FAILOVER, ControlAuthorityLevel.C5_GLOBAL_AUTHORITY, ControlPermission.GLOBAL_DISASTER_RECOVERY_EXECUTE, true, true, false);
        put(policies, CloudCommandType.AUTHORITY_GRANT, ControlAuthorityLevel.C5_GLOBAL_AUTHORITY, ControlPermission.AUTHORITY_GRANT, true, false, false);
        put(policies, CloudCommandType.AUTHORITY_REVOKE, ControlAuthorityLevel.C5_GLOBAL_AUTHORITY, ControlPermission.AUTHORITY_REVOKE, true, false, false);
        put(policies, CloudCommandType.BREAK_GLASS_ACTIVATE, ControlAuthorityLevel.C5_GLOBAL_AUTHORITY, ControlPermission.BREAK_GLASS_EXECUTE, true, true, false);
        put(policies, CloudCommandType.AI_RECOMMEND_ACTION, ControlAuthorityLevel.C6_INTELLIGENCE_AUTHORITY, ControlPermission.AI_RECOMMEND, false, false, true);
        put(policies, CloudCommandType.AI_SIMULATE_ACTION, ControlAuthorityLevel.C6_INTELLIGENCE_AUTHORITY, ControlPermission.AI_SIMULATE, false, false, true);
        put(policies, CloudCommandType.AI_EXECUTE_SAFE_ACTION, ControlAuthorityLevel.C6_INTELLIGENCE_AUTHORITY, ControlPermission.AI_EXECUTE_SAFE, false, false, true);
        put(policies, CloudCommandType.AI_EXECUTE_APPROVED_ACTION, ControlAuthorityLevel.C6_INTELLIGENCE_AUTHORITY, ControlPermission.AI_EXECUTE_APPROVED, true, false, true);
        put(policies, CloudCommandType.CONTROL_COMMAND_DISPATCH, ControlAuthorityLevel.C1_LOCAL_EXECUTOR, ControlPermission.COMMAND_DISPATCH, false, false, true);
        this.policiesByCommandType = Map.copyOf(policies);
    }

    @Override
    public ControlPermissionPolicy requiredPolicy(CloudCommandType commandType) {
        return findPolicy(commandType).orElseThrow(() -> new IllegalArgumentException("No permission policy for " + commandType + "."));
    }

    @Override
    public Optional<ControlPermissionPolicy> findPolicy(CloudCommandType commandType) {
        return Optional.ofNullable(policiesByCommandType.get(commandType));
    }

    private void put(
            EnumMap<CloudCommandType, ControlPermissionPolicy> policies,
            CloudCommandType commandType,
            ControlAuthorityLevel minimumAuthorityLevel,
            ControlPermission permission,
            boolean approval,
            boolean breakGlass,
            boolean aiAllowed
    ) {
        policies.put(commandType, new ControlPermissionPolicy(commandType, minimumAuthorityLevel, permission, approval, breakGlass, aiAllowed));
    }
}
