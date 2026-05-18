package org.tavall.control.cloud;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class CloudControlCliHandler implements ICloudControlCliHandler, ICloudControlDomain {
    public String execute(String input, UUID requestedBy, Instant now) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.startsWith("cloud nodes create-token")) {
            String[] parts = trimmed.split("\\s+");
            long ttlSeconds = parts.length >= 4 ? Long.parseLong(parts[3]) : Duration.ofHours(1).toSeconds();
            Optional<String> region = parts.length >= 5 ? Optional.of(parts[4]) : Optional.empty();
            return "joinToken=" + getJoinTokenCreationHandler()
                    .createJoinToken(requestedBy, now.plusSeconds(ttlSeconds), region, Set.of())
                    + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud nodes register ")) {
            return registerNode(trimmed.substring("cloud nodes register ".length()).trim(), requestedBy, now);
        }
        if (trimmed.equals("cloud nodes list")) {
            return "nodes=" + getCloudRepository().findNodes().size() + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud nodes inspect ")) {
            UUID nodeId = UUID.fromString(trimmed.substring("cloud nodes inspect ".length()).trim());
            return getCloudRepository().findNode(nodeId)
                    .map(node -> "node=" + node.nodeId()
                            + " hostname=" + node.hostname()
                            + " status=" + node.nodeStatus()
                            + " region=" + node.region()
                            + " architecture=" + node.architecture()
                            + System.lineSeparator())
                    .orElse("node=missing" + System.lineSeparator());
        }
        if (trimmed.equals("cloud workloads list")) {
            return renderWorkloadInventory("workloads", getCloudRepository().findWorkloads());
        }
        if (trimmed.equals("cloud servers list")) {
            return renderServerInventory();
        }
        if (trimmed.startsWith("cloud workloads create ")) {
            String[] parts = trimmed.split("\\s+");
            if (parts.length < 5) {
                return "usage=cloud workloads create <type> <name> [region]" + System.lineSeparator();
            }
            WorkloadRequest request = defaultWorkloadRequest(CloudWorkloadType.valueOf(parts[3]), parts[4],
                    parts.length >= 6 ? Optional.of(parts[5]) : Optional.empty());
            CloudWorkload workload = getWorkloadRequestHandler().createDesiredWorkload(request, now);
            String sessionName = getCloudControlPlaneFilesystemLayout().workloadSessionName(workload);
            return "workload=" + workload.workloadId()
                    + " node=" + workload.nodeId().orElseThrow()
                    + " session=" + sessionName
                    + " attach=" + getCloudControlPlaneFilesystemLayout().attachCommand(sessionName)
                    + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud workloads inspect ")) {
            return describeWorkloadById(trimmed.substring("cloud workloads inspect ".length()).trim(), "workload");
        }
        if (trimmed.startsWith("cloud servers inspect ")) {
            return describeWorkloadById(trimmed.substring("cloud servers inspect ".length()).trim(), "server");
        }
        if (trimmed.equals("cloud consoles list")) {
            return renderConsoleInventory();
        }
        if (trimmed.startsWith("cloud consoles attach ")) {
            return attachConsole(trimmed.substring("cloud consoles attach ".length()).trim());
        }
        if (trimmed.startsWith("cloud servers attach ")) {
            return attachConsole(trimmed.substring("cloud servers attach ".length()).trim());
        }
        if (trimmed.equals("cloud kingdoms list")) {
            return renderKingdomInventory();
        }
        if (trimmed.startsWith("cloud kingdoms ensure ")) {
            return ensureKingdom(trimmed.substring("cloud kingdoms ensure ".length()).trim());
        }
        if (trimmed.startsWith("cloud workloads start ")) {
            return updateDesiredState(trimmed.substring("cloud workloads start ".length()).trim(), WorkloadDesiredState.RUNNING, requestedBy, now);
        }
        if (trimmed.startsWith("cloud workloads stop ")) {
            return updateDesiredState(trimmed.substring("cloud workloads stop ".length()).trim(), WorkloadDesiredState.STOPPED, requestedBy, now);
        }
        if (trimmed.startsWith("cloud workloads restart ")) {
            return updateDesiredState(trimmed.substring("cloud workloads restart ".length()).trim(), WorkloadDesiredState.RESTARTING, requestedBy, now);
        }
        if (trimmed.startsWith("cloud workloads delete ")) {
            return updateDesiredState(trimmed.substring("cloud workloads delete ".length()).trim(), WorkloadDesiredState.DELETED, requestedBy, now);
        }
        if (trimmed.equals("cloud commands list")) {
            return "commands=" + getCloudRepository().findCommands().size() + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud commands inspect ")) {
            UUID commandId = UUID.fromString(trimmed.substring("cloud commands inspect ".length()).trim());
            return getCloudRepository().findCommand(commandId)
                    .map(command -> "command=" + command.commandId()
                            + " node=" + command.nodeId()
                            + " type=" + command.commandType()
                            + " status=" + command.status()
                            + System.lineSeparator())
                    .orElse("command=missing" + System.lineSeparator());
        }
        if (trimmed.startsWith("cloud commands retry ")) {
            return retryCommand(trimmed.substring("cloud commands retry ".length()).trim(), requestedBy, now);
        }
        if (trimmed.startsWith("cloud commands cancel ")) {
            return cancelCommand(trimmed.substring("cloud commands cancel ".length()).trim(), requestedBy, now);
        }
        if (trimmed.equals("cloud alerts list")) {
            return "alerts=" + getCloudRepository().findAlerts().size() + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud alerts ack ")) {
            UUID alertId = UUID.fromString(trimmed.substring("cloud alerts ack ".length()).trim());
            Optional<CloudAlert> alert = getCloudRepository().findAlerts().stream()
                    .filter(candidate -> candidate.alertId().equals(alertId))
                    .findFirst();
            if (alert.isEmpty()) {
                return "alert=missing" + System.lineSeparator();
            }
            getCloudRepository().saveAlert(new CloudAlert(alert.get().alertId(), alert.get().alertType(), alert.get().severity(),
                    alert.get().targetType(), alert.get().targetId(), alert.get().message(), CloudAlertState.ACKNOWLEDGED,
                    alert.get().createdAt(), alert.get().resolvedAt(), alert.get().metadata()));
            return "alert=" + alertId + " state=ACKNOWLEDGED" + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud metrics node ")) {
            UUID nodeId = UUID.fromString(trimmed.substring("cloud metrics node ".length()).trim());
            return "nodeMetrics=" + getCloudRepository().findNodeMetrics(nodeId).size() + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud metrics workload ")) {
            UUID workloadId = UUID.fromString(trimmed.substring("cloud metrics workload ".length()).trim());
            return "workloadMetrics=" + getCloudRepository().findWorkloadMetrics(workloadId).size() + System.lineSeparator();
        }
        if (trimmed.equals("cloud ports list")) {
            return "ports=" + getCloudRepository().findPorts().size() + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud ports allocate ")) {
            return allocatePort(trimmed.substring("cloud ports allocate ".length()).trim(), now);
        }
        if (trimmed.equals("cloud volumes list")) {
            return "volumes=" + getCloudRepository().findVolumes().size() + System.lineSeparator();
        }
        if (trimmed.equals("cloud firewall list")) {
            return "firewallRules=" + getCloudRepository().findFirewallRules().size() + System.lineSeparator();
        }
        if (trimmed.equals("cloud proxy routes list")) {
            return "proxyRoutes=" + getCloudRepository().findReverseProxyRoutes().size() + System.lineSeparator();
        }
        if (trimmed.equals("cloud dns records list")) {
            return "dnsRecords=" + getCloudRepository().findDnsRecords().size() + System.lineSeparator();
        }
        if (trimmed.equals("cloud backups list")) {
            return "backupPlans=" + getCloudRepository().findBackupPlans().size()
                    + " backupJobs=" + getCloudRepository().findBackupJobs().size()
                    + " restoreJobs=" + getCloudRepository().findRestoreJobs().size()
                    + System.lineSeparator();
        }
        if (trimmed.startsWith("cloud backups run ")) {
            return runBackup(trimmed.substring("cloud backups run ".length()).trim(), requestedBy, now);
        }
        if (trimmed.startsWith("cloud scheduler plan ")) {
            return schedulerPlan(trimmed.substring("cloud scheduler plan ".length()).trim(), false);
        }
        if (trimmed.startsWith("cloud scheduler candidates ")) {
            return schedulerPlan(trimmed.substring("cloud scheduler candidates ".length()).trim(), true);
        }
        if (trimmed.startsWith("cloud workloads reconcile ")) {
            UUID workloadId = UUID.fromString(trimmed.substring("cloud workloads reconcile ".length()).trim());
            return getWorkloadReconciliationHandler().reconcile(workloadId, requestedBy, now).reason() + System.lineSeparator();
        }
        if (trimmed.equals("cloud workloads reconcile-all")) {
            int created = 0;
            for (CloudWorkload workload : getCloudRepository().findWorkloads()) {
                if (getWorkloadReconciliationHandler().reconcile(workload.workloadId(), requestedBy, now).actionRequired()) {
                    created++;
                }
            }
            return "reconciliationCommands=" + created + System.lineSeparator();
        }
        return String.join(System.lineSeparator(),
                "cloud nodes list",
                "cloud nodes inspect <nodeId>",
                "cloud nodes create-token [ttlSeconds] [region]",
                "cloud nodes register <joinToken> [hostname] [region]",
                "cloud workloads list",
                "cloud servers list",
                "cloud workloads create <type> <name> [region]",
                "cloud workloads inspect <workloadId>",
                "cloud servers inspect <workloadId>",
                "cloud consoles list",
                "cloud consoles attach <target>",
                "cloud workloads start <workloadId>",
                "cloud workloads stop <workloadId>",
                "cloud workloads restart <workloadId>",
                "cloud workloads delete <workloadId>",
                "cloud workloads reconcile <workloadId>",
                "cloud workloads reconcile-all",
                "cloud kingdoms list",
                "cloud kingdoms ensure <kingdomId>",
                "cloud ports list",
                "cloud ports allocate <workloadId> <publicPort> [internalPort] [TCP|UDP]",
                "cloud volumes list",
                "cloud firewall list",
                "cloud proxy routes list",
                "cloud dns records list",
                "cloud backups list",
                "cloud backups run <workloadId> <sourcePath> <destination>",
                "cloud scheduler plan <type> <name> [region]",
                "cloud scheduler candidates <type> <name> [region]",
                "cloud metrics node <nodeId>",
                "cloud metrics workload <workloadId>",
                "cloud commands list",
                "cloud commands inspect <commandId>",
                "cloud commands retry <commandId>",
                "cloud commands cancel <commandId>",
                "cloud alerts list",
                "cloud alerts ack <alertId>",
                "");
    }

    private String updateDesiredState(String workloadIdText, WorkloadDesiredState desiredState, UUID requestedBy, Instant now) {
        UUID workloadId = UUID.fromString(workloadIdText);
        Optional<CloudWorkload> workload = getCloudRepository().findWorkload(workloadId);
        if (workload.isEmpty()) {
            return "workload=missing" + System.lineSeparator();
        }
        getCloudRepository().saveWorkload(workload.orElseThrow().withDesiredState(desiredState, now));
        return getWorkloadReconciliationHandler().reconcile(workloadId, requestedBy, now).reason() + System.lineSeparator();
    }

    private String retryCommand(String commandIdText, UUID requestedBy, Instant now) {
        Optional<CloudCommand> command = getCloudRepository().findCommand(UUID.fromString(commandIdText));
        if (command.isEmpty()) {
            return "command=missing" + System.lineSeparator();
        }
        CloudCommand retry = getCloudCommandCreationHandler().create(command.get().nodeId(), command.get().commandType(),
                command.get().payloadJson(), requestedBy, command.get().correlationId(), now);
        return "command=" + retry.commandId() + " retryOf=" + command.get().commandId() + System.lineSeparator();
    }

    private String cancelCommand(String commandIdText, UUID requestedBy, Instant now) {
        Optional<CloudCommand> command = getCloudRepository().findCommand(UUID.fromString(commandIdText));
        if (command.isEmpty()) {
            return "command=missing" + System.lineSeparator();
        }
        getCloudCommandCreationHandler().create(command.get().nodeId(), CloudCommandType.CANCEL_COMMAND,
                "{\"commandId\":\"" + command.get().commandId() + "\"}", requestedBy, command.get().commandId(), now);
        getCloudRepository().saveCommand(command.get().withStatus(CloudCommandStatus.CANCELLED, Optional.of("Cancellation requested.")));
        return "command=" + command.get().commandId() + " status=CANCELLED" + System.lineSeparator();
    }

    private String allocatePort(String arguments, Instant now) {
        String[] parts = arguments.split("\\s+");
        if (parts.length < 2) {
            return "usage=cloud ports allocate <workloadId> <publicPort> [internalPort] [TCP|UDP]" + System.lineSeparator();
        }
        UUID workloadId = UUID.fromString(parts[0]);
        Optional<CloudWorkload> workload = getCloudRepository().findWorkload(workloadId);
        if (workload.isEmpty() || workload.get().nodeId().isEmpty()) {
            return "workload=missing-or-unscheduled" + System.lineSeparator();
        }
        int publicPort = Integer.parseInt(parts[1]);
        int internalPort = parts.length >= 3 ? Integer.parseInt(parts[2]) : publicPort;
        PortProtocol protocol = parts.length >= 4 ? PortProtocol.valueOf(parts[3].toUpperCase()) : PortProtocol.TCP;
        PortAllocation allocation = getPortAllocationHandler().allocate(workloadId, workload.get().nodeId().orElseThrow(),
                protocol, publicPort, internalPort, now);
        return "port=" + allocation.allocationId() + " node=" + allocation.nodeId() + " public="
                + allocation.publicPort() + "/" + allocation.protocol() + System.lineSeparator();
    }

    private String runBackup(String arguments, UUID requestedBy, Instant now) {
        String[] parts = arguments.split("\\s+");
        if (parts.length < 3) {
            return "usage=cloud backups run <workloadId> <sourcePath> <destination>" + System.lineSeparator();
        }
        UUID workloadId = UUID.fromString(parts[0]);
        Optional<CloudWorkload> workload = getCloudRepository().findWorkload(workloadId);
        if (workload.isEmpty() || workload.get().nodeId().isEmpty()) {
            return "workload=missing-or-unscheduled" + System.lineSeparator();
        }
        BackupPlan plan = new BackupPlan(UUID.randomUUID(), Optional.of(workloadId), workload.get().nodeId(),
                BackupType.FULL_WORKLOAD, "manual", true, "manual", parts[2], true, Map.of("cli", "true"));
        BackupJob job = getBackupJobCreationHandler().createAndCommand(plan, parts[1], requestedBy, now);
        return "backupJob=" + job.backupId() + System.lineSeparator();
    }

    private String schedulerPlan(String arguments, boolean includeCandidates) {
        String[] parts = arguments.split("\\s+");
        if (parts.length < 2) {
            return "usage=cloud scheduler " + (includeCandidates ? "candidates" : "plan") + " <type> <name> [region]"
                    + System.lineSeparator();
        }
        WorkloadRequest request = defaultWorkloadRequest(CloudWorkloadType.valueOf(parts[0]), parts[1],
                parts.length >= 3 ? Optional.of(parts[2]) : Optional.empty());
        NodeSchedulingDecision decision = getNodeSchedulerHandler().plan(request);
        if (!includeCandidates) {
            return "success=" + decision.success()
                    + " selected=" + decision.selectedNodeId().map(UUID::toString).orElse("none")
                    + " message=\"" + decision.message() + "\""
                    + System.lineSeparator();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("eligible=").append(decision.candidates().size())
                .append(" rejected=").append(decision.rejectedCandidates().size())
                .append(System.lineSeparator());
        decision.candidates().forEach(candidate -> builder.append("candidate=")
                .append(candidate.nodeId())
                .append(" score=")
                .append(candidate.score())
                .append(" reasons=")
                .append(candidate.reasons())
                .append(System.lineSeparator()));
        decision.rejectedCandidates().forEach(candidate -> builder.append("rejected=")
                .append(candidate.nodeId())
                .append(" reasons=")
                .append(candidate.rejectionReasons())
                .append(System.lineSeparator()));
        return builder.toString();
    }

    /**
     * CLI workload creation uses conservative built-in profiles until durable template storage is introduced.
     */
    private WorkloadRequest defaultWorkloadRequest(CloudWorkloadType workloadType, String name, Optional<String> preferredRegion) {
        Set<CloudNodeCapability> capabilities = capabilitiesFor(workloadType);
        Optional<NodeArchitecture> architecture = capabilities.contains(CloudNodeCapability.X86_64)
                ? Optional.of(NodeArchitecture.X86_64)
                : Optional.empty();
        ResourceLimits limits = switch (workloadType) {
            case POSTGRES, QDRANT -> new ResourceLimits(2, 4096, 40, Map.of());
            case REDIS -> new ResourceLimits(1, 1024, 10, Map.of());
            case BOT_TEST_WORKER -> new ResourceLimits(1, 1024, 5, Map.of());
            case SPRING_PANEL, VELOCITY_PROXY -> new ResourceLimits(1, 1024, 5, Map.of());
            case BACKUP_WORKER, FILE_STORAGE -> new ResourceLimits(1, 2048, 50, Map.of());
            default -> new ResourceLimits(2, 2048, 10, Map.of());
        };
        return new WorkloadRequest(workloadType, name, preferredRegion, capabilities, architecture, limits, Map.of(), Set.of(),
                new SchedulingPolicy(preferredRegion, capabilities, architecture, true, true, true, Map.of()),
                Map.of("runtime", "tmux", "sessionName", getCloudControlPlaneFilesystemLayout().workloadSessionName(name)));
    }

    private String renderWorkloadInventory(String label, List<CloudWorkload> workloads) {
        List<String> lines = new ArrayList<>();
        lines.add(label + "=" + workloads.size());
        for (CloudWorkload workload : workloads) {
            lines.add(describeWorkload("workload", workload));
        }
        return joinLines(lines);
    }

    private String renderServerInventory() {
        List<CloudWorkload> workloads = getCloudRepository().findWorkloads();
        List<String> lines = new ArrayList<>();
        lines.add("servers=" + workloads.size());
        for (CloudWorkload workload : workloads) {
            lines.add(describeWorkload("server", workload));
        }
        return joinLines(lines);
    }

    private String renderConsoleInventory() {
        CloudControlPlaneFilesystemLayout layout = getCloudControlPlaneFilesystemLayout();
        List<CloudWorkload> workloads = getCloudRepository().findWorkloads();
        List<String> lines = new ArrayList<>();
        lines.add("consoles=" + (workloads.size() + 1));
        lines.add("console=" + layout.controlPlaneSessionName()
                + " target=control-plane"
                + " attach=" + layout.attachCommand(layout.controlPlaneSessionName()));
        for (CloudWorkload workload : workloads) {
            String sessionName = layout.workloadSessionName(workload);
            lines.add("console=" + sessionName
                    + " target=" + workload.workloadId()
                    + " name=" + workload.name()
                    + " type=" + workload.workloadType()
                    + " attach=" + layout.attachCommand(sessionName));
        }
        return joinLines(lines);
    }

    private String renderKingdomInventory() {
        CloudControlPlaneFilesystemLayout layout = getCloudControlPlaneFilesystemLayout();
        List<String> kingdomIds = layout.kingdomIds();
        List<String> lines = new ArrayList<>();
        lines.add("kingdoms=" + kingdomIds.size());
        for (String kingdomId : kingdomIds) {
            lines.add("kingdom=" + kingdomId + " path=" + layout.kingdomDir(kingdomId));
        }
        return joinLines(lines);
    }

    private String ensureKingdom(String kingdomId) {
        CloudControlPlaneFilesystemLayout layout = getCloudControlPlaneFilesystemLayout();
        Path kingdomPath = layout.kingdomDir(kingdomId);
        boolean existed = Files.exists(kingdomPath);
        layout.ensureKingdom(kingdomId);
        return "kingdom=" + CloudControlPlaneFilesystemLayout.normalizeToken(kingdomId, "kingdom-1")
                + " state=" + (existed ? "EXISTING" : "CREATED")
                + " path=" + kingdomPath
                + System.lineSeparator();
    }

    private String attachConsole(String target) {
        CloudControlPlaneFilesystemLayout layout = getCloudControlPlaneFilesystemLayout();
        if (target == null || target.isBlank()) {
            return "console=missing" + System.lineSeparator();
        }
        String trimmedTarget = target.trim();
        if (trimmedTarget.equalsIgnoreCase("control-plane")
                || trimmedTarget.equalsIgnoreCase(layout.controlPlaneSessionName())
                || trimmedTarget.equalsIgnoreCase("cloud-control-plane")) {
            String sessionName = layout.controlPlaneSessionName();
            return "console=" + sessionName
                    + " target=control-plane"
                    + " attach=" + layout.attachCommand(sessionName)
                    + System.lineSeparator();
        }
        Optional<CloudWorkload> workload = findWorkloadByTarget(trimmedTarget);
        if (workload.isEmpty()) {
            return "console=missing target=" + trimmedTarget + System.lineSeparator();
        }
        String sessionName = layout.workloadSessionName(workload.get());
        return "console=" + sessionName
                + " target=" + workload.get().workloadId()
                + " name=" + workload.get().name()
                + " attach=" + layout.attachCommand(sessionName)
                + System.lineSeparator();
    }

    private Optional<CloudWorkload> findWorkloadByTarget(String target) {
        try {
            UUID workloadId = UUID.fromString(target);
            return getCloudRepository().findWorkload(workloadId);
        } catch (IllegalArgumentException ignored) {
        }
        return getCloudRepository().findWorkloads().stream()
                .filter(workload -> workload.name().equalsIgnoreCase(target)
                        || getCloudControlPlaneFilesystemLayout().workloadSessionName(workload).equalsIgnoreCase(target))
                .findFirst();
    }

    private String describeWorkloadById(String workloadIdText, String label) {
        UUID workloadId = UUID.fromString(workloadIdText);
        return getCloudRepository().findWorkload(workloadId)
                .map(workload -> describeWorkload(label, workload))
                .orElse(label + "=missing" + System.lineSeparator());
    }

    private String describeWorkload(String label, CloudWorkload workload) {
        CloudControlPlaneFilesystemLayout layout = getCloudControlPlaneFilesystemLayout();
        String sessionName = layout.workloadSessionName(workload);
        return label + "=" + workload.workloadId()
                + " name=" + workload.name()
                + " type=" + workload.workloadType()
                + " node=" + workload.nodeId().map(UUID::toString).orElse("unscheduled")
                + " desired=" + workload.desiredState()
                + " actual=" + workload.actualState()
                + " health=" + workload.healthStatus()
                + " session=" + sessionName
                + " attach=" + layout.attachCommand(sessionName)
                + System.lineSeparator();
    }

    private String joinLines(List<String> lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }

    private String registerNode(String arguments, UUID requestedBy, Instant now) {
        String[] parts = arguments.split("\\s+");
        if (parts.length < 1 || parts[0].isBlank()) {
            return "usage=cloud nodes register <joinToken> [hostname] [region]" + System.lineSeparator();
        }
        String joinToken = parts[0].trim();
        String hostname = parts.length >= 2 ? parts[1].trim() : "cloud-control-plane-node-1";
        String region = parts.length >= 3 ? parts[2].trim() : "us-west";
        NodeRegistrationRequest request = new NodeRegistrationRequest(
                joinToken,
                hostname,
                "127.0.0.1",
                "127.0.0.1",
                region,
                "control-plane",
                "tavall",
                "linux",
                NodeArchitecture.X86_64,
                "bootstrap",
                4,
                8192,
                8192,
                128,
                128,
                EnumSet.of(CloudNodeCapability.CAN_RUN_BOTS, CloudNodeCapability.CAN_RUN_MINECRAFT,
                        CloudNodeCapability.CAN_RUN_PROXY, CloudNodeCapability.X86_64),
                Set.of(SupportedRuntime.SYSTEMD),
                Set.of("bootstrap", "control-plane"),
                Map.of("bootstrap", "true", "requestedBy", requestedBy.toString()));
        NodeRegistrationResult result = getNodeRegistrationRequestHandler().register(request, now);
        return "success=" + result.success()
                + " node=" + result.nodeId().map(UUID::toString).orElse("missing")
                + " agent=" + result.agentId().map(UUID::toString).orElse("missing")
                + " message=\"" + result.message() + "\""
                + System.lineSeparator();
    }

    private Set<CloudNodeCapability> capabilitiesFor(CloudWorkloadType workloadType) {
        return switch (workloadType) {
            case MINECRAFT_SERVER, INTERIOR_SERVER -> EnumSet.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64);
            case BOT_TEST_WORKER -> EnumSet.of(CloudNodeCapability.CAN_RUN_BOTS);
            case REDIS -> EnumSet.of(CloudNodeCapability.CAN_RUN_REDIS);
            case POSTGRES -> EnumSet.of(CloudNodeCapability.CAN_RUN_POSTGRES);
            case QDRANT -> EnumSet.of(CloudNodeCapability.CAN_RUN_QDRANT);
            case SPRING_PANEL -> EnumSet.of(CloudNodeCapability.CAN_RUN_WEB_PANEL);
            case VELOCITY_PROXY -> EnumSet.of(CloudNodeCapability.CAN_RUN_PROXY, CloudNodeCapability.X86_64);
            case BACKUP_WORKER -> EnumSet.of(CloudNodeCapability.CAN_RUN_BACKUPS);
            case FILE_STORAGE, CUSTOM -> EnumSet.noneOf(CloudNodeCapability.class);
        };
    }
}
