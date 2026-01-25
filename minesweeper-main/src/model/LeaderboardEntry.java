package model;

public class LeaderboardEntry {
    private final int rank;
    private final String duo;
    private final int wins;
    private final int games;
    private final String winRate;

    public LeaderboardEntry(int rank, String duo, int wins, int games, String winRate) {
        this.rank = rank;
        this.duo = duo;
        this.wins = wins;
        this.games = games;
        this.winRate = winRate;
    }

    public int getRank() { return rank; }
    public String getDuo() { return duo; }
    public int getWins() { return wins; }
    public int getGames() { return games; }
    public String getWinRate() { return winRate; }
}
