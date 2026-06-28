package com.multigame.dao;

import com.multigame.model.Player;
import com.multigame.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PlayerDAO — handles all database operations for the Players table.
 *
 * Methods:
 *   getAllPlayers()         → used to populate the ComboBox on startup
 *   searchByUsername(str)  → used by the Match History search box
 */
public class PlayerDAO {

    /**
     * Fetches every player from the database, sorted alphabetically by username.
     * Called once on app startup to fill the player dropdown.
     *
     * SQL: SELECT all columns FROM Players ORDER BY username
     *
     * @return List of Player objects (one per row)
     * @throws SQLException if the query fails
     */
    public List<Player> getAllPlayers() throws SQLException {
        List<Player> players = new ArrayList<>();

        String sql = "SELECT PlayerID, username, level, email, gender, xp "
                   + "FROM Players "
                   + "ORDER BY username ASC";

        // try-with-resources automatically closes Statement and ResultSet
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                players.add(new Player(
                    rs.getInt("PlayerID"),
                    rs.getString("username"),
                    rs.getInt("level"),
                    rs.getString("email"),
                    rs.getString("gender"),
                    rs.getInt("xp")
                ));
            }
        }

        return players;
    }

    /**
     * Searches players by username — partial match, case-insensitive.
     * Used by the Match History tab's search box.
     *
     * SQL: SELECT ... WHERE username LIKE '%keyword%'
     *
     * Uses PreparedStatement (not string concatenation) to prevent SQL injection.
     *
     * @param keyword  the text the user typed in the search box
     * @return List of matching Player objects
     * @throws SQLException if the query fails
     */
    public List<Player> searchByUsername(String keyword) throws SQLException {
        List<Player> players = new ArrayList<>();

        String sql = "SELECT PlayerID, username, level, email, gender, xp "
                   + "FROM Players "
                   + "WHERE username LIKE ? "
                   + "ORDER BY username ASC";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            // Wrap keyword in % wildcards: "ali" becomes "%ali%"
            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(new Player(
                        rs.getInt("PlayerID"),
                        rs.getString("username"),
                        rs.getInt("level"),
                        rs.getString("email"),
                        rs.getString("gender"),
                        rs.getInt("xp")
                    ));
                }
            }
        }

        return players;
    }
}
