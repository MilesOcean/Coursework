package util;

import interfaces.CsvPersistable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Generic CSV file reader/writer.
 *
 * Design decisions (from design.md §2.5, §5):
 * - Uses UTF-8 encoding throughout.
 * - First line of every file is a header (skipped on read, written on write).
 * - readCsv() catches parse errors per-row so one bad line doesn't lose the whole file.
 * - Empty lines are skipped silently.
 */
public final class CsvUtil {

    private CsvUtil() {}

    /**
     * Reads a CSV file and returns a list of parsed objects.
     *
     * @param path     file path relative to working directory
     * @param parser   function that converts a String[] (fields) into a T, or null to skip the row
     * @return parsed items; empty list if the file does not exist or is unreadable
     */
    public static <T> List<T> readCsv(String path, Function<String[], T> parser) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("  [CsvUtil] File not found: " + path + " — using empty list.");
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header

            int lineNum = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;

                String[] fields = parseCsvLine(line);
                try {
                    T item = parser.apply(fields);
                    if (item != null) {
                        result.add(item);
                    } else {
                        System.out.println("  [CsvUtil] Skipping malformed row in "
                                + path + " line " + lineNum + ": " + line);
                    }
                } catch (Exception e) {
                    System.out.println("  [CsvUtil] Skipping row due to error in "
                            + path + " line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("  [CsvUtil] Error reading " + path + ": " + e.getMessage());
        }
        return result;
    }

    /**
     * Writes a list of CsvPersistable items to a CSV file with a header row.
     * Creates parent directories if needed.
     */
    public static void writeCsv(String path, List<? extends CsvPersistable> items, String header) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.println(header);
                for (CsvPersistable item : items) {
                    String[] fields = item.toCsvRow().split(",", -1);
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].contains(",") || fields[i].contains("\"")
                                || fields[i].contains("\n") || fields[i].contains("\r")) {
                            fields[i] = "\"" + fields[i].replace("\"", "\"\"") + "\"";
                        }
                    }
                    writer.println(String.join(",", fields));
                }
            }
        } catch (IOException e) {
            System.out.println("  [CsvUtil] Error writing " + path + ": " + e.getMessage());
        }
    }

    /**
     * Splits a CSV line into fields, handling double-quoted fields
     * (e.g. field,"quoted,field",another → [field, quoted,field, another]).
     */
    static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}
