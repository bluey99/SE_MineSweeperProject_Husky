package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Central data access class. Handles: - Game history (game_history.csv) -
 * Trivia questions (QuestionsCSV.csv)
 *
 * Controllers and views MUST NOT access files directly.
 */
public class SysData {

	// ===================== HISTORY CONFIG =====================
	private static final String HISTORY_FILE = "game_history.csv";
	private static final String HISTORY_HEADER = "dateTime,difficulty,player1Name,player2Name,result,finalScore,gameLengthSeconds";

	// ===================== QUESTIONS CONFIG =====================
	// CSV columns: ID,Question,Difficulty,A,B,C,D,Correct Answer
	private static final String QUESTIONS_FILE = "QuestionsCSV.csv";
	private static final String QUESTIONS_HEADER = "ID,Question,Difficulty,A,B,C,D,Correct Answer";

	// ============================================================
	// GAME HISTORY API
	// ============================================================
	public static void saveGame(GameHistoryEntry entry) {
		File file = new File(HISTORY_FILE);

		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

			// if file is new or empty → write header first
			if (!file.exists() || file.length() == 0) {
				writer.write(HISTORY_HEADER);
				writer.newLine();
			}

			writer.write(entry.toCsvRow());
			writer.newLine();

		} catch (IOException e) {
			System.err.println("Failed to write to history file: " + e.getMessage());
		}
	}

	public static List<GameHistoryEntry> loadHistory() {
		List<GameHistoryEntry> list = new ArrayList<>();
		File file = new File(HISTORY_FILE);

		if (!file.exists() || file.length() == 0) {
			return list; // no history yet
		}

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

			String line = reader.readLine(); // skip header

			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;

				String[] parts = line.split(",", -1);
				if (parts.length < 7)
					continue;

				String dateTime = parts[0];
				String difficulty = parts[1];
				String p1 = parts[2];
				String p2 = parts[3];
				String result = parts[4];
				int finalScore = Integer.parseInt(parts[5]);
				int gameLength = Integer.parseInt(parts[6]);

				GameHistoryEntry entry = new GameHistoryEntry(dateTime, difficulty, p1, p2, result, finalScore,
						gameLength);
				list.add(entry);
			}

		} catch (IOException | NumberFormatException e) {
			System.err.println("Failed to load history: " + e.getMessage());
		}

		return list;
	}

	// ============================================================
