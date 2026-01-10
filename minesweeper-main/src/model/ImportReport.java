package model;

public class ImportReport {
    public final boolean success;
    public final String message;
    public final int totalInJson;
    public final int imported;
    public final int skippedDuplicates;
    public final int skippedInvalid;

    public ImportReport(boolean success, String message,
                        int totalInJson, int imported,
                        int skippedDuplicates, int skippedInvalid) {
        this.success = success;
        this.message = message;
        this.totalInJson = totalInJson;
        this.imported = imported;
        this.skippedDuplicates = skippedDuplicates;
        this.skippedInvalid = skippedInvalid;
    }

    public static ImportReport ok(int total, int imported, int dup, int invalid) {
        return new ImportReport(true,
                "Imported: " + imported +
                "\nSkipped duplicates: " + dup +
                "\nSkipped invalid: " + invalid +
                "\nTotal in JSON: " + total,
                total, imported, dup, invalid);
    }

    public static ImportReport fail(String msg) {
        return new ImportReport(false, msg, 0, 0, 0, 0);
    }
}
