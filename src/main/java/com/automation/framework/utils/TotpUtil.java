package com.automation.framework.utils;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

/**
 * Time-based one-time-password support for MFA/SSO enrollment and login
 * flows: generating an enrollment secret and producing the current valid
 * code for it, the same way an authenticator app would.
 *
 * Not currently wired into any feature in this repo - automationexercise.com
 * has no MFA step - but kept as a reusable capability for applications
 * (e.g. an SSO-backed admin console) that require TOTP as part of login.
 */
public final class TotpUtil {

    private static final TimeProvider TIME_PROVIDER = new SystemTimeProvider();
    private static final CodeGenerator CODE_GENERATOR = new DefaultCodeGenerator(HashingAlgorithm.SHA1);

    private TotpUtil() {
    }

    /** Generates a new base32 TOTP enrollment secret. */
    public static String generateSecret() {
        return new DefaultSecretGenerator().generate();
    }

    /** Produces the currently valid 6-digit code for the given enrollment secret. */
    public static String currentCode(String secret) {
        try {
            long currentBucket = Math.floorDiv(TIME_PROVIDER.getTime(), 30);
            return CODE_GENERATOR.generate(secret, currentBucket);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate TOTP code for provided secret", e);
        }
    }

    /** Verifies a user-entered code against the secret, allowing for normal clock drift. */
    public static boolean isValidCode(String secret, String code) {
        return new DefaultCodeVerifier(CODE_GENERATOR, TIME_PROVIDER).isValidCode(secret, code);
    }
}
