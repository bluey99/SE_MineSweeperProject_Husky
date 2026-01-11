package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Central data access class.
 *
 * Handles:
 * - Game history (game_history.csv)
 * - Trivia questions (QuestionsCSV.csv)
 *
 * Controllers and views MUST NOT access files directly.
 */
public class SysData {

    // ===================== HISTORY CONFIG =====================
    private static final String HISTORY_FILE = "game_history.csv";
    private static final String HISTORY_HEADER =
            "dateTime,difficulty,player1Name,player2Name,result,finalScore,gameLengthSeconds";

    // ===================== QUESTIONS CONFIG =====================
    // CSV columns: ID,Question,Difficulty,A,B,C,D,Correct Answer
    private static final String QUESTIONS_FILE = "QuestionsCSV.csv";
    private static final String QUESTIONS_HEADER =
            "ID,Question,Difficulty,A,B,C,D,Correct Answer";

    // ============================================================
    //                      GAME HISTORY API
    // ============================================================

    /** Append a game history entry */
    public static void saveGame(GameHistoryEntry entry) {
        File file = new File(HISTORY_FILE);

        // ✅ IMPORTANT: decide header BEFORE opening stream (opening can create the file)
        boolean writeHeader = (!file.exists() || file.length() == 0);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

            if (writeHeader) {
                writer.write(HISTORY_HEADER);
                writer.newLine();
            }

            writer.write(entry.toCsvRow());
            writer.newLine();

        } catch (IOException e) {
            System.err.println("Failed to write to history file: " + e.getMessage());
        }
    }

    /** Load all history entries */
    public static List<GameHistoryEntry> loadHistory() {
        List<GameHistoryEntry> list = new ArrayList<>();
        File file = new File(HISTORY_FILE);

        if (!file.exists() || file.length() == 0) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue;

                GameHistoryEntry entry = new GameHistoryEntry(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6])
                );

                list.add(entry);
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load history: " + e.getMessage());
        }

        return list;
    }

    // ============================================================
    //                  HISTORY ADMIN OPERATIONS
    // ============================================================

    /** Remove all history entries (keep header) */
    public static int clearHistory() {
        List<GameHistoryEntry> all = loadHistory();
        rewriteHistory(new ArrayList<>());
        return all.size();
    }

    /** Keep only the last N history entries */
    public static int trimHistory(int keepN) {
        List<GameHistoryEntry> all = loadHistory();

        if (keepN >= all.size()) return 0;

        if (keepN <= 0) {
            int removed = all.size();
            rewriteHistory(new ArrayList<>());
            return removed;
        }

        int fromIndex = Math.max(0, all.size() - keepN);
        List<GameHistoryEntry> kept = new ArrayList<>(all.subList(fromIndex, all.size()));

        int removed = all.size() - kept.size();
        rewriteHistory(kept);
        return removed;
    }

    /** Safely delete a specific history entry */
    public static boolean deleteHistoryEntry(GameHistoryEntry target) {
        List<GameHistoryEntry> all = loadHistory();

        boolean removed = all.removeIf(e -> sameEntry(e, target));
        if (!removed) return false;

        rewriteHistory(all);
        return true;
    }

    /** Rewrite history CSV from scratch */
    private static void rewriteHistory(List<GameHistoryEntry> entries) {
        File file = new File(HISTORY_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            writer.write(HISTORY_HEADER);
            writer.newLine();

            for (GameHistoryEntry e : entries) {
                writer.write(e.toCsvRow());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Failed to rewrite history: " + e.getMessage());
        }
    }

    /** Strict comparison for safe deletion */
    private static boolean sameEntry(GameHistoryEntry a, GameHistoryEntry b) {
        if (a == null || b == null) return false;

        return safeEq(a.getDateTime(), b.getDateTime())
                && safeEq(a.getDifficulty(), b.getDifficulty())
                && safeEq(a.getPlayer1Name(), b.getPlayer1Name())
                && safeEq(a.getPlayer2Name(), b.getPlayer2Name())
                && safeEq(a.getResult(), b.getResult())
                && a.getFinalScore() == b.getFinalScore()
                && a.getGameLengthSeconds() == b.getGameLengthSeconds();
    }

    private static boolean safeEq(String x, String y) {
        if (x == null && y == null) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    // ============================================================
    //                      QUESTIONS API
    // ============================================================

    /** Validate questions CSV state */
    public static QuestionsFileStatus getQuestionsFileStatus() {
        File file = new File(QUESTIONS_FILE);

        if (!file.exists()) return QuestionsFileStatus.NOT_EXISTS;
        if (file.length() == 0) return QuestionsFileStatus.EMPTY;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return QuestionsFileStatus.MALFORMED;

            // ✅ Remove BOM if present + trim
            header = header.replace("\uFEFF", "").trim();

            if (!header.equals(QUESTIONS_HEADER)) {
                return QuestionsFileStatus.MALFORMED;
            }

            String line;
            boolean hasData = false;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                hasData = true;

                // NOTE: this simple split assumes no quoted commas in fields.
                String[] cols = line.split(",", -1);
                if (cols.length != 8) return QuestionsFileStatus.MALFORMED;

                // Validate ID
                String idStr = cols[0].trim().replace("\uFEFF", "");
                if (idStr.isEmpty()) return QuestionsFileStatus.MALFORMED;
                Integer.parseInt(idStr);

                // Validate question text
                if (cols[1].trim().isEmpty()) return QuestionsFileStatus.MALFORMED;

                // Validate difficulty
                if (QuestionDifficulty.fromString(cols[2].trim()) == null)
                    return QuestionsFileStatus.MALFORMED;

                // Validate answer options A–D
                for (int i = 3; i <= 6; i++) {
                    if (cols[i].trim().isEmpty())
                        return QuestionsFileStatus.MALFORMED;
                }

                // Validate correct answer
                if (letterToIndex(cols[7].trim()) == -1)
                    return QuestionsFileStatus.MALFORMED;
            }

            return hasData ? QuestionsFileStatus.HAS_DATA : QuestionsFileStatus.EMPTY;

        } catch (Exception e) {
            return QuestionsFileStatus.MALFORMED;
        }
    }

    /** Load all questions */
    public static List<Question> loadQuestions() {
        List<Question> list = new ArrayList<>();
        File file = new File(QUESTIONS_FILE);

        if (!file.exists() || file.length() == 0) return list;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // skip header
                }

                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",", -1);
                if (cols.length < 8) continue;

                // Remove BOM if present
                cols[0] = cols[0].replace("\uFEFF", "");

                Question q = new Question(
                        Integer.parseInt(cols[0].trim()),
                        cols[1],
                        new String[]{cols[3], cols[4], cols[5], cols[6]},
                        letterToIndex(cols[7].trim()),
                        QuestionDifficulty.fromString(cols[2].trim())
                );

                list.add(q);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addQuestion(Question q) {
        List<Question> all = loadQuestions();
        q.setId(all.size() + 1);
        all.add(q);
        reassignQuestionIds(all);
        return saveQuestions(all);
    }

    public static boolean updateQuestion(Question updated) {
        List<Question> all = loadQuestions();

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == updated.getId()) {
                all.set(i, updated);
                break;
            }
        }

        reassignQuestionIds(all);
        return saveQuestions(all);
    }

    public static boolean deleteQuestion(Question toDelete) {
        List<Question> all = loadQuestions();

        boolean removed = all.removeIf(q -> q.getId() == toDelete.getId());
        if (!removed) return false;

        reassignQuestionIds(all);
        return saveQuestions(all);
    }

    /** Write full list of questions back to CSV */
    private static boolean saveQuestions(List<Question> questions) {
        File file = new File(QUESTIONS_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            writer.write(QUESTIONS_HEADER);
            writer.newLine();

            for (Question q : questions) {
                writer.write(
                        q.getId() + "," +
                                escapeCsv(q.getText()) + "," +
                                q.getDifficulty() + "," +
                                escapeCsv(q.getOptions()[0]) + "," +
                                escapeCsv(q.getOptions()[1]) + "," +
                                escapeCsv(q.getOptions()[2]) + "," +
                                escapeCsv(q.getOptions()[3]) + "," +
                                indexToLetter(q.getCorrectIndex())
                );
                writer.newLine();
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Recreate a clean questions file with header only */
    public static boolean recreateQuestionsFile() {
        File file = new File(QUESTIONS_FILE);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            writer.write(QUESTIONS_HEADER);
            writer.newLine();
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    // ============================================================
    //                      HELPERS
    // ============================================================

    private static int letterToIndex(String letter) {
        if (letter == null) return -1;
        switch (letter.toUpperCase()) {
            case "A": return 0;
            case "B": return 1;
            case "C": return 2;
            case "D": return 3;
            default: return -1;
        }
    }

    private static String indexToLetter(int idx) {
        switch (idx) {
            case 0: return "A";
            case 1: return "B";
            case 2: return "C";
            case 3: return "D";
            default: return "";
        }
    }

    /**
     * ✅ Proper CSV escaping:
     * - replaces newlines
     * - wraps in quotes if contains comma or quote
     * - doubles internal quotes
     */
    private static String escapeCsv(String s) {
        if (s == null) return "";
        String cleaned = s.replace("\r", " ").replace("\n", " ");
        boolean mustQuote = cleaned.contains(",") || cleaned.contains("\"");
        cleaned = cleaned.replace("\"", "\"\"");
        return mustQuote ? "\"" + cleaned + "\"" : cleaned;
    }

    private static void reassignQuestionIds(List<Question> questions) {
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setId(i + 1);
        }
    }

    // ============================================================
    //                  ✅ NEW: JSON IMPORT (NO EXTERNAL JARS)
    // ============================================================

    /**
     * Import questions from a JSON file into QuestionsCSV.csv (no external jars).
     *
     * Supported JSON formats:
     *
     * 1) [
     *   { "question":"...", "difficulty":"Easy", "options":["A","B","C","D"], "correct":"B" }
     * ]
     *
     * 2) [
     *   { "text":"...", "difficulty":"MEDIUM", "A":"..", "B":"..", "C":"..", "D":"..", "correctIndex":2 }
     * ]
     *
     * Notes:
     * - Duplicates are skipped (same question + difficulty + options + correct).
     * - Invalid entries are skipped.
     * - CSV safety: commas/newlines are sanitized.
     */
    public static ImportReport importQuestionsFromJson(File jsonFile) {
        try {
            if (jsonFile == null || !jsonFile.exists()) {
                return ImportReport.fail("JSON file not found.");
            }

            // Ensure questions file exists (header only) if missing/empty
            QuestionsFileStatus status = getQuestionsFileStatus();
            if (status == QuestionsFileStatus.NOT_EXISTS || status == QuestionsFileStatus.EMPTY) {
                recreateQuestionsFile();
            } else if (status == QuestionsFileStatus.MALFORMED) {
                return ImportReport.fail(
                        "QuestionsCSV.csv is malformed.\n" +
                                "Fix/replace it first before importing."
                );
            }

            String json = readAllText(jsonFile);

            List<java.util.Map<String, Object>> objects = SimpleJson.parseArrayOfObjects(json);
            if (objects == null || objects.isEmpty()) {
                return ImportReport.fail(
                        "No questions found in JSON.\n\nExpected: an array of objects, e.g.\n" +
                                "[{ \"question\":\"...\", \"difficulty\":\"Easy\", \"options\":[\"A\",\"B\",\"C\",\"D\"], \"correct\":\"B\" }]"
                );
            }

            List<Question> existing = loadQuestions();

            // signatures used to detect duplicates (ignore IDs)
            java.util.Set<String> signatures = new java.util.HashSet<>();
            for (Question q : existing) signatures.add(signatureOf(q));

            int total = objects.size();
            int imported = 0;
            int dup = 0;
            int invalid = 0;

            for (java.util.Map<String, Object> obj : objects) {
                Question q = convertJsonObjectToQuestion(obj);
                if (q == null) {
                    invalid++;
                    continue;
                }

                String sig = signatureOf(q);
                if (signatures.contains(sig)) {
                    dup++;
                    continue;
                }

                signatures.add(sig);
                existing.add(q);
                imported++;
            }

            reassignQuestionIds(existing);

            boolean saved = saveQuestions(existing);
            if (!saved) {
                return ImportReport.fail(
                        "Import succeeded but saving to QuestionsCSV.csv failed.\n" +
                                "Make sure the file is not open in Excel and try again."
                );
            }

            return ImportReport.ok(total, imported, dup, invalid);

        } catch (Exception e) {
            return ImportReport.fail("Import failed: " + e.getMessage());
        }
    }

    private static String readAllText(File f) throws IOException {
        try (InputStream in = new FileInputStream(f)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String signatureOf(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append(norm(q.getText())).append("|")
                .append(q.getDifficulty()).append("|");

        String[] o = q.getOptions();
        for (int i = 0; i < 4; i++) sb.append(norm(o[i])).append("|");

        sb.append(q.getCorrectIndex());
        return sb.toString();
    }

    private static String norm(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static Question convertJsonObjectToQuestion(java.util.Map<String, Object> obj) {
        if (obj == null) return null;

        String text = str(obj, "question", "Question", "text", "Text");
        if (text == null || text.trim().isEmpty()) return null;

        String diffStr = str(obj, "difficulty", "Difficulty", "level", "Level");
        QuestionDifficulty diff = QuestionDifficulty.fromString(diffStr);

        String[] opts = extractOptions(obj);
        if (opts == null || opts.length != 4) return null;

        // CSV safety (match your UI restrictions)
        text = sanitizeForCsv(text);
        for (int i = 0; i < 4; i++) {
            if (opts[i] == null || opts[i].trim().isEmpty()) return null;
            opts[i] = sanitizeForCsv(opts[i]);
        }

        int correctIndex = -1;

        Object ci = first(obj, "correctIndex", "CorrectIndex", "correct_index");
        if (ci instanceof Number) {
            correctIndex = ((Number) ci).intValue();
        }

        if (correctIndex < 0 || correctIndex > 3) {
            String correct = str(obj, "correct", "Correct", "correctAnswer", "Correct Answer", "correct_letter");
            if (correct != null) correctIndex = letterToIndex(correct.trim());
        }

        if (correctIndex < 0 || correctIndex > 3) return null;

        return new Question(text, opts, correctIndex, diff);
    }

    private static String sanitizeForCsv(String s) {
        if (s == null) return "";
        s = s.replace("\r", " ").replace("\n", " ").trim();
        s = s.replaceAll("\\s+", " ");
        // IMPORTANT: your CSV validator assumes no commas in fields.
        // We replace commas with semicolons to keep file stable.
        s = s.replace(",", ";");
        return s;
    }

    private static String[] extractOptions(java.util.Map<String, Object> obj) {
        Object arr = first(obj, "options", "Options", "answers", "Answers");
        if (arr instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) arr;
            if (list.size() >= 4) {
                return new String[]{
                        String.valueOf(list.get(0)),
                        String.valueOf(list.get(1)),
                        String.valueOf(list.get(2)),
                        String.valueOf(list.get(3))
                };
            }
        }

        // fallback: A,B,C,D keys
        String A = str(obj, "A", "a");
        String B = str(obj, "B", "b");
        String C = str(obj, "C", "c");
        String D = str(obj, "D", "d");

        if (A != null && B != null && C != null && D != null) {
            return new String[]{A, B, C, D};
        }

        return null;
    }

    private static Object first(java.util.Map<String, Object> obj, String... keys) {
        for (String k : keys) {
            if (obj.containsKey(k)) return obj.get(k);
        }
        return null;
    }

    private static String str(java.util.Map<String, Object> obj, String... keys) {
        Object v = first(obj, keys);
        if (v == null) return null;
        return String.valueOf(v);
    }

    // ===================== Minimal JSON parser (no external jars) =====================

    private static class SimpleJson {

        /**
         * Parses JSON with structure:
         *   [ { ... }, { ... } ]
         *
         * Supported value types:
         * - strings
         * - numbers
         * - booleans
         * - arrays (of strings/numbers/booleans)
         * - nested objects (not needed but supported)
         */
        static java.util.List<java.util.Map<String, Object>> parseArrayOfObjects(String json) {
            if (json == null) return java.util.List.of();
            json = json.trim();
            if (!json.startsWith("[")) return java.util.List.of();

            Tokenizer t = new Tokenizer(json);
            t.expect('[');

            java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
            t.skipWs();

            if (t.peek() == ']') {
                t.next();
                return list;
            }

            while (true) {
                t.skipWs();
                java.util.Map<String, Object> obj = parseObject(t);
                if (obj != null) list.add(obj);

                t.skipWs();
                char ch = t.next();
                if (ch == ']') break;
                if (ch != ',') throw new RuntimeException("Expected , or ]");
            }

            return list;
        }

        private static java.util.Map<String, Object> parseObject(Tokenizer t) {
            t.skipWs();
            t.expect('{');

            java.util.Map<String, Object> map = new java.util.HashMap<>();
            t.skipWs();

            if (t.peek() == '}') {
                t.next();
                return map;
            }

            while (true) {
                t.skipWs();
                String key = t.readString();
                t.skipWs();
                t.expect(':');
                t.skipWs();

                Object val = parseValue(t);
                map.put(key, val);

                t.skipWs();
                char ch = t.next();
                if (ch == '}') break;
                if (ch != ',') throw new RuntimeException("Expected , or }");
            }

            return map;
        }

        private static Object parseValue(Tokenizer t) {
            t.skipWs();
            char p = t.peek();

            if (p == '"') return t.readString();
            if (p == '{') return parseObject(t);
            if (p == '[') return parseArray(t);

            String lit = t.readLiteral();
            if ("true".equals(lit)) return Boolean.TRUE;
            if ("false".equals(lit)) return Boolean.FALSE;
            if ("null".equals(lit)) return null;

            try {
                if (lit.contains(".")) return Double.parseDouble(lit);
                return Integer.parseInt(lit);
            } catch (Exception e) {
                return lit; // fallback
            }
        }

        private static java.util.List<Object> parseArray(Tokenizer t) {
            t.expect('[');
            java.util.List<Object> arr = new java.util.ArrayList<>();
            t.skipWs();

            if (t.peek() == ']') {
                t.next();
                return arr;
            }

            while (true) {
                Object v = parseValue(t);
                arr.add(v);

                t.skipWs();
                char ch = t.next();
                if (ch == ']') break;
                if (ch != ',') throw new RuntimeException("Expected , or ]");
            }
            return arr;
        }

        private static class Tokenizer {
            private final String s;
            private int i = 0;

            Tokenizer(String s) { this.s = s; }

            void skipWs() {
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
                    else break;
                }
            }

            char peek() {
                if (i >= s.length()) return '\0';
                return s.charAt(i);
            }

            char next() {
                if (i >= s.length()) throw new RuntimeException("Unexpected end of JSON");
                return s.charAt(i++);
            }

            void expect(char c) {
                skipWs();
                char got = next();
                if (got != c) throw new RuntimeException("Expected '" + c + "' but got '" + got + "'");
            }

            String readString() {
                skipWs();
                expect('"');
                StringBuilder out = new StringBuilder();

                while (true) {
                    char c = next();
                    if (c == '"') break;

                    if (c == '\\') {
                        char e = next();
                        switch (e) {
                            case '"': out.append('"'); break;
                            case '\\': out.append('\\'); break;
                            case '/': out.append('/'); break;
                            case 'b': out.append('\b'); break;
                            case 'f': out.append('\f'); break;
                            case 'n': out.append('\n'); break;
                            case 'r': out.append('\r'); break;
                            case 't': out.append('\t'); break;
                            case 'u':
                                String hex = "" + next() + next() + next() + next();
                                out.append((char) Integer.parseInt(hex, 16));
                                break;
                            default:
                                out.append(e);
                        }
                    } else {
                        out.append(c);
                    }
                }

                return out.toString();
            }

            String readLiteral() {
                skipWs();
                StringBuilder out = new StringBuilder();

                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c == ',' || c == ']' || c == '}' || Character.isWhitespace(c)) break;
                    out.append(c);
                    i++;
                }

                return out.toString().trim();
            }
        }
    }
}
