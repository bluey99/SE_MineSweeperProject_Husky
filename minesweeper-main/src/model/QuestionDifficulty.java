package model;

public enum QuestionDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT;

    public static QuestionDifficulty fromString(String s) {
        if (s == null) return EASY;
        switch (s.trim().toUpperCase()) {
            case "MEDIUM": return MEDIUM;
            case "HARD":   return HARD;
            case "EXPERT": return EXPERT;
            default:       return EASY;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case MEDIUM: return "Medium";
            case HARD:   return "Hard";
            case EXPERT: return "Expert";
            default:     return "Easy";
        }
    }
}
