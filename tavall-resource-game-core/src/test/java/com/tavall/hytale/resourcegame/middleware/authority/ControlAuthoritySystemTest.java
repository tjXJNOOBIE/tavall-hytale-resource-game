package com.tavall.hytale.resourcegame.middleware.authority;

import com.tavall.hytale.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.hytale.resourcegame.middleware.control.CommandExecutionState;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.hytale.resourcegame.middleware.control.ControlPermission;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlAuthoritySystemTest implements IControlAuthorityDomain {
    @Test
    void c1AuthorityCanRestartAssignedWorkloadOnScopedNode() {
        InMemoryAuthorityRepository authorityRepository = new InMemoryAuthorityRepository();
        registerAuthorityDependencies(authorityRepository);
        UUID principalId = UUID.randomUUID();
        authorityRepository.saveAuthority(ControlAuthority.enabled(
                principalId,
                ControlAuthorityLevel.C1_LOCAL_EXECUTOR,
                new AuthorityScope(AuthorityScopeType.NODE, "oracle-arm-1"),
                Set.of(ControlPermission.WORKLOAD_RESTART),
                principalId,
                Instant.now().toEpochMilli(),
                true,
                false
        ));

        AuthorizationResult result = getControlAuthorizationHandler().authorize(new ControlCommandRequest(
                UUID.randomUUID(),
                principalId,
                ControlPrincipalType.NODE_AGENT,
                CloudCommandType.RESTART_WORKLOAD,
                workloadTarget("hytale-main-1", "oracle-arm-1", "us-west"),
                "{}",
                "recover workload",
                Instant.now().toEpochMilli(),
                false,
                false
        ));

        assertTrue(result.allowed());
    }

    @Test
    void c1AuthorityCannotTouchUnrelatedNode() {
        InMemoryAuthorityRepository authorityRepository = new InMemoryAuthorityRepository();
        registerAuthorityDependencies(authorityRepository);
        UUID principalId = UUID.randomUUID();
        authorityRepository.saveAuthority(ControlAuthority.enabled(
                principalId,
                ControlAuthorityLevel.C1_LOCAL_EXECUTOR,
                new AuthorityScope(AuthorityScopeType.NODE, "oracle-arm-1"),
                Set.of(ControlPermission.WORKLOAD_RESTART),
                principalId,
                Instant.now().toEpochMilli(),
                true,
                false
        ));

        AuthorizationResult result = getControlAuthorizationHandler().authorize(new ControlCommandRequest(
                UUID.randomUUID(),
                principalId,
                ControlPrincipalType.NODE_AGENT,
                CloudCommandType.RESTART_WORKLOAD,
                workloadTarget("hytale-main-2", "oracle-arm-2", "us-west"),
                "{}",
                "wrong node",
                Instant.now().toEpochMilli(),
                false,
                false
        ));

        assertFalse(result.allowed());
        assertTrue(result.message().contains("No active authority"));
    }

    @Test
    void c6AdvisoryDoesNotSatisfyDestructiveHumanPolicy() {
        InMemoryAuthorityRepository authorityRepository = new InMemoryAuthorityRepository();
        registerAuthorityDependencies(authorityRepository);
        UUID principalId = UUID.randomUUID();
        authorityRepository.saveAuthority(ControlAuthority.enabled(
                principalId,
                ControlAuthorityLevel.C6_INTELLIGENCE_AUTHORITY,
                AuthorityScope.global(),
                EnumSet.of(ControlPermission.CLUSTER_DESTROY, ControlPermission.AI_RECOMMEND),
                principalId,
                Instant.now().toEpochMilli(),
                false,
                false
        ));

        AuthorizationResult result = getControlAuthorizationHandler().authorize(new ControlCommandRequest(
                UUID.randomUUID(),
                principalId,
                ControlPrincipalType.AI_AGENT,
                CloudCommandType.CLUSTER_DESTROY,
                ResourceTarget.of(AuthorityScopeType.CLUSTER, "minecraft-kingdomfactions-prod"),
                "{}",
                "simulate cluster destroy",
                Instant.now().toEpochMilli(),
                true,
                false
        ));

        assertFalse(result.allowed());
    }

    @Test
    void c6AdvisoryCanRecommendAction() {
        InMemoryAuthorityRepository authorityRepository = new InMemoryAuthorityRepository();
        registerAuthorityDependencies(authorityRepository);
        UUID principalId = UUID.randomUUID();
        authorityRepository.saveAuthority(ControlAuthority.enabled(
                principalId,
                ControlAuthorityLevel.C6_INTELLIGENCE_AUTHORITY,
                AuthorityScope.global(),
                Set.of(ControlPermission.AI_RECOMMEND),
                principalId,
                Instant.now().toEpochMilli(),
                false,
                false
        ));

        AuthorizationResult result = getControlAuthorizationHandler().authorize(new ControlCommandRequest(
                UUID.randomUUID(),
                principalId,
                ControlPrincipalType.AI_AGENT,
                CloudCommandType.AI_RECOMMEND_ACTION,
                ResourceTarget.global(),
                "{}",
                "recommend scaling",
                Instant.now().toEpochMilli(),
                false,
                false
        ));

        assertTrue(result.allowed());
    }

    @Test
    void deleteWorkloadRequiresApproval() {
        InMemoryAuthorityRepository authorityRepository = new InMemoryAuthorityRepository();
        registerAuthorityDependencies(authorityRepository);
        UUID principalId = UUID.randomUUID();
        authorityRepository.saveAuthority(ControlAuthority.enabled(
                principalId,
                ControlAuthorityLevel.C2_REGIONAL_OPERATOR,
                new AuthorityScope(AuthorityScopeType.REGION, "us-west"),
                Set.of(ControlPermission.WORKLOAD_DELETE),
                principalId,
                Instant.now().toEpochMilli(),
                false,
                false
        ));

        AuthorizationResult missingApproval = getControlAuthorizationHandler().authorize(new ControlCommandRequest(
                UUID.randomUUID(),
                principalId,
                ControlPrincipalType.HUMAN_ADMIN,
                CloudCommandType.DELETE_WORKLOAD,
                workloadTarget("minecraft-1", "node-1", "us-west"),
                "{}",
                "delete stale test workload",
                Instant.now().toEpochMilli(),
                false,
                false
        ));
        AuthorizationResult approved = getControlAuthorizationHandler().authorize(new ControlCommandRequest(
                UUID.randomUUID(),
                principalId,
                ControlPrincipalType.HUMAN_ADMIN,
                CloudCommandType.DELETE_WORKLOAD,
                workloadTarget("minecraft-1", "node-1", "us-west"),
                "{}",
                "delete stale test workload",
                Instant.now().toEpochMilli(),
                true,
                false
        ));

        assertFalse(missingApproval.allowed());
        assertTrue(missingApproval.message().contains("requires approval"));
        assertTrue(approved.allowed());
    }

    @Test
    void frontendIngressCommandsPassThroughSeededSystemAuthority() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(
                FrontendCommandEnvelope.command(
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "ResourceProxyBot",
                        "ResourceProxyBot",
                        "/kd clock state kingdom-1",
                        UUID.randomUUID().toString(),
                        Map.of("platformInstanceId", "kingdom-1-minecraft-primary")
                ),
                Instant.now()
        );

        assertEquals(CommandExecutionState.COMPLETED.name(), result.controlCommandState());
        assertFalse(runtime.authorizationAuditRepository().findRecent(10).isEmpty());
    }

    private void registerAuthorityDependencies(InMemoryAuthorityRepository authorityRepository) {
        DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(AuthorityRepository.class, authorityRepository);
        DependencyLoaderAccess.registerInstance(PermissionPolicyRepository.class, new InMemoryPermissionPolicyRepository());
        DependencyLoaderAccess.registerInstance(AuthorizationAuditRepository.class, new InMemoryAuthorizationAuditRepository());
        DependencyLoaderAccess.registerInstance(com.tavall.hytale.resourcegame.middleware.control.ControlCommandRegistry.class,
                new com.tavall.hytale.resourcegame.middleware.control.ControlCommandRegistry());
        new ControlAuthorityDependencyModule().registerDependencies();
    }

    private ResourceTarget workloadTarget(String workloadId, String nodeId, String region) {
        return new ResourceTarget(
                AuthorityScopeType.WORKLOAD,
                workloadId,
                Map.of(
                        AuthorityScopeType.WORKLOAD, workloadId,
                        AuthorityScopeType.NODE, nodeId,
                        AuthorityScopeType.REGION, region,
                        AuthorityScopeType.ENVIRONMENT, "dev"
                )
        );
    }
}
