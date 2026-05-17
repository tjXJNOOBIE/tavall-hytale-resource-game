package com.tavall.resourcegame.middleware.identity;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;

public interface IIdentityDomainGenerated {
    default UniversalPlayerAccountRepository getUniversalPlayerAccountRepository() {
        return DependencyLoaderAccess.findOptionalInstance(UniversalPlayerAccountRepository.class)
                .orElseGet(() -> registerIdentityRepository(new InMemoryIdentityRepository()));
    }

    default AuthIdentityRepository getAuthIdentityRepository() {
        return DependencyLoaderAccess.findOptionalInstance(AuthIdentityRepository.class)
                .orElseGet(() -> registerIdentityRepository(new InMemoryIdentityRepository()));
    }

    default PlatformAccountBindingRepository getPlatformAccountBindingRepository() {
        return DependencyLoaderAccess.findOptionalInstance(PlatformAccountBindingRepository.class)
                .orElseGet(() -> registerIdentityRepository(new InMemoryIdentityRepository()));
    }

    default PasswordlessEmailChallengeRepository getPasswordlessEmailChallengeRepository() {
        return DependencyLoaderAccess.findOptionalInstance(PasswordlessEmailChallengeRepository.class)
                .orElseGet(() -> registerIdentityRepository(new InMemoryIdentityRepository()));
    }

    default PlatformLinkChallengeRepository getPlatformLinkChallengeRepository() {
        return DependencyLoaderAccess.findOptionalInstance(PlatformLinkChallengeRepository.class)
                .orElseGet(() -> registerIdentityRepository(new InMemoryIdentityRepository()));
    }

    default PasswordlessEmailAuthHandler getPasswordlessEmailAuthHandler() {
        return DependencyLoaderAccess.findOptionalInstance(PasswordlessEmailAuthHandler.class)
                .orElseGet(() -> registerPasswordlessEmailAuthHandler(new PasswordlessEmailAuthHandler()));
    }

    default PlatformAccountLinkHandler getPlatformAccountLinkHandler() {
        return DependencyLoaderAccess.findOptionalInstance(PlatformAccountLinkHandler.class)
                .orElseGet(() -> registerPlatformAccountLinkHandler(new PlatformAccountLinkHandler()));
    }

    default ProviderAuthLinkHandler getProviderAuthLinkHandler() {
        return DependencyLoaderAccess.findOptionalInstance(ProviderAuthLinkHandler.class)
                .orElseGet(() -> registerProviderAuthLinkHandler(new ProviderAuthLinkHandler()));
    }

    default UniversalPlayerAccountHandler getUniversalPlayerAccountHandler() {
        return DependencyLoaderAccess.findOptionalInstance(UniversalPlayerAccountHandler.class)
                .orElseGet(() -> registerUniversalPlayerAccountHandler(new UniversalPlayerAccountHandler()));
    }

    default InMemoryIdentityRepository registerIdentityRepository(InMemoryIdentityRepository repository) {
        DependencyLoaderAccess.registerInstance(UniversalPlayerAccountRepository.class, repository);
        DependencyLoaderAccess.registerInstance(AuthIdentityRepository.class, repository);
        DependencyLoaderAccess.registerInstance(PlatformAccountBindingRepository.class, repository);
        DependencyLoaderAccess.registerInstance(PasswordlessEmailChallengeRepository.class, repository);
        DependencyLoaderAccess.registerInstance(PlatformLinkChallengeRepository.class, repository);
        return repository;
    }

    default UniversalPlayerAccountRepository registerUniversalPlayerAccountRepository(UniversalPlayerAccountRepository repository) {
        DependencyLoaderAccess.registerInstance(UniversalPlayerAccountRepository.class, repository);
        return repository;
    }

    default AuthIdentityRepository registerAuthIdentityRepository(AuthIdentityRepository repository) {
        DependencyLoaderAccess.registerInstance(AuthIdentityRepository.class, repository);
        return repository;
    }

    default PlatformAccountBindingRepository registerPlatformAccountBindingRepository(PlatformAccountBindingRepository repository) {
        DependencyLoaderAccess.registerInstance(PlatformAccountBindingRepository.class, repository);
        return repository;
    }

    default PasswordlessEmailChallengeRepository registerPasswordlessEmailChallengeRepository(PasswordlessEmailChallengeRepository repository) {
        DependencyLoaderAccess.registerInstance(PasswordlessEmailChallengeRepository.class, repository);
        return repository;
    }

    default PlatformLinkChallengeRepository registerPlatformLinkChallengeRepository(PlatformLinkChallengeRepository repository) {
        DependencyLoaderAccess.registerInstance(PlatformLinkChallengeRepository.class, repository);
        return repository;
    }

    default PasswordlessEmailAuthHandler registerPasswordlessEmailAuthHandler(PasswordlessEmailAuthHandler handler) {
        DependencyLoaderAccess.registerInstance(PasswordlessEmailAuthHandler.class, handler);
        return handler;
    }

    default PlatformAccountLinkHandler registerPlatformAccountLinkHandler(PlatformAccountLinkHandler handler) {
        DependencyLoaderAccess.registerInstance(PlatformAccountLinkHandler.class, handler);
        return handler;
    }

    default ProviderAuthLinkHandler registerProviderAuthLinkHandler(ProviderAuthLinkHandler handler) {
        DependencyLoaderAccess.registerInstance(ProviderAuthLinkHandler.class, handler);
        return handler;
    }

    default UniversalPlayerAccountHandler registerUniversalPlayerAccountHandler(UniversalPlayerAccountHandler handler) {
        DependencyLoaderAccess.registerInstance(UniversalPlayerAccountHandler.class, handler);
        return handler;
    }
}
