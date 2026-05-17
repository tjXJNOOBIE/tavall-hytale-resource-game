package com.tavall.resourcegame.middleware.trade;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;

public interface ITradeDomainGenerated {
    default TradeRouteRepository getTradeRouteRepository() {
        return DependencyLoaderAccess.findOptionalInstance(TradeRouteRepository.class)
                .orElseGet(() -> registerTradeRouteRepository(new InMemoryTradeRouteRepository()));
    }

    default TradeRouteCreationHandler getTradeRouteCreationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TradeRouteCreationHandler.class)
                .orElseGet(() -> registerTradeRouteCreationHandler(new TradeRouteCreationHandler()));
    }

    default TradeRouteAttackValidationHandler getTradeRouteAttackValidationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TradeRouteAttackValidationHandler.class)
                .orElseGet(() -> registerTradeRouteAttackValidationHandler(new TradeRouteAttackValidationHandler()));
    }

    default TradeRouteProgressTickHandler getTradeRouteProgressTickHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TradeRouteProgressTickHandler.class)
                .orElseGet(() -> registerTradeRouteProgressTickHandler(new TradeRouteProgressTickHandler()));
    }

    default TradeRouteRepository registerTradeRouteRepository(TradeRouteRepository tradeRouteRepository) {
        DependencyLoaderAccess.registerInstance(TradeRouteRepository.class, tradeRouteRepository);
        return tradeRouteRepository;
    }

    default TradeRouteCreationHandler registerTradeRouteCreationHandler(TradeRouteCreationHandler handler) {
        DependencyLoaderAccess.registerInstance(TradeRouteCreationHandler.class, handler);
        return handler;
    }

    default TradeRouteAttackValidationHandler registerTradeRouteAttackValidationHandler(TradeRouteAttackValidationHandler handler) {
        DependencyLoaderAccess.registerInstance(TradeRouteAttackValidationHandler.class, handler);
        return handler;
    }

    default TradeRouteProgressTickHandler registerTradeRouteProgressTickHandler(TradeRouteProgressTickHandler handler) {
        DependencyLoaderAccess.registerInstance(TradeRouteProgressTickHandler.class, handler);
        return handler;
    }
}
