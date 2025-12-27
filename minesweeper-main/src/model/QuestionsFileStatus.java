package model;

/**
 * Indicates the current state of the questions CSV file.
 *
 * Used to distinguish between missing, empty, and populated files
 * so the UI can handle each case appropriately.
 */
public enum QuestionsFileStatus {
    NOT_EXISTS,  // File does not exist yet
    EMPTY,       // File exists but contains no questions
    HAS_DATA,     // File exists and contains question data
	MALFORMED // Questions file exists but is malformed or unreadable
}
