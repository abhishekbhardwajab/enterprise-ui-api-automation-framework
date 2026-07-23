package com.automation.framework.utils;

import com.automation.framework.driver.Driver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Captures screenshots on test failure via the Driver facade. Files are
 * saved under reports/screenshots and also returned as raw bytes so they
 * can be attached directly to the Cucumber report (see Hooks.java).
 */
public final class ScreenshotUtil {

    private static final Logger log = LogManager.getLogger(ScreenshotUtil.class);
    private static final Path SCREENSHOT_DIR = Paths.get("reports", "screenshots");

    private ScreenshotUtil() {
    }

    public static byte[] captureAsBytes() {
        return Driver.screenshotBytes();
    }

    /**
     * Saves a screenshot to disk named after the failing scenario, returns the file path.
     */
    public static String captureToFile(String scenarioName) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String safeName = scenarioName.replaceAll("[^a-zA-Z0-9-_]", "_");
            Path destination = SCREENSHOT_DIR.resolve(safeName + "_" + timestamp + ".png");
            Files.write(destination, captureAsBytes());
            log.warn("Screenshot captured on failure: {}", destination);
            return destination.toString();
        } catch (IOException e) {
            log.error("Failed to capture screenshot", e);
            return null;
        }
    }
}
