package com.multigame.dao;

import com.multigame.model.LeaderboardEntry;
import com.multigame.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LeaderboardDAO — reads from the Leaderboard table.
 *
 * The app NEVER writes to Leaderboard directly.
 * MySQL's UpdateLeaderboard trigger handles all writes automatically
 * whenever a new MatchResult is inserted.
 *
 * This DAO only ever reads — it joins Leaderboard with Players to
 * get the player's name for display alongside their points and rank.
 */
public class LeaderboardDAO {

    /**
     * Returns the top 50 players by TotalPoints, highest first.
     *
     * SQL:
     *   SELECT Players.PlayerID, username, TotalPoints, GlobalRank
     *   FROM Leaderboard JOIN Players
     *   ORDER BY TotalPoints DESC
     *   LIMIT 50
     *
     * @return List of LeaderboardEntry objects for the TableView
     * @throws SQLException if the query fails
     */
    public List<LeaderboardEntry> getTopPlayers() throws SQLException {
        List<LeaderboardEntry> entries = new ArrayList<>();

        String sql =
            "SELECT p.PlayerID, p.username, l.TotalPoints, l.GlobalRank "
          + "FROM Leaderboard l "
          + "JOIN Players p ON l.PlayerID = p.PlayerID "
          + "ORDER BY l.TotalPoints DESC "
          + "LIMIT 50";

        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                entries.add(new LeaderboardEntry(
                    rs.getInt("PlayerID"),
                    rs.getString("username"),
                    rs.getInt("TotalPoints"),
                    rs.getInt("GlobalRank")
                ));
            }
        }

        return entries;
    }
}
