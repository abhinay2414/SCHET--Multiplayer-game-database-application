package com.multigame.controller;

import com.multigame.dao.LeaderboardDAO;
import com.multigame.dao.MatchResultDAO;
import com.multigame.dao.PlayerDAO;
import com.multigame.model.LeaderboardEntry;
import com.multigame.model.MatchResult;
import com.multigame.model.Player;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * MainController — the brain of the application.
 *
 * Responsibilities:
 *   • Wired to main.fxml via fx:controller attribute
 *   • @FXML fields are auto-injected by FXMLLoader (must match fx:id in FXML)
 *   • @FXML methods are called when the user clicks buttons (match onAction in FXML)
 *   • Calls DAO methods to read/write the database
 *   • Updates the UI based on results
 *
 * Three panels (VBox nodes) are stacked in the StackPane:
 *   panelAdd         → Add Result form
 *   panelResults     → Match History table
 *   panelLeaderboard → Leaderboard table
 * Only one is visible at a time; switchPanel() handles showing/hiding.
 */
public class MainController implements Initializable {

    // ── Navigation buttons (top bar) ────────────────────────────────────────
    @FXML private Button btnTabAdd;
    @FXML private Button btnTabResults;
    @FXML private Button btnTabLeaderboard;

    // ── Panel containers (only one visible at a time) ───────────────────────
    @FXML private VBox panelAdd;
    @FXML private VBox panelResults;
    @FXML private VBox panelLeaderboard;

    // ── Add Result form fields ───────────────────────────────────────────────
    @FXML private ComboBox<Player> cbPlayer;      // dropdown — players from DB
    @FXML private TextField        tfMatchID;     // match ID input
    @FXML private DatePicker       dpMatchDate;   // date picker
    @FXML private TextField        tfScore;       // score input
    @FXML private TextField        tfKills;       // kills input
    @FXML private TextField        tfDeaths;      // deaths input
    @FXML private TextField        tfRank;        // rank position input
    @FXML private Label            lblStatus;     // success / error message

    // ── Match History table and its columns ─────────────────────────────────
    @FXML private TableView<MatchResult>            tableResults;
    @FXML private TableColumn<MatchResult, Integer>   colMatchID;
    @FXML private TableColumn<MatchResult, String>    colUsername;
    @FXML private TableColumn<MatchResult, LocalDate> colDate;
    @FXML private TableColumn<MatchResult, Integer>   colScore;
    @FXML private TableColumn<MatchResult, Integer>   colKills;
    @FXML private TableColumn<MatchResult, Integer>   colDeaths;
    @FXML private TableColumn<MatchResult, Integer>   colRank;
    @FXML private TextField tfSearch;             // search box

    // ── Leaderboard table and its columns ───────────────────────────────────
    @FXML private TableView<LeaderboardEntry>              tableLeaderboard;
    @FXML private TableColumn<LeaderboardEntry, Integer>   colLbRank;
    @FXML private TableColumn<LeaderboardEntry, String>    colLbPlayer;
    @FXML private TableColumn<LeaderboardEntry, Integer>   colLbPoints;
    @FXML private TableColumn<LeaderboardEntry, Integer>   colLbID;

    // ── DAO instances (one per table group) ─────────────────────────────────
    private final PlayerDAO      playerDAO      = new PlayerDAO();
    private final MatchResultDAO matchResultDAO = new MatchResultDAO();
    private final LeaderboardDAO leaderboardDAO = new LeaderboardDAO();

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALIZATION — called automatically by FXMLLoader after @FXML inject
    // ════════════════════════════════════════════════════════════════════════

    /**
     * initialize() runs once after all @FXML fields have been injected.
     * This is where we do one-time setup: wire columns, load initial data.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupMatchHistoryColumns();   // tell each column which field to display
        setupLeaderboardColumns();
        loadPlayersIntoDropdown();    // fill the ComboBox from the database
        dpMatchDate.setValue(LocalDate.now());  // default date = today
        highlightNav(btnTabAdd);      // highlight the first tab
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NAVIGATION — switch between the 3 panels
    // ════════════════════════════════════════════════════════════════════════

    /** Show Add Result panel */
    @FXML
    private void showAddTab() {
        switchPanel(panelAdd);
        highlightNav(btnTabAdd);
    }

    /** Show Match History panel and load latest data */
    @FXML
    private void showResultsTab() {
        switchPanel(panelResults);
        highlightNav(btnTabResults);
        loadAllResults();   // refresh data every time user opens this tab
    }

    /** Show Leaderboard panel and load latest data */
    @FXML
    private void showLeaderboardTab() {
        switchPanel(panelLeaderboard);
        highlightNav(btnTabLeaderboard);
        loadLeaderboard();  // refresh data every time user opens this tab
    }

