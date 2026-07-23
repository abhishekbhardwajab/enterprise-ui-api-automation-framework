package com.automation.tests.listeners;

import com.automation.framework.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to {@code retry.count} times (config-driven,
 * default 2) before letting it be reported as a genuine failure.
 *
 * This absorbs transient flakiness (slow page loads, momentary network
 * blips) without masking real, reproducible failures - a test only passes
 * on retry if the underlying assertion actually succeeds.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private int attempt = 0;
    private final int maxRetries = ConfigReader.getInt("retry.count", 2);

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < maxRetries) {
            attempt++;
            log.warn("Retrying test [{}] - attempt {}/{}", result.getName(), attempt, maxRetries);
            return true;
        }
        return false;
    }
}
