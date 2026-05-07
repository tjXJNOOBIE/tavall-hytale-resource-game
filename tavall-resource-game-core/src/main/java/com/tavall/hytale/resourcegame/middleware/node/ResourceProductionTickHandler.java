package com.tavall.hytale.resourcegame.middleware.node;

import com.tavall.hytale.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;

public final class ResourceProductionTickHandler {
    private final ResourceNodeRepository resourceNodeRepository;
    private final GuildJobBuffCalculationHandler guildJobBuffCalculationHandler;

    public ResourceProductionTickHandler(ResourceNodeRepository resourceNodeRepository, GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        this.resourceNodeRepository = resourceNodeRepository;
        this.guildJobBuffCalculationHandler = guildJobBuffCalculationHandler;
    }

    public ResourceProductionTickResult runProductionTick(ResourceNodeId nodeId, GuildMemberProfile actor, double guildBuffModifier) {
        ResourceNode node = resourceNodeRepository.findResourceNode(nodeId)
                .orElseThrow(() -> new ResourceNodeValidationException("Resource node was not found."));
        if (node.depleted()) {
            return new ResourceProductionTickResult(node, 0);
        }
        double jobModifier = guildJobBuffCalculationHandler.calculateJobModifier(actor, GuildJobDomain.RESOURCE_PRODUCTION);
        int produced = (int) Math.floor(node.productionRate() * (1.0d + guildBuffModifier + jobModifier));
        ResourceNode updated = node.withCurrentStoredAmount(node.currentStoredAmount() + produced);
        resourceNodeRepository.saveResourceNode(updated);
        return new ResourceProductionTickResult(updated, produced);
    }
}
