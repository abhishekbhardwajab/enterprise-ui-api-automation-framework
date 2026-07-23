package com.automation.tests.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Dry-run validation: binds every step in every feature to its step
 * definition without opening a browser or executing anything, to catch
 * missing/ambiguous step definitions fast (e.g. in a pre-commit check).
 *
 * Dry-run mode itself is a Cucumber runtime property rather than an
 * @CucumberOptions attribute in this Cucumber version, so it must be
 * passed alongside selecting this runner:
 *   mvn test -DrunnerClass=CucumberDryRunTest -Dcucumber.execution.dry-run=true
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.automation.tests.stepDefinitions"},
        plugin = {"pretty"}
)
public class CucumberDryRunTest {
}