    /**
     * Makes only the target panel visible; hides the other two.
     * managed=false means the hidden panel also takes no layout space.
     */
    private void switchPanel(VBox target) {
        VBox[] panels = { panelAdd, panelResults, panelLeaderboard };
        for (VBox p : panels) {
            boolean active = (p == target);
            p.setVisible(active);
            p.setManaged(active);
        }
    }

    /**
     * Highlights the active nav button in red; others go grey.
     */
    private void highlightNav(Button active) {
        String activeStyle = "-fx-background-color:#e94560; -fx-text-fill:white; "
                           + "-fx-font-size:14px; -fx-font-weight:bold; "
                           + "-fx-padding:12 20; -fx-cursor:hand; "
                           + "-fx-background-radius:0;";
        String inactiveStyle = "-fx-background-color:transparent; -fx-text-fill:#999; "
                             + "-fx-font-size:14px; -fx-padding:12 20; "
                             + "-fx-cursor:hand; -fx-background-radius:0;";

        btnTabAdd.setStyle(         btnTabAdd         == active ? activeStyle : inactiveStyle);
        btnTabResults.setStyle(     btnTabResults     == active ? activeStyle : inactiveStyle);
        btnTabLeaderboard.setStyle( btnTabLeaderboard == active ? activeStyle : inactiveStyle);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ADD RESULT — form logic
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Loads all players from the Players table into the ComboBox dropdown.
     * The Player.toString() method controls what text appears in the dropdown.
     * Called once on startup.
     */
    private void loadPlayersIntoDropdown() {
        try {
            List<Player> players = playerDAO.getAllPlayers();
            cbPlayer.setItems(FXCollections.observableArrayList(players));
        } catch (SQLException e) {
            showError("Could not load players: " + e.getMessage());
        }
    }

    /**
     * Called when the user clicks "Save Result".
     *
     * Flow:
     *  1. Validate all inputs (empty check, numeric check, duplicate check)
     *  2. Call matchResultDAO.addMatchResult() — runs the DB transaction
     *  3. Show success or error message
     *  4. On success, clear the form
     */
    @FXML
    private void saveMatchResult() {
        clearStatus();

        // ── Validate: player selected ──
        Player selectedPlayer = cbPlayer.getValue();
        if (selectedPlayer == null) {
            showError("Please select a player from the dropdown.");
            return;
        }

        // ── Validate: date selected ──
        if (dpMatchDate.getValue() == null) {
            showError("Please select a match date.");
            return;
        }

        // ── Validate: all text fields filled ──
        if (tfMatchID.getText().trim().isEmpty() ||
            tfScore.getText().trim().isEmpty()   ||
            tfKills.getText().trim().isEmpty()   ||
            tfDeaths.getText().trim().isEmpty()  ||
            tfRank.getText().trim().isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        // ── Validate: numeric fields contain actual numbers ──
        int matchID, score, kills, deaths, rank;
        try {
            matchID = Integer.parseInt(tfMatchID.getText().trim());
            score   = Integer.parseInt(tfScore.getText().trim());
            kills   = Integer.parseInt(tfKills.getText().trim());
            deaths  = Integer.parseInt(tfDeaths.getText().trim());
            rank    = Integer.parseInt(tfRank.getText().trim());
        } catch (NumberFormatException e) {
            showError("Match ID, Score, Kills, Deaths, and Rank must be whole numbers.");
            return;
        }

        // ── Validate: no negative values ──
        if (score < 0 || kills < 0 || deaths < 0 || rank < 1) {
            showError("Score/Kills/Deaths must be ≥ 0. Rank must be ≥ 1.");
            return;
        }

        // ── Validate: duplicate check ──
        try {
            if (matchResultDAO.resultExists(matchID, selectedPlayer.getPlayerID())) {
                showError("This player already has a result for Match ID " + matchID + ".");
                return;
            }
        } catch (SQLException e) {
            showError("DB error during duplicate check: " + e.getMessage());
            return;
        }

        // ── All validation passed → save to DB ──
        try {
            matchResultDAO.addMatchResult(
                matchID,
                dpMatchDate.getValue(),
                selectedPlayer.getPlayerID(),
                score, kills, deaths, rank
            );

            // ── Success ──
            showSuccess("✔  Result saved for " + selectedPlayer.getUsername()
                      + "! Leaderboard updated automatically.");
            clearForm();

        } catch (SQLException e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    /**
     * Resets all form fields to their defaults.
     * Called after a successful save or when the user clicks "Clear".
     */
    @FXML
    private void clearForm() {
        cbPlayer.setValue(null);
        tfMatchID.clear();
        tfScore.clear();
        tfKills.clear();
        tfDeaths.clear();
        tfRank.clear();
        dpMatchDate.setValue(LocalDate.now());
        clearStatus();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MATCH HISTORY — table setup and data loading
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Binds each TableColumn to a field in MatchResult.
     *
     * PropertyValueFactory("score") calls getScore() on each row object.
     * This is how JavaFX knows which value to put in which column.
     */
    private void setupMatchHistoryColumns() {
        colMatchID .setCellValueFactory(new PropertyValueFactory<>("matchID"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colDate    .setCellValueFactory(new PropertyValueFactory<>("matchDate"));
        colScore   .setCellValueFactory(new PropertyValueFactory<>("score"));
        colKills   .setCellValueFactory(new PropertyValueFactory<>("kills"));
        colDeaths  .setCellValueFactory(new PropertyValueFactory<>("deaths"));
        colRank    .setCellValueFactory(new PropertyValueFactory<>("rankPosition"));
    }

    /**
     * Loads ALL match results from the database into the table.
     * Called when the user opens Match History tab or clicks "Show All".
     */
    @FXML
    public void loadAllResults() {
        try {
            List<MatchResult> results = matchResultDAO.getAllMatchResults();
            tableResults.setItems(FXCollections.observableArrayList(results));

            if (results.isEmpty()) {
                tableResults.setPlaceholder(
                    new Label("No match results found. Add one using 'Add Result'.")
                );
            }
        } catch (SQLException e) {
            showError("Could not load match history: " + e.getMessage());
        }
    }

    /**
     * Searches for players matching the typed keyword,
     * then loads match results for all matched players.
     *
     * Called when user clicks "Search" in Match History.
     */
    @FXML
    private void searchResults() {
        String keyword = tfSearch.getText().trim();

        // If search box is empty, just show everything
        if (keyword.isEmpty()) {
            loadAllResults();
            return;
        }

        try {
            List<Player> matchedPlayers = playerDAO.searchByUsername(keyword);

            if (matchedPlayers.isEmpty()) {
                // No player found with that name
                tableResults.setItems(FXCollections.observableArrayList());
                tableResults.setPlaceholder(
                    new Label("No player found matching: \"" + keyword + "\"")
                );
                return;
            }

            // Load results for the first matched player
            // (you could extend this to show results for all matches)
            Player first = matchedPlayers.get(0);
            List<MatchResult> results = matchResultDAO.getResultsByPlayer(first.getPlayerID());
            tableResults.setItems(FXCollections.observableArrayList(results));

            if (results.isEmpty()) {
                tableResults.setPlaceholder(
                    new Label(first.getUsername() + " has no match results yet.")
                );
            }

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LEADERBOARD — table setup and data loading
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Binds each leaderboard column to a field in LeaderboardEntry.
     */
    private void setupLeaderboardColumns() {
        colLbRank  .setCellValueFactory(new PropertyValueFactory<>("globalRank"));
        colLbPlayer.setCellValueFactory(new PropertyValueFactory<>("username"));
        colLbPoints.setCellValueFactory(new PropertyValueFactory<>("totalPoints"));
        colLbID    .setCellValueFactory(new PropertyValueFactory<>("playerID"));
    }

    /**
     * Loads top 50 players from the Leaderboard table.
     * Called every time the Leaderboard tab is opened (so it always reflects
     * the latest trigger-updated totals).
     */
    private void loadLeaderboard() {
        try {
            List<LeaderboardEntry> entries = leaderboardDAO.getTopPlayers();
            tableLeaderboard.setItems(FXCollections.observableArrayList(entries));

            if (entries.isEmpty()) {
                tableLeaderboard.setPlaceholder(
                    new Label("No leaderboard data yet. Add match results to populate it.")
                );
            }
        } catch (SQLException e) {
            showError("Could not load leaderboard: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STATUS LABEL HELPERS
    // ════════════════════════════════════════════════════════════════════════

    /** Shows a green success message in the status label */
    private void showSuccess(String msg) {
        lblStatus.setStyle("-fx-font-size:13px; -fx-text-fill:#4ecca3; -fx-font-weight:bold;");
        lblStatus.setText(msg);
    }

    /** Shows a red error message in the status label */
    private void showError(String msg) {
        lblStatus.setStyle("-fx-font-size:13px; -fx-text-fill:#e94560; -fx-font-weight:bold;");
        lblStatus.setText("✖  " + msg);
    }

    /** Clears the status label */
    private void clearStatus() {
        lblStatus.setText("");
    }
}
