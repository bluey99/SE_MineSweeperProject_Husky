package model;

public class GameHistoryEntry {
    private final String dateTime;
    private final String difficulty;
    private final String player1Name;
    private final String player2Name;
    private final String result;           // WIN / LOSE
    private final int finalScore;
    private final int gameLengthSeconds;

    public GameHistoryEntry(String dateTime,
                            String difficulty,
                            String player1Name,
                            String player2Name,
                            String result,
                            int finalScore,
                            int gameLengthSeconds) {
        this.dateTime = dateTime;
        this.difficulty = difficulty;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.result = result;
        this.finalScore = finalScore;
        this.gameLengthSeconds = gameLengthSeconds;
    }

    // === getters (TableView needs them) ===
    public String getDateTime()          { return dateTime; }
    public String getDifficulty()        { return difficulty; }
    public String getPlayer1Name()       { return player1Name; }
    public String getPlayer2Name()       { return player2Name; }
    public String getResult()            { return result; }
    public int    getFinalScore()        { return finalScore; }
    public int    getGameLengthSeconds() { return gameLengthSeconds; }

    // used for saving to CSV
    public String toCsvRow() {
        return String.join(",",
                dateTime,
                difficulty,
                player1Name,
                player2Name,
                result,
                String.valueOf(finalScore),
                String.valueOf(gameLengthSeconds)
        );
    }
}
