package com.automation.tests.plugins;

import com.automation.framework.utils.CsvUtils;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Cucumber report plugin that appends one CSV row per finished
 * scenario (timestamp, name, status, duration) - the "CSV report details"
 * output alongside the built-in HTML/JSON/rerun-file reports.
 *
 * Registered in a runner's @CucumberOptions plugin list with an output
 * path suffix, e.g.:
 *   "com.automation.tests.plugins.CsvReportListener:target/cucumber-reports/scenario-results.csv"
 * Cucumber supplies that path to this constructor automatically.
 */
public class CsvReportListener implements ConcurrentEventListener {

    private final Path outputPath;

    public CsvReportListener(File outputFile) {
        this.outputPath = outputFile.toPath();
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("timestamp", Instant.now().toString());
        row.put("scenario", event.getTestCase().getName());
        row.put("status", event.getResult().getStatus().name());
        row.put("durationMillis", String.valueOf(event.getResult().getDuration().toMillis()));
        CsvUtils.appendRow(outputPath, row);
    }
}
