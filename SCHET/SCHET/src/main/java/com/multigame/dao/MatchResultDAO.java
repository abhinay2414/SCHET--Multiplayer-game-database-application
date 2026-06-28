package com.multigame.dao;

import com.multigame.model.MatchResult;
import com.multigame.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MatchResultDAO — handles all DB operations for MatchResults and Matches tables.
 *
 * Methods:
 *   addMatchResult(...)      → inserts a new match + result in one transaction
 *   getAllMatchResults()      → returns all results joined with player/match data
 *   getResultsByPlayer(id)   → returns results for one specific player
 *
 * KEY DESIGN — Transaction:
 *   addMatchResult() uses a manual transaction (setAutoCommit false).
 *   Both INSERTs (Matches + MatchResults) succeed or both are rolled back.
 *   After the MatchResults insert, MySQL fires the UpdateLeaderboard trigger
 *   automatically — no extra code needed in Java.
 */
public class MatchResultDAO {

    /**
     * Records a player's result for a match.
     *
     * Steps:
     *  1. BEGIN TRANSACTION
     *  2. INSERT IGNORE into Matches (skips if MatchID already exists)
     *  3. INSERT into MatchResults
     *  4. COMMIT → MySQL trigger fires → Leaderboard.TotalPoints updated
     *  5. If any error → ROLLBACK (nothing is saved)
     *
     * @param matchID    the match identifier
     * @param matchDate  the date the match was played
     * @param playerID   the player's ID
     * @param score      player's score in this match
     * @param kills      number of kills
     * @param deaths     number of deaths
     * @param rankPosition  finishing position (1 = first place)
     * @throws SQLException on DB error or duplicate MatchID+PlayerID
     */
    public void addMatchResult(int matchID, LocalDate matchDate,
                               int playerID, int score,
                               int kills, int deaths,
                               int rankPosition) throws SQLException {

        Connection conn = DBConnection.getConnection();

        // Disable auto-commit to start a manual transaction
        conn.setAutoCommit(false);

        try {
            // ── Step 1: Insert the match (skip if MatchID already exists) ──
            // INSERT IGNORE silently skips if the primary key (MatchID) is duplicate
            String insertMatch = "INSERT IGNORE INTO Matches (MatchID, MatchDate) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertMatch)) {
                ps.setInt(1, matchID);
                ps.setDate(2, Date.valueOf(matchDate));  // LocalDate → java.sql.Date
                ps.executeUpdate();
            }

            // ── Step 2: Insert the match result ──
            // This will fail if (MatchID, PlayerID) already exists (composite PK)
            String insertResult =
                "INSERT INTO MatchResults (MatchID, PlayerID, Score, Kills, Deaths, RankPosition) "
              + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertResult)) {
                ps.setInt(1, matchID);
                ps.setInt(2, playerID);
                ps.setInt(3, score);
                ps.setInt(4, kills);
                ps.setInt(5, deaths);
                ps.setInt(6, rankPosition);
                ps.executeUpdate();
            }

            // ── Step 3: Commit — trigger fires here automatically ──
            conn.commit();
            System.out.println("✔ Match result saved. Trigger updating leaderboard...");

        } catch (SQLException e) {
            // Something went wrong — undo both inserts
            conn.rollback();
            System.err.println("✖ Transaction rolled back: " + e.getMessage());
            throw e;  // re-throw so the controller can show the error to the user

        } finally {
            // Always restore auto-commit mode (important for reused connections)
            conn.setAutoCommit(true);
        }
    }

    /**
     * Returns ALL match results, joined with player names and match dates.
     *
     * SQL joins three tables:
     *   MatchResults  ←→  Players   (to get username instead of PlayerID)
     *   MatchResults  ←→  Matches   (to get MatchDate instead of MatchID only)
     *
     * Results ordered newest-first by MatchDate.
     *
     * @return List of MatchResult objects ready to display in the TableView
     * @throws SQLException if the query fails
     */
    public List<MatchResult> getAllMatchResults() throws SQLException {
        List<MatchResult> results = new ArrayList<>();

        String sql =
            "SELECT mr.MatchID, mr.PlayerID, p.username, m.MatchDate, "
          + "       mr.Score, mr.Kills, mr.Deaths, mr.RankPosition "
          + "FROM MatchResults mr "
          + "JOIN Players p ON mr.PlayerID = p.PlayerID "
          + "JOIN Matches m ON mr.MatchID  = m.MatchID "
          + "ORDER BY m.MatchDate DESC, mr.Score DESC";

        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                results.add(new MatchResult(
                    rs.getInt("MatchID"),
                    rs.getInt("PlayerID"),
                    rs.getString("username"),
                    rs.getDate("MatchDate").toLocalDate(),  // java.sql.Date → LocalDate
                    rs.getInt("Score"),
                    rs.getInt("Kills"),
                    rs.getInt("Deaths"),
                    rs.getInt("RankPosition")
                ));
            }
        }

        return results;
    }

    /**
     * Returns match results for ONE specific player.
     * Used when the user searches by name in Match History.
     *
     * SQL: Same 3-table JOIN but with WHERE mr.PlayerID = ?
     *
     * @param playerID  the player to filter by
     * @return List of that player's match results
     * @throws SQLException if the query fails
     */
    public List<MatchResult> getResultsByPlayer(int playerID) throws SQLException {
        List<MatchResult> results = new ArrayList<>();

        String sql =
            "SELECT mr.MatchID, mr.PlayerID, p.username, m.MatchDate, "
          + "       mr.Score, mr.Kills, mr.Deaths, mr.RankPosition "
          + "FROM MatchResults mr "
          + "JOIN Players p ON mr.PlayerID = p.PlayerID "
          + "JOIN Matches m ON mr.MatchID  = m.MatchID "
          + "WHERE mr.PlayerID = ? "
          + "ORDER BY m.MatchDate DESC";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, playerID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new MatchResult(
                        rs.getInt("MatchID"),
                        rs.getInt("PlayerID"),
                        rs.getString("username"),
                        rs.getDate("MatchDate").toLocalDate(),
                        rs.getInt("Score"),
                        rs.getInt("Kills"),
                        rs.getInt("Deaths"),
                        rs.getInt("RankPosition")
                    ));
                }
            }
        }

        return results;
    }

    /**
     * Checks whether a (MatchID, PlayerID) pair already exists.
     * Used to show a friendlier error before attempting the insert.
     *
     * @param matchID   the match
     * @param playerID  the player
     * @return true if the result already exists
     * @throws SQLException if the query fails
     */
    public boolean resultExists(int matchID, int playerID) throws SQLException {
        String sql = "SELECT 1 FROM MatchResults WHERE MatchID = ? AND PlayerID = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, matchID);
            ps.setInt(2, playerID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();  // true if at least one row found
            }
        }
    }
}
