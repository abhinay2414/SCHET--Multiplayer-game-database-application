package com.multigame.model;

/**
 * Player — maps one row from the Players table.
 *
 * Players table columns:
 *   PlayerID | username | level | email | gender | xp
 *
 * Used in:
 *   - PlayerDAO (fetched from DB)
 *   - MainController (loaded into the ComboBox dropdown)
 *
 * toString() is overridden so the ComboBox shows "111 – Humfrid Dawtre"
 * instead of the default object hash.
 */
public class Player {

    private final int    playerID;
    private final String username;
    private final int    level;
    private final String email;
    private final String gender;
    private final int    xp;

    public Player(int playerID, String username, int level,
                  String email, String gender, int xp) {
        this.playerID = playerID;
        this.username = username;
        this.level    = level;
        this.email    = email;
        this.gender   = gender;
        this.xp       = xp;
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    public int    getPlayerID() { return playerID; }
    public String getUsername() { return username; }
    public int    getLevel()    { return level; }
    public String getEmail()    { return email; }
    public String getGender()   { return gender; }
    public int    getXp()       { return xp; }

    /**
     * Shown in the ComboBox dropdown as: "111 – Humfrid Dawtre"
     */
    @Override
    public String toString() {
        return playerID + "  –  " + username;
    }
}
