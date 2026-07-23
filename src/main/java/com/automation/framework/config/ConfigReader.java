package com.automation.framework.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads non-secret runtime defaults from a single application.properties
 * file on the test classpath (src/test/resources/application.properties):
 * the target environment identifier, headless-browser behavior, and
 * BrowserStack build/project naming.
 *
 * Any key can be overridden per-run via a matching -Dkey=value system
 * property without editing the file - this is how Maven profiles and the
 * Jenkins pipeline communicate execution mode (env, execution, headless)
 * to the driver/runner infrastructure.
 */
public final class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final String PROPERTIES_FILE = "application.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        load();
    }

    private ConfigReader() {
    }

    private static void load() {
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (is == null) {
                log.warn("{} not found on classpath - relying entirely on -D system properties", PROPERTIES_FILE);
                return;
            }
            PROPERTIES.load(is);
            log.info("Loaded {} ({} keys)", PROPERTIES_FILE, PROPERTIES.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + PROPERTIES_FILE, e);
        }
    }

    /**
     * Returns the config value, checking system properties (-D) first,
     * then application.properties.
     */
    public static String get(String key) {
        String systemOverride = System.getProperty(key);
        if (systemOverride != null && !systemOverride.isBlank()) {
            return systemOverride;
        }
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing config key: '" + key + "'");
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    /** The active environment identifier, e.g. dev | qa | staging | prod. Defaults to "qa". */
    public static String environment() {
        return get("env", "qa");
    }
}
