package com.tavall.resourcegame.middleware.identity;

import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryIdentityRepository implements UniversalPlayerAccountRepository,
        AuthIdentityRepository,
        PlatformAccountBindingRepository,
        PasswordlessEmailChallengeRepository,
        PlatformLinkChallengeRepository {
    private final Map<UniversalPlayerId, UniversalPlayerAccount> accountsById = new ConcurrentHashMap<>();
    private final Map<String, AuthIdentity> authIdentitiesByProviderSubject = new ConcurrentHashMap<>();
    private final Map<String, PlatformAccountBinding> bindingsByPlatformAccount = new ConcurrentHashMap<>();
    private final Map<UUID, PasswordlessEmailChallenge> passwordlessChallengesById = new ConcurrentHashMap<>();
    private final Map<UUID, PlatformLinkChallenge> platformLinkChallengesById = new ConcurrentHashMap<>();

    @Override
    public UniversalPlayerAccount saveAccount(UniversalPlayerAccount account) {
        accountsById.put(account.universalPlayerId(), account);
        return account;
    }

    @Override
    public Optional<UniversalPlayerAccount> findAccount(UniversalPlayerId universalPlayerId) {
        return Optional.ofNullable(accountsById.get(universalPlayerId));
    }

    @Override
    public AuthIdentity saveAuthIdentity(AuthIdentity authIdentity) {
        String key = authIdentityKey(authIdentity.provider(), authIdentity.providerSubject());
        AuthIdentity existing = authIdentitiesByProviderSubject.putIfAbsent(key, authIdentity);
        if (existing != null && !existing.universalPlayerId().equals(authIdentity.universalPlayerId())) {
            throw new IdentityOperationException("Auth identity is already linked to another universal account.");
        }
        authIdentitiesByProviderSubject.put(key, authIdentity);
        return authIdentity;
    }

    @Override
    public Optional<AuthIdentity> findAuthIdentity(AuthProvider provider, String providerSubject) {
        return Optional.ofNullable(authIdentitiesByProviderSubject.get(authIdentityKey(provider, providerSubject)));
    }

    @Override
    public PlatformAccountBinding savePlatformBinding(PlatformAccountBinding binding) {
        String key = platformBindingKey(binding.platform(), binding.platformAccountId());
        PlatformAccountBinding existing = bindingsByPlatformAccount.putIfAbsent(key, binding);
        if (existing != null && !existing.universalPlayerId().equals(binding.universalPlayerId())) {
            throw new IdentityOperationException("Platform account is already linked to another universal account.");
        }
        bindingsByPlatformAccount.put(key, binding);
        return binding;
    }

    @Override
    public Optional<PlatformAccountBinding> findPlatformBinding(GamePlatform platform, String platformAccountId) {
        return Optional.ofNullable(bindingsByPlatformAccount.get(platformBindingKey(platform, platformAccountId)));
    }

    @Override
    public List<PlatformAccountBinding> findPlatformBindings(UniversalPlayerId universalPlayerId) {
        List<PlatformAccountBinding> bindings = new ArrayList<>();
        for (PlatformAccountBinding binding : bindingsByPlatformAccount.values()) {
            if (binding.universalPlayerId().equals(universalPlayerId)) {
                bindings.add(binding);
            }
        }
        return List.copyOf(bindings);
    }

    @Override
    public PasswordlessEmailChallenge savePasswordlessChallenge(PasswordlessEmailChallenge challenge) {
        passwordlessChallengesById.put(challenge.challengeId(), challenge);
        return challenge;
    }

    @Override
    public Optional<PasswordlessEmailChallenge> findPasswordlessChallenge(UUID challengeId) {
        return Optional.ofNullable(passwordlessChallengesById.get(challengeId));
    }

    @Override
    public PlatformLinkChallenge savePlatformLinkChallenge(PlatformLinkChallenge challenge) {
        platformLinkChallengesById.put(challenge.challengeId(), challenge);
        return challenge;
    }

    @Override
    public Optional<PlatformLinkChallenge> findPlatformLinkChallenge(UUID challengeId) {
        return Optional.ofNullable(platformLinkChallengesById.get(challengeId));
    }

    private String authIdentityKey(AuthProvider provider, String subject) {
        return provider.name() + ":" + subject;
    }

    private String platformBindingKey(GamePlatform platform, String platformAccountId) {
        return platform.name() + ":" + platformAccountId;
    }
}
