package org.tavall.minecraft.permissions;

import org.tavall.api.minecraft.cache.IKingdomSemanticCacheGateway;
import org.tavall.api.minecraft.backend.rank.RankAccess;
import org.tavall.dependency.composition.IDependencyBundleAccess;
import org.tavall.minecraft.bootstrap.VelocityDependencies;
import org.tavall.minecraft.bootstrap.VelocityProxyConfig;
import org.tavall.minecraft.commands.IGuild;
import org.tavall.minecraft.commands.IRank;
import org.tavall.minecraft.commands.ISim;

public interface IVelocityDependencyAccess extends IDependencyBundleAccess<VelocityDependencies> {
    default VelocityProxyConfig getVelocityProxyConfig() {
        return dependencies().velocityProxyConfig();
    }

    default IKingdomSemanticCacheGateway getKingdomCacheGateway() {
        return dependencies().kingdomSemanticCacheGateway();
    }

    default IVelocityPermissionResolver getVelocityPermissionResolver() {
        return dependencies().velocityPermissionResolver();
    }

    default IVelocityCommandPermissionHandler getVelocityCommandPermissionHandler() {
        return dependencies().velocityCommandPermissionHandler();
    }

    default RankAccess getRankAccess() {
        return dependencies().rankAccess();
    }

    default ISim getSimCommand() {
        return dependencies().simCommand();
    }

    default IGuild getGuildCommand() {
        return dependencies().guildCommand();
    }

    default IRank getRankCommand() {
        return dependencies().rankCommand();
    }
}

