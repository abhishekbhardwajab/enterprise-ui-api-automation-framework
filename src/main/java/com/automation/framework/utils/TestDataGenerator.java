package com.automation.framework.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates unique, disposable test data for the signup flow so the
 * scenario can register a brand-new account on every run (and every
 * parallel thread) without colliding on "email already exists" errors.
 */
public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    public static String uniqueEmail() {
        return "qe.automation." + System.currentTimeMillis() + "."
                + ThreadLocalRandom.current().nextInt(1000, 9999) + "@mailinator.com";
    }

    public static String randomMobileNumber() {
        return "555" + ThreadLocalRandom.current().nextInt(1000000, 9999999);
    }

    public static String randomZipcode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
    }
}
