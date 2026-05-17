package com.tavall.resourcegame.middleware.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;

public final class TotpCodeGenerator {
    private static final int DIGITS = 6;
    private static final long STEP_SECONDS = 30L;

    public String generateCode(String base64Secret, Instant now) {
        long counter = now.getEpochSecond() / STEP_SECONDS;
        return generateCode(base64Secret, counter);
    }

    public boolean verifyCode(String base64Secret, String code, Instant now) {
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long counter = now.getEpochSecond() / STEP_SECONDS;
        for (long candidate = counter - 1; candidate <= counter + 1; candidate++) {
            if (generateCode(base64Secret, candidate).equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateCode(String base64Secret, long counter) {
        try {
            byte[] secret = Base64.getDecoder().decode(base64Secret);
            byte[] counterBytes = ByteBuffer.allocate(Long.BYTES).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(counterBytes);
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            int otp = binary % (int) Math.pow(10, DIGITS);
            return String.format("%06d", otp);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to generate TOTP code.", ex);
        }
    }
}
