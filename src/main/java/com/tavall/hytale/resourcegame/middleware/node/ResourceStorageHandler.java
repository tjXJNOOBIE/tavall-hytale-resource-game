package com.tavall.hytale.resourcegame.middleware.node;

public final class ResourceStorageHandler {
    private final ResourceNodeRepository resourceNodeRepository;

    public ResourceStorageHandler(ResourceNodeRepository resourceNodeRepository) {
        this.resourceNodeRepository = resourceNodeRepository;
    }

    public ResourceNode storeResource(ResourceNodeId nodeId, int amount) {
        ResourceNode node = resourceNodeRepository.findResourceNode(nodeId)
                .orElseThrow(() -> new ResourceNodeValidationException("Resource node was not found."));
        ResourceNode updated = node.withCurrentStoredAmount(node.currentStoredAmount() + Math.max(0, amount));
        return resourceNodeRepository.saveResourceNode(updated);
    }
}
