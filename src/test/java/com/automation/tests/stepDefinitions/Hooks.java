package com.automation.tests.stepDefinitions;

import com.automation.framework.context.UserContext;
import com.automation.framework.driver.Driver;
import com.automation.framework.utils.ScreenshotUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Cucumber lifecycle hooks.
 *
 * - @Before initialises a fresh, thread-isolated browser per scenario (safe for parallel
 *   runs) and clears any leftover UserContext state from a previous scenario on this thread.
 * - @After captures a screenshot and attaches it to the Cucumber report whenever a scenario
 *   fails, reports pass/fail status back to BrowserStack for remote runs, then always tears
 *   the driver and context down so nothing leaks into the next scenario.
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);

    @Before
    public void setUp(Scenario scenario) {
        log.info("========== Starting scenario: {} ==========", scenario.getName());
        UserContext.clear();
        Driver.init();
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                byte[] screenshot = ScreenshotUtil.captureAsBytes();
                scenario.attach(screenshot, "image/png", scenario.getName());
                ScreenshotUtil.captureToFile(scenario.getName());
                Driver.markRemoteStatus(false, "Scenario failed: " + scenario.getName());
                log.error("Scenario FAILED: {}", scenario.getName());
            } else {
                Driver.markRemoteStatus(true, "Scenario passed: " + scenario.getName());
                log.info("Scenario PASSED: {}", scenario.getName());
            }
        } catch (Exception e) {
            log.error("Error while handling scenario teardown/screenshot", e);
        } finally {
            Driver.quit();
            UserContext.clear();
            log.info("========== Finished scenario: {} ==========\n", scenario.getName());
        }
    }
}
