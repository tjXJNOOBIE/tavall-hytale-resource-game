package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class CloudControlPlaneRuntime implements CloudControlDomain, IDependencyInjectableConcrete {
    public CloudRepository repository() {
        return getCloudRepository();
    }

    public IJoinTokenCreationHandler joinTokenCreationHandler() {
        return getJoinTokenCreationHandler();
    }

    public INodeRegistrationRequestHandler nodeRegistrationRequestHandler() {
        return getNodeRegistrationRequestHandler();
    }

    public INodeHeartbeatHandler nodeHeartbeatHandler() {
        return getNodeHeartbeatHandler();
    }

    public INodeSchedulerHandler nodeSchedulerHandler() {
        return getNodeSchedulerHandler();
    }

    public IWorkloadRequestHandler workloadRequestHandler() {
        return getWorkloadRequestHandler();
    }

    public IWorkloadReconciliationHandler workloadReconciliationHandler() {
        return getWorkloadReconciliationHandler();
    }

    public ICloudCommandCreationHandler commandCreationHandler() {
        return getCloudCommandCreationHandler();
    }

    public ICloudCommandValidationHandler commandValidationHandler() {
        return getCloudCommandValidationHandler();
    }

    public ICloudCommandAuthorizationHandler commandAuthorizationHandler() {
        return getCloudCommandAuthorizationHandler();
    }

    public IAgentCommandPollHandler agentCommandPollHandler() {
        return getAgentCommandPollHandler();
    }

    public IAgentCommandResultReportHandler agentCommandResultReportHandler() {
        return getAgentCommandResultReportHandler();
    }

    public ICloudControlCliHandler cliHandler() {
        return getCloudControlCliHandler();
    }

    public ICloudControlPanelViewHandler controlPanelViewHandler() {
        return getCloudControlPanelViewHandler();
    }

    public IVolumeProvisioningHandler volumeProvisioningHandler() {
        return getVolumeProvisioningHandler();
    }

    public IFirewallRuleDesiredStateHandler firewallRuleDesiredStateHandler() {
        return getFirewallRuleDesiredStateHandler();
    }

    public IReverseProxyRouteCreationHandler reverseProxyRouteCreationHandler() {
        return getReverseProxyRouteCreationHandler();
    }

    public IDnsRecordDesiredStateHandler dnsRecordDesiredStateHandler() {
        return getDnsRecordDesiredStateHandler();
    }

    public IMinecraftServerSnapshotIngressHandler minecraftServerSnapshotIngressHandler() {
        return getMinecraftServerSnapshotIngressHandler();
    }
}
