package model;

public class QuestionFactory {

    // Used when loading from CSV (id already exists)
    public static Question createQuestion(
            int id,
            String text,
            String[] options,
            int correctIndex,
            QuestionDifficulty difficulty
    ) {
        return new Question(id, text, options, correctIndex, difficulty);
    }

    // Used when creating a new question from UI (id assigned later)
    public static Question createQuestion(
            String text,
            String[] options,
            int correctIndex,
            QuestionDifficulty difficulty
    ) {
        return new Question(text, options, correctIndex, difficulty);
    }
}
