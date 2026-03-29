package comp2800_project;

import java.sql.*;
import java.util.*;

/**
 * DatabaseManager — SQLite data-access layer for MathQuest.
 *
 * Direct Java equivalent of the Python queryManager.py.
 * Uses a local SQLite file (mathquest.db) stored in the project folder.
 * No server, no internet, no credentials — works completely offline.
 *
 * Data structure mirrors the Python version exactly:
 * {
 *   game_id      : 1,          // save slot 1, 2, or 3
 *   level_number : 2,
 *   player_index : 7,          // current board position (0–14)
 *   players : [
 *     { name, password, streak, duck_count, score },
 *     ...
 *   ]
 * }
 *
 * ── Setup (do this once) ───────────────────────────────────────────────────
 * 1. Download the SQLite JDBC JAR (single file, ~10 MB):
 *    https://github.com/xerial/sqlite-jdbc/releases
 *    → sqlite-jdbc-3.x.x.jar
 *
 * 2. Add the JAR to IntelliJ:
 *    File → Project Structure → Libraries → "+" → Java → select the JAR → OK
 *
 * 3. That's it. The mathquest.db file is created automatically on first run.
 *
 * ── Python → Java method mapping ──────────────────────────────────────────
 *   insert_game(game)                →  insertGame(gameId, levelNumber, playerIndex, players)
 *   find_game_by_id(game_id)         →  findGameById(gameId)
 *   get_player_scores()              →  getPlayerScores()
 *   get_player_info()                →  getPlayerInfo()
 *   update_player_score(name, score) →  updatePlayerScore(name, score)
 */
public class DatabaseManager {

    // ── Database file location ─────────────────────────────────────────────
    // The .db file will be created in the root of your project folder.
    // Change this path if you want it stored elsewhere.
    private static final String DB_URL = "jdbc:sqlite:mathquest.db";

