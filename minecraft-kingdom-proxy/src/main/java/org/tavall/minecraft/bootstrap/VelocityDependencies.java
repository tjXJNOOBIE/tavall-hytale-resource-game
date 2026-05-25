package org.tavall.minecraft.bootstrap;

import org.tavall.api.minecraft.cache.IKingdomSemanticCacheGateway;
import org.tavall.api.minecraft.backend.rank.RankAccess;
import org.tavall.dependency.composition.IDependencyBundle;
import org.tavall.minecraft.commands.IGuild;
import org.tavall.minecraft.commands.IRank;
import org.tavall.minecraft.commands.ISim;
import org.tavall.minecraft.permissions.IVelocityCommandPermissionHandler;
import org.tavall.minecraft.permissions.IVelocityPermissionResolver;

public record VelocityDependencies(
        VelocityProxyConfig velocityProxyConfig,
        IKingdomSemanticCacheGateway kingdomSemanticCacheGateway,
        IVelocityPermissionResolver velocityPermissionResolver,
        IVelocityCommandPermissionHandler velocityCommandPermissionHandler,
        RankAccess rankAccess,
        ISim simCommand,
        IGuild guildCommand,
        IRank rankCommand
) implements IDependencyBundle {
}

