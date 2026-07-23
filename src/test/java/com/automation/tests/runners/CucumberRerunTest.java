package com.automation.tests.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Reruns only the scenarios written to target/rerun.txt by the previous
 * run's "rerun" plugin. This is this framework's retry mechanism, applied
 * at the run level (Jenkins runs this as its second stage) rather than
 * per-scenario:
 *   mvn test -PfailedTests
 *
 * If target/rerun.txt is empty (nothing failed), Cucumber runs zero
 * scenarios and this stage is a no-op.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "@target/rerun.txt",
        glue = {"com.automation.tests.stepDefinitions"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/rerun.html",
                "json:target/cucumber-reports/rerun.json",
                "rerun:target/rerun.txt"
        }
)
public class CucumberRerunTest {
}
