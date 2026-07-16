package org.tavall.control.security;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

import java.security.SecureRandom;

public interface SecurityDomain {
    default ProtectedSecretCodec getProtectedSecretCodec() {
        return DependencyLoaderAccess.findOptionalInstance(ProtectedSecretCodec.class)
                .orElseGet(() -> registerProtectedSecretCodec(new IsolatedSecretCodec()));
    }

    default SecureRandom getSecureRandom() {
        return DependencyLoaderAccess.findOptionalInstance(SecureRandom.class)
                .orElseGet(() -> registerSecureRandom(new SecureRandom()));
    }

    default TokenHasher getTokenHasher() {
        return DependencyLoaderAccess.findOptionalInstance(TokenHasher.class)
                .orElseGet(() -> registerTokenHasher(new Sha256TokenHasher("")));
    }

    default TotpCodeGenerator getTotpCodeGenerator() {
        return DependencyLoaderAccess.findOptionalInstance(TotpCodeGenerator.class)
                .orElseGet(() -> registerTotpCodeGenerator(new TotpCodeGenerator()));
    }

    default TwoFactorRepository getTwoFactorRepository() {
        return DependencyLoaderAccess.findInstance(TwoFactorRepository.class);
    }

    default ProtectedSecretCodec registerProtectedSecretCodec(ProtectedSecretCodec protectedSecretCodec) {
        DependencyLoaderAccess.registerInstance(ProtectedSecretCodec.class, protectedSecretCodec);
        return protectedSecretCodec;
    }

    default SecureRandom registerSecureRandom(SecureRandom secureRandom) {
        DependencyLoaderAccess.registerInstance(SecureRandom.class, secureRandom);
        return secureRandom;
    }

    default TokenHasher registerTokenHasher(TokenHasher tokenHasher) {
        DependencyLoaderAccess.registerInstance(TokenHasher.class, tokenHasher);
        return tokenHasher;
    }

    default TotpCodeGenerator registerTotpCodeGenerator(TotpCodeGenerator totpCodeGenerator) {
        DependencyLoaderAccess.registerInstance(TotpCodeGenerator.class, totpCodeGenerator);
        return totpCodeGenerator;
    }

    default void registerTwoFactorRepository(TwoFactorRepository twoFactorRepository) {
        DependencyLoaderAccess.registerInstance(TwoFactorRepository.class, twoFactorRepository);
    }
}
