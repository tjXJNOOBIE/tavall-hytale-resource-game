package org.tavall.control.node;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.guild.IGuildDomain;

public interface IResourceNodeDomain extends IGuildDomain {
    default ResourceNodeRepository getResourceNodeRepository() {
        return DependencyLoaderAccess.findOptionalInstance(ResourceNodeRepository.class)
                .orElseGet(() -> registerResourceNodeRepository(new InMemoryResourceNodeRepository()));
    }

    default ResourceNodeCreationHandler getResourceNodeCreationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(ResourceNodeCreationHandler.class)
                .orElseGet(() -> registerResourceNodeCreationHandler(new ResourceNodeCreationHandler()));
    }

    default ResourceStorageHandler getResourceStorageHandler() {
        return DependencyLoaderAccess.findOptionalInstance(ResourceStorageHandler.class)
                .orElseGet(() -> registerResourceStorageHandler(new ResourceStorageHandler()));
    }

    default ResourceProductionTickHandler getResourceProductionTickHandler() {
        return DependencyLoaderAccess.findOptionalInstance(ResourceProductionTickHandler.class)
                .orElseGet(() -> registerResourceProductionTickHandler(new ResourceProductionTickHandler()));
    }

    default ResourceNodeRepository registerResourceNodeRepository(ResourceNodeRepository resourceNodeRepository) {
        DependencyLoaderAccess.registerInstance(ResourceNodeRepository.class, resourceNodeRepository);
        return resourceNodeRepository;
    }

    default ResourceNodeCreationHandler registerResourceNodeCreationHandler(ResourceNodeCreationHandler handler) {
        DependencyLoaderAccess.registerInstance(ResourceNodeCreationHandler.class, handler);
        return handler;
    }

    default ResourceStorageHandler registerResourceStorageHandler(ResourceStorageHandler handler) {
        DependencyLoaderAccess.registerInstance(ResourceStorageHandler.class, handler);
        return handler;
    }

    default ResourceProductionTickHandler registerResourceProductionTickHandler(ResourceProductionTickHandler handler) {
        DependencyLoaderAccess.registerInstance(ResourceProductionTickHandler.class, handler);
        return handler;
    }
}
