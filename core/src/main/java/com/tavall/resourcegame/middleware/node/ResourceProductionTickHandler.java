package com.tavall.resourcegame.middleware.node;

import com.tavall.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.resourcegame.middleware.guild.GuildMemberProfile;

public final class ResourceProductionTickHandler implements IResourceNodeDomain {
    public ResourceProductionTickHandler() {
    }

    public ResourceProductionTickHandler(ResourceNodeRepository resourceNodeRepository, GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        registerResourceNodeRepository(resourceNodeRepository);
        registerGuildJobBuffCalculationHandler(guildJobBuffCalculationHandler);
    }

    public ResourceProductionTickResult runProductionTick(ResourceNodeId nodeId, GuildMemberProfile actor, double guildBuffModifier) {
        ResourceNodeRepository resourceNodeRepository = getResourceNodeRepository();
        ResourceNode node = resourceNodeRepository.findResourceNode(nodeId)
                .orElseThrow(() -> new ResourceNodeValidationException("Resource node was not found."));
        if (node.depleted()) {
            return new ResourceProductionTickResult(node, 0);
        }
        double jobModifier = getGuildJobBuffCalculationHandler().calculateJobModifier(actor, GuildJobDomain.RESOURCE_PRODUCTION);
        int produced = (int) Math.floor(node.productionRate() * (1.0d + guildBuffModifier + jobModifier));
        ResourceNode updated = node.withCurrentStoredAmount(node.currentStoredAmount() + produced);
        resourceNodeRepository.saveResourceNode(updated);
        return new ResourceProductionTickResult(updated, produced);
    }
}
