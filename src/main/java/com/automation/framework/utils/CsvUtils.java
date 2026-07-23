package com.automation.framework.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Header-based CSV read/write support.
 *
 * Readers load a classpath CSV resource into a list of column-name -> value
 * row maps (used by UserRepository to build seed accounts). Writers append
 * generated records to a file on disk, creating the header row on first
 * write if the file doesn't exist yet.
 */
public final class CsvUtils {

    private static final Logger log = LogManager.getLogger(CsvUtils.class);

    private CsvUtils() {
    }

    /**
     * Reads a header-based CSV file from the classpath (e.g. "data/users.csv")
     * and returns one Map<columnName, value> per data row.
     */
    public static List<Map<String, String>> readRows(String classpathResource) {
        List<Map<String, String>> rows = new ArrayList<>();
        try (InputStream is = CsvUtils.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (is == null) {
                log.warn("CSV resource not found on classpath: {}", classpathResource);
                return rows;
            }
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build();
            try (CSVParser parser = new CSVParser(reader, format)) {
                for (CSVRecord record : parser) {
                    rows.add(new LinkedHashMap<>(record.toMap()));
                }
            }
            log.info("Loaded {} row(s) from {}", rows.size(), classpathResource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV resource: " + classpathResource, e);
        }
        return rows;
    }

    /**
     * Appends a single record to a CSV file on disk, writing the header row
     * (derived from the map's key order) first if the file doesn't already exist.
     * Intended for generated-credential output, which must stay untracked -
     * see .gitignore.
     */
    public static synchronized void appendRow(Path filePath, Map<String, String> row) {
        try {
            boolean isNewFile = !Files.exists(filePath);
            if (isNewFile && filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader(row.keySet().toArray(new String[0]))
                    .setSkipHeaderRecord(!isNewFile)
                    .build();

            try (Writer writer = Files.newBufferedWriter(filePath,
                    isNewFile ? java.nio.file.StandardOpenOption.CREATE : java.nio.file.StandardOpenOption.APPEND);
                 CSVPrinter printer = new CSVPrinter(writer, format)) {
                printer.printRecord(row.values());
            }
            log.info("Appended generated record to {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to append CSV row to: " + filePath, e);
        }
    }
}
