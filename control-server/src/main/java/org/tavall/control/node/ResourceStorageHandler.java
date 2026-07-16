package org.tavall.control.node;

public final class ResourceStorageHandler implements ResourceNodeDomain {
    public ResourceStorageHandler() {
    }

    public ResourceStorageHandler(ResourceNodeRepository resourceNodeRepository) {
        registerResourceNodeRepository(resourceNodeRepository);
    }

    public ResourceNode storeResource(ResourceNodeId nodeId, int amount) {
        ResourceNodeRepository resourceNodeRepository = getResourceNodeRepository();
        ResourceNode node = resourceNodeRepository.findResourceNode(nodeId)
                .orElseThrow(() -> new ResourceNodeValidationException("Resource node was not found."));
        ResourceNode updated = node.withCurrentStoredAmount(node.currentStoredAmount() + Math.max(0, amount));
        return resourceNodeRepository.saveResourceNode(updated);
    }
}
