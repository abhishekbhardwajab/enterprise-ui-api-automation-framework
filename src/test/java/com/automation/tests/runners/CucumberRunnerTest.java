package com.automation.tests.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Default runner: the normal full-suite entry point selected by
 * Maven Surefire's runnerClass property (see pom.xml) for plain "mvn test".
 *
 * Tag filter excludes:
 *   not @wip              - work in progress, not ready to run
 *   not @requires-account - needs a real, pre-registered site login (see login.feature)
 * Opt into the account-gated scenario explicitly:
 *   mvn test -Dcucumber.filter.tags="@requires-account"
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.automation.tests.stepDefinitions"},
        tags = "not @wip and not @requires-account",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "rerun:target/rerun.txt",
                "com.automation.tests.plugins.CsvReportListener:target/cucumber-reports/scenario-results.csv"
        }
)
public class CucumberRunnerTest {
}