    // ── Singleton ──────────────────────────────────────────────────────────
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        connect();
        createTables();
    }

    /**
     * Returns the shared DatabaseManager instance.
     * Creates the database and tables on the very first call.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ── Connection ─────────────────────────────────────────────────────────

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            // Keep WAL mode for safer concurrent reads
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL;");
            }
            System.out.println("[DB] Connected → mathquest.db");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] SQLite JDBC driver not found. Add sqlite-jdbc-x.x.x.jar to your classpath.");
        } catch (SQLException e) {
            System.err.println("[DB] Connection error: " + e.getMessage());
        }
    }

    /** Closes the connection. Call this when the application exits. */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Close error: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ── Table creation (runs once on first launch) ─────────────────────────

    /**
     * Creates the two tables if they don't already exist.
     *
     * saved_games        – one row per save slot (game_id 1/2/3)
     * saved_game_players – one row per player inside each save slot
     */
    private void createTables() {
        String savedGames =
            "CREATE TABLE IF NOT EXISTS saved_games ("
          + "  game_id      INTEGER PRIMARY KEY,"   // 1, 2, or 3  (same as Python game_id)
          + "  level_number INTEGER NOT NULL,"
          + "  player_index INTEGER NOT NULL,"      // legacy field kept for compatibility
          + "  current_player INTEGER NOT NULL DEFAULT 0"
          + ");";

        String savedPlayers =
            "CREATE TABLE IF NOT EXISTS saved_game_players ("
          + "  id           INTEGER PRIMARY KEY AUTOINCREMENT,"
          + "  game_id      INTEGER NOT NULL,"
          + "  player_order INTEGER NOT NULL,"      // preserves list order
          + "  name         TEXT    NOT NULL,"
          + "  password     TEXT    NOT NULL DEFAULT '',"
          + "  streak       INTEGER NOT NULL DEFAULT 0,"
          + "  duck_count   INTEGER NOT NULL DEFAULT 0,"
          + "  position     INTEGER NOT NULL DEFAULT 0,"
          + "  score        REAL    NOT NULL DEFAULT 0,"
          + "  FOREIGN KEY (game_id) REFERENCES saved_games(game_id) ON DELETE CASCADE"
          + ");";

        String highScores =
            "CREATE TABLE IF NOT EXISTS high_scores ("
          + "  id          INTEGER PRIMARY KEY AUTOINCREMENT,"
          + "  player_name TEXT    NOT NULL,"
          + "  score       INTEGER NOT NULL,"
          + "  level       INTEGER NOT NULL,"
          + "  achieved_at TEXT    NOT NULL"
          + ");";

        String gameSessions =
            "CREATE TABLE IF NOT EXISTS game_sessions ("
          + "  session_id   TEXT    PRIMARY KEY,"
          + "  level        INTEGER NOT NULL,"
          + "  player_names TEXT    NOT NULL,"
          + "  started_at   TEXT    NOT NULL,"
          + "  ended_at     TEXT,"
          + "  winner       TEXT"
          + ");";

        String questionLog =
            "CREATE TABLE IF NOT EXISTS question_log ("
          + "  id             INTEGER PRIMARY KEY AUTOINCREMENT,"
          + "  session_id     TEXT    NOT NULL,"
          + "  player_name    TEXT    NOT NULL,"
          + "  question_text  TEXT    NOT NULL,"
          + "  correct_answer TEXT    NOT NULL,"
          + "  player_answer  TEXT,"
          + "  correct        INTEGER NOT NULL,"
          + "  points_earned  INTEGER NOT NULL DEFAULT 0,"
          + "  row_number     INTEGER NOT NULL,"
          + "  answered_at    TEXT    NOT NULL"
          + ");";

        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
            st.execute(savedGames);
            st.execute(savedPlayers);
            st.execute(highScores);
            st.execute(gameSessions);
            st.execute(questionLog);
            ensureSaveSchema(st);
            System.out.println("[DB] Tables ready.");
        } catch (SQLException e) {
            System.err.println("[DB] createTables error: " + e.getMessage());
        }
    }

    private void ensureSaveSchema(Statement st) throws SQLException {
        Set<String> savedGameColumns = getColumnNames("saved_games");
        if (!savedGameColumns.contains("current_player")) {
            st.execute("ALTER TABLE saved_games ADD COLUMN current_player INTEGER NOT NULL DEFAULT 0");
        }

        Set<String> savedPlayerColumns = getColumnNames("saved_game_players");
        if (!savedPlayerColumns.contains("position")) {
            st.execute("ALTER TABLE saved_game_players ADD COLUMN position INTEGER NOT NULL DEFAULT 0");
        }
    }

    private Set<String> getColumnNames(String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement pragma = connection.createStatement();
             ResultSet rs = pragma.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }
        return columns;
    }

    // =========================================================================
    //  CORE OPERATIONS  (mirrors queryManager.py exactly)
    // =========================================================================

    /**
     * Inserts or updates a save slot — equivalent to Python's insert_game().
     *
     * Python equivalent:
     *   insert_game({"game_id":1, "level_number":2, "player_index":7,
     *                "players":[{"name":"Alice","password":"pw","streak":1,
     *                            "duck_count":0,"score":300.0}]})
     *
     * @param gameId       Save slot number (1, 2, or 3).
     * @param levelNumber  Current game level.
     * @param playerIndex  Current board position (0–14).
     * @param players      List of player maps, each with keys:
     *                     name, password, streak, duck_count, score
     */
    public void insertGame(int gameId,
                           int levelNumber,
                           int currentPlayer,
                           List<Map<String, Object>> players) {
        try {
            connection.setAutoCommit(false);

            // Upsert the header row (INSERT OR REPLACE handles the slot overwrite)
            String upsertHeader =
                "INSERT OR REPLACE INTO saved_games "
              + "(game_id, level_number, player_index, current_player) "
              + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(upsertHeader)) {
                ps.setInt(1, gameId);
                ps.setInt(2, levelNumber);
                ps.setInt(3, currentPlayer);
                ps.setInt(4, currentPlayer);
                ps.executeUpdate();
            }

            // Clear old player rows for this slot, then re-insert
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM saved_game_players WHERE game_id = ?")) {
                ps.setInt(1, gameId);
                ps.executeUpdate();
            }

            String insertPlayer =
                "INSERT INTO saved_game_players "
              + "(game_id, player_order, name, password, streak, duck_count, position, score) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertPlayer)) {
                for (int i = 0; i < players.size(); i++) {
                    Map<String, Object> p = players.get(i);
                    ps.setInt(1, gameId);
                    ps.setInt(2, i);
                    ps.setString(3, getString(p, "name"));
                    ps.setString(4, getString(p, "password"));
                    ps.setInt(5, getInt(p, "streak"));
                    ps.setInt(6, getInt(p, "duck_count"));
                    ps.setInt(7, getInt(p, "position"));
                    ps.setDouble(8, getDouble(p, "score"));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
            System.out.println("[DB] Game saved to slot " + gameId);

        } catch (SQLException e) {
            System.err.println("[DB] insertGame error: " + e.getMessage());
            try { connection.rollback(); } catch (SQLException ex) { /* ignored */ }
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { /* ignored */ }
        }
    }

    /**
     * Convenience overload that accepts the same Map structure the Python
     * queryManager used, so you can call it from GameBoard like this:
     *
     *   Map<String,Object> game = new HashMap<>();
     *   game.put("game_id", 1);
     *   game.put("level_number", 2);
     *   game.put("player_index", 7);
     *   game.put("players", players);
     *   db.insertGame(game);
     *
     * @param game Map with keys: game_id, level_number, player_index, players
     */
    @SuppressWarnings("unchecked")
    public void insertGame(Map<String, Object> game) {
        insertGame(
            getInt(game, "game_id"),
            getInt(game, "level_number"),
            getInt(game, "player_index"),
            (List<Map<String, Object>>) game.get("players")
        );
    }

    /**
     * Loads a saved game by its slot number — equivalent to Python's find_game_by_id().
     *
     * Python equivalent:
     *   game = find_game_by_id(1)
     *   # game["players"][0]["name"] → "Alice"
     *
     * Returns a Map with keys: game_id, level_number, player_index, players
     * Returns null if the slot is empty.
     *
     * Usage in GameBoard:
     *   Map<String,Object> game = db.findGameById(1);
     *   if (game != null) {
     *       int level       = (int) game.get("level_number");
     *       int playerIndex = (int) game.get("player_index");
     *       List<Map<String,Object>> players =
     *           (List<Map<String,Object>>) game.get("players");
     *       String name = (String) players.get(0).get("name");
     *   }
     *
     * @param gameId Save slot number (1, 2, or 3).
     * @return Map representing the saved game, or null if not found.
     */
    public Map<String, Object> findGameById(int gameId) {
        String sql = "SELECT game_id, level_number, player_index, current_player FROM saved_games WHERE game_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("[DB] Slot " + gameId + " is empty.");
                return null;
            }

            Map<String, Object> game = new LinkedHashMap<>();
            game.put("game_id",      rs.getInt("game_id"));
            game.put("level_number", rs.getInt("level_number"));
            game.put("player_index", rs.getInt("player_index"));
            game.put("current_player", rs.getInt("current_player"));

            // Load the players list
            List<Map<String, Object>> players = new ArrayList<>();
            String playerSql =
                "SELECT name, password, streak, duck_count, position, score "
              + "FROM saved_game_players WHERE game_id = ? ORDER BY player_order ASC";
            try (PreparedStatement ps2 = connection.prepareStatement(playerSql)) {
                ps2.setInt(1, gameId);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    Map<String, Object> player = new LinkedHashMap<>();
                    player.put("name",       rs2.getString("name"));
                    player.put("password",   rs2.getString("password"));
                    player.put("streak",     rs2.getInt("streak"));
                    player.put("duck_count", rs2.getInt("duck_count"));
                    player.put("position",   rs2.getInt("position"));
                    player.put("score",      rs2.getDouble("score"));
                    players.add(player);
                }
            }
            game.put("players", players);
            return game;

        } catch (SQLException e) {
            System.err.println("[DB] findGameById error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns a map of player name → score across all saved games.
     * Equivalent to Python's get_player_scores().
     *
     * Python equivalent:
     *   scores = get_player_scores()
     *   # scores["Alice"] → 300.0
     *
     * @return Map of { playerName → score }
     */
    public Map<String, Double> getPlayerScores() {
        Map<String, Double> scores = new LinkedHashMap<>();
        String sql = "SELECT name, score FROM saved_game_players";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                scores.put(rs.getString("name"), rs.getDouble("score"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] getPlayerScores error: " + e.getMessage());
        }
        return scores;
    }

    /**
     * Returns detailed info for every player across all saved games.
     * Equivalent to Python's get_player_info().
     *
     * Python equivalent:
     *   info = get_player_info()
     *   # info["Alice"]["streak"]     → 2
     *   # info["Alice"]["duck_count"] → 1
     *   # info["Alice"]["score"]      → 300.0
     *
     * @return Map of { playerName → { streak, duck_count, score } }
     */
    public Map<String, Map<String, Object>> getPlayerInfo() {
        Map<String, Map<String, Object>> info = new LinkedHashMap<>();
        String sql = "SELECT name, streak, duck_count, score FROM saved_game_players";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("streak",     rs.getInt("streak"));
                details.put("duck_count", rs.getInt("duck_count"));
                details.put("score",      rs.getDouble("score"));
                info.put(rs.getString("name"), details);
            }
        } catch (SQLException e) {
            System.err.println("[DB] getPlayerInfo error: " + e.getMessage());
        }
        return info;
    }

    /**
     * Updates a player's score in every saved game they appear in.
     * Equivalent to Python's update_player_score().
     *
     * Python equivalent:
     *   update_player_score("Alice", 450.0)
     *
     * @param playerName The player's display name.
     * @param score      The new score value.
     */
    public void updatePlayerScore(String playerName, double score) {
        String sql = "UPDATE saved_game_players SET score = ? WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, score);
            ps.setString(2, playerName);
            int rows = ps.executeUpdate();
            System.out.println("[DB] updatePlayerScore: " + rows + " row(s) updated for " + playerName);
        } catch (SQLException e) {
            System.err.println("[DB] updatePlayerScore error: " + e.getMessage());
        }
    }

    /**
     * Deletes a save slot (game_id 1, 2, or 3) entirely.
     * Player rows are removed automatically via CASCADE.
     *
     * @param gameId Save slot to delete.
     */
    public void deleteGame(int gameId) {
        String sql = "DELETE FROM saved_games WHERE game_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            ps.executeUpdate();
            System.out.println("[DB] Slot " + gameId + " deleted.");
        } catch (SQLException e) {
            System.err.println("[DB] deleteGame error: " + e.getMessage());
        }
    }

    /**
     * Returns which save slots (1/2/3) currently contain data.
     * Useful for greying out empty slots in the Load Game menu.
     *
     * @return List of occupied slot numbers, e.g. [1, 3]
     */
    public List<Integer> getOccupiedSlots() {
        List<Integer> slots = new ArrayList<>();
        String sql = "SELECT game_id FROM saved_games ORDER BY game_id";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                slots.add(rs.getInt("game_id"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] getOccupiedSlots error: " + e.getMessage());
        }
        return slots;
    }

    // =========================================================================
    //  HELPER UTILITIES
    // =========================================================================

    /**
     * Builds the player Map structure expected by insertGame()
     * directly from the GameBoard arrays — convenience method so
     * GameBoard doesn't have to build maps manually.
     *
     * Usage:
     *   List<Map<String,Object>> players = DatabaseManager.buildPlayerList(
     *       playerNames, playerScores, playerStreaks, playerDucks);
     *   db.insertGame(slot, level, currentPlayerIndex, players);
     *
     * @param names   Player display names.
     * @param scores  Cumulative scores.
     * @param streaks Current streaks.
     * @param ducks   Duck counts.
     * @return List of player maps ready for insertGame().
     */
    public static List<Map<String, Object>> buildPlayerList(String[] names,
                                                            int[]    scores,
                                                            int[]    streaks,
                                                            int[]    ducks,
                                                            int[]    positions) {
        List<Map<String, Object>> players = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("name",       names[i]);
            p.put("password",   "");          // Java version has no password field
            p.put("streak",     streaks[i]);
            p.put("duck_count", ducks[i]);
            p.put("position",   positions[i]);
            p.put("score",      (double) scores[i]);
            players.add(p);
        }
        return players;
    }

    // ── Type-safe map readers ──────────────────────────────────────────────

    private static String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    static int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val != null) {
            try { return Integer.parseInt(val.toString()); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private static double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val != null) {
            try { return Double.parseDouble(val.toString()); } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }

    // =========================================================================
    //  DEMO MAIN — run this first to confirm everything works
    // =========================================================================

    /**
     * Smoke test that mirrors the Python insert.py and queryManagerUnitTesting.py.
     * Run it standalone:  right-click DatabaseManager.java → Run 'DatabaseManager.main()'
     */
    @SuppressWarnings("unchecked")

    // =========================================================================
    //  GAME SESSION + HIGH SCORES  (used by GameBoard)
    // =========================================================================

    /** Creates a game session record. Returns a unique session ID. */
    public String createGameSession(String[] playerNames, int level) {
        String sessionId = "session_" + System.currentTimeMillis()
                         + "_" + (int)(Math.random() * 10000);
        if (playerNames == null || playerNames.length == 0) return sessionId;
        String sql = "INSERT INTO game_sessions "
                   + "(session_id, level, player_names, started_at) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setInt   (2, level);
            ps.setString(3, String.join(",", playerNames));
            ps.setString(4, java.time.Instant.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] createGameSession error: " + e.getMessage());
        }
        return sessionId;
    }

    /** Logs a single question-answer event to the session. */
    public void logQuestion(String sessionId, String playerName,
                            String questionText, String correctAnswer,
                            String playerAnswer, boolean correct,
                            int pointsEarned, int row) {
        String sql = "INSERT INTO question_log "
                   + "(session_id,player_name,question_text,correct_answer,"
                   + " player_answer,correct,points_earned,row_number,answered_at) "
                   + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString (1, sessionId);
            ps.setString (2, playerName);
            ps.setString (3, questionText);
            ps.setString (4, correctAnswer);
            ps.setString (5, playerAnswer);
            ps.setInt    (6, correct ? 1 : 0);
            ps.setInt    (7, pointsEarned);
            ps.setInt    (8, row);
            ps.setString (9, java.time.Instant.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] logQuestion error: " + e.getMessage());
        }
    }

    /** Records a winner and end time for a session. */
    public void endGameSession(String sessionId, String winner) {
        String sql = "UPDATE game_sessions SET ended_at=?, winner=? WHERE session_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, java.time.Instant.now().toString());
            ps.setString(2, winner);
            ps.setString(3, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] endGameSession error: " + e.getMessage());
        }
    }

    /** Adds a high-score entry. Called when a player wins the game. */
    public void addHighScore(String playerName, int score, int level) {
        String sql = "INSERT INTO high_scores (player_name,score,level,achieved_at) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerName);
            ps.setInt   (2, score);
            ps.setInt   (3, level);
            ps.setString(4, java.time.Instant.now().toString());
            ps.executeUpdate();
            System.out.println("[DB] High score: " + playerName + " — " + score);
        } catch (SQLException e) {
            System.err.println("[DB] addHighScore error: " + e.getMessage());
        }
    }

    /**
     * Returns the top-N high scores as a list of maps.
     * Used by MainMenu to populate the High Scores screen.
     * Each map has keys: rank, player_name, score, level, achieved_at
     */
    public List<Map<String, Object>> getTopHighScores(int limit) {
        List<Map<String, Object>> scores = new ArrayList<>();
        String sql = "SELECT player_name,score,level,achieved_at "
                   + "FROM high_scores ORDER BY score DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("rank",        rank++);
                row.put("player_name", rs.getString("player_name"));
                row.put("score",       rs.getInt("score"));
                row.put("level",       rs.getInt("level"));
                row.put("achieved_at", rs.getString("achieved_at"));
                scores.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[DB] getTopHighScores error: " + e.getMessage());
        }
        return scores;
    }

    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.getInstance();

        // ── 1. Build player list (same shape as Python) ────────────────────
        List<Map<String, Object>> players = new ArrayList<>();
        Map<String, Object> p1 = new LinkedHashMap<>();
        p1.put("name",       "Player4");
        p1.put("password",   "password123");
        p1.put("streak",     2);
        p1.put("duck_count", 0);
        p1.put("score",      250.0);
        players.add(p1);

        // ── 2. insertGame (mirrors Python insert_game) ─────────────────────
        Map<String, Object> game = new LinkedHashMap<>();
        game.put("game_id",      1);
        game.put("level_number", 2);
        game.put("player_index", 14);
        game.put("players",      players);
        db.insertGame(game);

        // ── 3. findGameById (mirrors Python find_game_by_id) ───────────────
        Map<String, Object> retrieved = db.findGameById(1);
        System.out.println("Retrieved game_id: "      + retrieved.get("game_id"));
        System.out.println("Level:             "      + retrieved.get("level_number"));
        System.out.println("Player index:      "      + retrieved.get("player_index"));
        List<Map<String,Object>> rPlayers =
                (List<Map<String,Object>>) retrieved.get("players");
        System.out.println("Player name:       "      + rPlayers.get(0).get("name"));
        System.out.println("Player score:      "      + rPlayers.get(0).get("score"));

        // ── 4. getPlayerScores (mirrors Python get_player_scores) ──────────
        Map<String, Double> scores = db.getPlayerScores();
        System.out.println("Player scores:     "      + scores);

        // ── 5. getPlayerInfo (mirrors Python get_player_info) ──────────────
        Map<String, Map<String,Object>> info = db.getPlayerInfo();
        System.out.println("Player info:       "      + info);

        // ── 6. updatePlayerScore (mirrors Python update_player_score) ──────
        db.updatePlayerScore("Player4", 999.0);
        System.out.println("After update:      "      + db.getPlayerScores());

        // ── 7. Occupied slots ──────────────────────────────────────────────
        System.out.println("Occupied slots:    "      + db.getOccupiedSlots());

        db.close();
        System.out.println("\nAll tests passed!");
    }
}
