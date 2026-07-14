package com.multigame.model;

/**
 * LeaderboardEntry — maps one row from the Leaderboard table joined with Players.
 *
 * The Leaderboard table only stores PlayerID, TotalPoints, GlobalRank.
 * We JOIN Players to also get the username for display.
 *
 * TotalPoints is updated automatically by the MySQL trigger (UpdateLeaderboard)
 * every time a MatchResult row is inserted — the app never writes to Leaderboard directly.
 */
public class LeaderboardEntry {

    private final int    playerID;
    private final String username;
    private final int    totalPoints;
    private final int    globalRank;

    public LeaderboardEntry(int playerID, String username,
                            int totalPoints, int globalRank) {
        this.playerID    = playerID;
        this.username    = username;
        this.totalPoints = totalPoints;
        this.globalRank  = globalRank;
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    public int    getPlayerID()    { return playerID; }
    public String getUsername()    { return username; }
    public int    getTotalPoints() { return totalPoints; }
    public int    getGlobalRank()  { return globalRank; }
}
