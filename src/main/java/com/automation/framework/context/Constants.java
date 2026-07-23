package com.automation.framework.context;

import com.automation.framework.config.ConfigReader;

import java.util.Map;

/**
 * Fixed, non-secret values that aren't runtime-overridable the way
 * application.properties keys are: primarily the environment-identifier ->
 * base-URL mapping, since automationexercise.com is a single public site
 * used to stand in for what would otherwise be distinct dev/qa/staging/prod
 * deployments.
 */
public final class Constants {

    private static final Map<String, String> ENVIRONMENT_URLS = Map.of(
            "dev", "https://automationexercise.com",
            "qa", "https://automationexercise.com",
            "staging", "https://automationexercise.com",
            "prod", "https://automationexercise.com"
    );

    public static final String USERS_CSV_PATH = "data/users.csv";
    public static final int IMPLICIT_WAIT_SECONDS = ConfigReader.getInt("implicit.wait.seconds", 5);
    public static final int EXPLICIT_WAIT_SECONDS = ConfigReader.getInt("explicit.wait.seconds", 10);

    private Constants() {
    }

    /** Base URL for the currently configured environment (see ConfigReader#environment). */
    public static String baseUrl() {
        String env = ConfigReader.environment();
        String url = ENVIRONMENT_URLS.get(env);
        if (url == null) {
            throw new IllegalStateException("No base URL configured for environment: " + env);
        }
        return url;
    }
}