//  HISTORY ADMIN OPERATIONS
//============================================================

	/** Clear all history rows, keep header. @return number removed */
	public static int clearHistory() {
		List<GameHistoryEntry> all = loadHistory();
		rewriteHistory(new ArrayList<>());
		return all.size();
	}

	/**
	 * Keep only most recent keepN rows (based on file order: append = newest at
	 * end)
	 * 
	 * @return number removed
	 */
	public static int trimHistory(int keepN) {
		List<GameHistoryEntry> all = loadHistory();

		if (keepN >= all.size())
			return 0;

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

	/**
	 * Delete a specific history entry (works even if UI is sorted).
	 * 
	 * @return true if removed
	 */
	public static boolean deleteHistoryEntry(GameHistoryEntry target) {
		List<GameHistoryEntry> all = loadHistory();

		boolean removed = all.removeIf(e -> sameEntry(e, target));
		if (!removed)
			return false;

		rewriteHistory(all);
		return true;
	}

	/** Strict match by all fields (so delete is safe) */
	private static boolean sameEntry(GameHistoryEntry a, GameHistoryEntry b) {
		if (a == null || b == null)
			return false;
		return safeEq(a.getDateTime(), b.getDateTime()) && safeEq(a.getDifficulty(), b.getDifficulty())
				&& safeEq(a.getPlayer1Name(), b.getPlayer1Name()) && safeEq(a.getPlayer2Name(), b.getPlayer2Name())
				&& safeEq(a.getResult(), b.getResult()) && a.getFinalScore() == b.getFinalScore()
				&& a.getGameLengthSeconds() == b.getGameLengthSeconds();
	}

	private static boolean safeEq(String x, String y) {
		if (x == null && y == null)
			return true;
		if (x == null || y == null)
			return false;
		return x.equals(y);
	}

	/** Rewrite CSV from scratch (MODEL ONLY) */
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

	// ============================================================
	// QUESTIONS API
	// ============================================================

	/**
	 * Load all questions from the QuestionsCSV.csv file.
	 */
	public static List<Question> loadQuestions() {
		List<Question> list = new ArrayList<>();
		File file = new File(QUESTIONS_FILE);

		if (!file.exists() || file.length() == 0) {
			return list; // no questions yet
		}

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

			String line;
			boolean firstLine = true;

			while ((line = br.readLine()) != null) {

				// Skip header
				if (firstLine) {
					firstLine = false;
					continue;
				}

				if (line.trim().isEmpty())
					continue;

				String[] cols = line.split(",", -1);
				if (cols.length < 8) {
					// malformed line, skip
					continue;
				}

				// Remove BOM from first column if present
				cols[0] = cols[0].replace("\uFEFF", "");

				int id = Integer.parseInt(cols[0].trim());
				String text = cols[1];
				String difficultyStr = cols[2];

				String[] options = new String[4];
				options[0] = cols[3]; // A
				options[1] = cols[4]; // B
				options[2] = cols[5]; // C
				options[3] = cols[6]; // D

				String correctLetter = cols[7].trim();
				int correctIndex = letterToIndex(correctLetter);

				QuestionDifficulty diff = QuestionDifficulty.fromString(difficultyStr);

				Question q = new Question(id, text, options, correctIndex, diff);
				list.add(q);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * Add a new question to storage. Simple implementation: - Load all questions -
	 * Add new one - Rewrite file
	 */
	public static boolean addQuestion(Question q) {
		List<Question> all = loadQuestions();

		// Assign next ID based on list size (temporary)
		q.setId(all.size() + 1);

		all.add(q);

		// Reassign IDs to guarantee consistency
		reassignQuestionIds(all);

		return saveQuestions(all);
	}

	/**
	 * Update an existing question in storage. This assumes equality by ID.
	 */
	public static boolean updateQuestion(Question updated) {
		List<Question> all = loadQuestions();

		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).getId() == updated.getId()) {
				all.set(i, updated);
				break;
			}
		}

		// Ensure IDs are still aligned
		reassignQuestionIds(all);

		return saveQuestions(all);
	}

	/**
	 * Delete a question from storage.
	 */
	public static boolean deleteQuestion(Question toDelete) {
		List<Question> all = loadQuestions();

		boolean removed = all.removeIf(q -> q.getId() == toDelete.getId());
		if (!removed) {
			return false; // nothing to delete
		}

		reassignQuestionIds(all);
		return saveQuestions(all);
	}

	/**
	 * Helper: write full list of questions back to CSV.
	 */
	private static boolean saveQuestions(List<Question> questions) {
		File file = new File(QUESTIONS_FILE);

		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

			writer.write(QUESTIONS_HEADER);
			writer.newLine();

			for (Question q : questions) {
				StringBuilder sb = new StringBuilder();

				sb.append(q.getId()).append(",");
				sb.append(escape(q.getText())).append(",");
				sb.append(q.getDifficulty().toString()).append(",");

				String[] options = q.getOptions();
				sb.append(escape(options[0])).append(",");
				sb.append(escape(options[1])).append(",");
				sb.append(escape(options[2])).append(",");
				sb.append(escape(options[3])).append(",");

				sb.append(indexToLetter(q.getCorrectIndex()));

				writer.write(sb.toString());
				writer.newLine();
			}

			return true;

		} catch (IOException e) {
			// bayan added here - silent failure, controller handles user notification
			return false;
		}

	}

	// ====== Helpers ======

	private static int letterToIndex(String letter) {
		if (letter == null)
			return -1;
		switch (letter.toUpperCase()) {
		case "A":
			return 0;
		case "B":
			return 1;
		case "C":
			return 2;
		case "D":
			return 3;
		default:
			return -1;
		}
	}

	private static String indexToLetter(int idx) {
		switch (idx) {
		case 0:
			return "A";
		case 1:
			return "B";
		case 2:
			return "C";
		case 3:
			return "D";
		default:
			return "";
		}
	}

	// Very simple escaping – if you want to support commas inside text, improve
	// this.
	private static String escape(String s) {
		if (s == null)
			return "";
		return s.replace("\n", " ").replace("\r", " ");
	}

	// 🔒 MODEL-ONLY helper (not visible to controllers)
	private static void reassignQuestionIds(List<Question> questions) {
		for (int i = 0; i < questions.size(); i++) {
			questions.get(i).setId(i + 1);
		}
	}
}
