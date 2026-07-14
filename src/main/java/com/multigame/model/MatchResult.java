package com.multigame.model;

import java.time.LocalDate;

/**
 * MatchResult — maps one joined row from MatchResults + Players + Matches.
 *
 * The raw MatchResults table only stores IDs (MatchID, PlayerID).
 * But in the UI we want to show the player's name and the match date,
 * so the DAO does a JOIN and populates username + matchDate here.
 *
 * Table columns used:
 *   MatchResults: MatchID, PlayerID, Score, Kills, Deaths, RankPosition
 *   Players:      username
 *   Matches:      MatchDate
 *
 * JavaFX TableView uses PropertyValueFactory("fieldName") which calls
 * getFieldName() via reflection — so every getter must match exactly.
 */
public class MatchResult {

    private final int       matchID;
    private final int       playerID;
    private final String    username;      // joined from Players
    private final LocalDate matchDate;     // joined from Matches
    private final int       score;
    private final int       kills;
    private final int       deaths;
    private final int       rankPosition;

    public MatchResult(int matchID, int playerID, String username,
                       LocalDate matchDate, int score,
                       int kills, int deaths, int rankPosition) {
        this.matchID      = matchID;
        this.playerID     = playerID;
        this.username     = username;
        this.matchDate    = matchDate;
        this.score        = score;
        this.kills        = kills;
        this.deaths       = deaths;
        this.rankPosition = rankPosition;
    }

    // ── Getters (names must match PropertyValueFactory strings exactly) ──────
    public int       getMatchID()      { return matchID; }
    public int       getPlayerID()     { return playerID; }
    public String    getUsername()     { return username; }
    public LocalDate getMatchDate()    { return matchDate; }
    public int       getScore()        { return score; }
    public int       getKills()        { return kills; }
    public int       getDeaths()       { return deaths; }
    public int       getRankPosition() { return rankPosition; }
}
