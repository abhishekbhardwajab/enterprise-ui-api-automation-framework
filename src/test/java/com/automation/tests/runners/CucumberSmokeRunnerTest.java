package com.automation.tests.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Targeted local execution: just the @smoke-tagged scenarios, for a fast
 * sanity check while iterating locally without running the full suite:
 *   mvn test -DrunnerClass=CucumberSmokeRunnerTest
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.automation.tests.stepDefinitions"},
        tags = "@smoke",
        plugin = {"pretty", "html:target/cucumber-reports/smoke.html"}
)
public class CucumberSmokeRunnerTest {
}
