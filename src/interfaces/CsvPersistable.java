package interfaces;

/**
 * Contract for any entity that can serialize itself to a single CSV row.
 * Each implementing class also provides its own static {@code fromCsvRow(String)}
 * factory method (documented by convention, not enforced by this interface).
 */
public interface CsvPersistable {
    /** @return one CSV line (no trailing newline) representing this object. */
    String toCsvRow();
}
