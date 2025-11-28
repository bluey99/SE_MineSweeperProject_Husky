package model;

public class Question {

    private int id;                      // unique id inside CSV
    private String text;
    private String[] options;           // length 4
    private int correctIndex;           // 0–3
    private QuestionDifficulty difficulty;

    public Question(int id, String text, String[] options,
                    int correctIndex, QuestionDifficulty difficulty) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.difficulty = difficulty;
    }

    // convenience ctor (id set later)
    public Question(String text, String[] options,
                    int correctIndex, QuestionDifficulty difficulty) {
        this(-1, text, options, correctIndex, difficulty);
    }

    // Getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String[] getOptions() { return options; }
    public void setOptions(String[] options) { this.options = options; }

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public QuestionDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(QuestionDifficulty difficulty) { this.difficulty = difficulty; }

    public String getCorrectAnswerText() {
        if (options == null || correctIndex < 0 || correctIndex >= options.length) return "";
        return options[correctIndex];
    }
}
