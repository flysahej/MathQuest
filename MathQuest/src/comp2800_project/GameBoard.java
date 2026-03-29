package comp2800_project;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * GameBoard — main game window.
 *
 * Images loaded from assets/ folder (project root), mirroring Python Board.py
 * which used pygame.image.load("./assets/...").
 *
 * Image placement matches Python exactly:
 *   transparentduck.png → player tokens on board squares (50x50)
 *                         END square decoration (100x100)
 *                         duck power-up dialogs (64x64)
 *   pauseButton.png     → top-left pause button (40x40)
 *   checkmark.png       → answer-feedback dialog correct (150x150)
 *   redX.png            → answer-feedback dialog incorrect (150x150)
 *   StreakIcon.png       → score panel streak indicator (50x50)
 *   arrow.png           → player-moved notification (40x40)
 */
public class GameBoard extends JFrame {
    private static final Color BUTTON_TEXT_COLOR = Color.BLACK;
    private static final Color BUTTON_BORDER_COLOR = new Color(45, 45, 45);

    // ── Player state arrays ───────────────────────────────────────────────
    private String[] playerNames;
    private int[]    playerPositions;
    private int[]    playerScores;
    private int[]    playerStreaks;
    private int[]    playerDucks;
    private int      currentPlayer;
    private int      level;
    private String   sessionId;

    // ── UI ────────────────────────────────────────────────────────────────
    private JLabel  infoLabel;
    private JPanel  boardPanel;
    private JLabel  scoreLabel;
    private JButton duckButton;

    // ── Assets ────────────────────────────────────────────────────────────
    // Mirrors Python Board.py class-level scaled image variables
    private ImageIcon imgDuckSmall;       // 50x50  — player token on board squares
    private ImageIcon imgDuckLarge;       // 100x100 — END square
    private ImageIcon imgDuckMedium;      // 64x64  — dialogs
    private ImageIcon imgPause;           // 40x40  — pause button (top-left)
    private ImageIcon imgCheckmark;       // 150x150 — correct answer feedback
    private ImageIcon imgRedX;            // 150x150 — incorrect answer feedback
    private ImageIcon imgStreak;          // 50x50  — streak icon in scores
    private ImageIcon imgArrow;           // 40x40  — player-moved dialog

    // ── Database ──────────────────────────────────────────────────────────
    private final DatabaseManager db = DatabaseManager.getInstance();

    // ─────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────

    /** New game constructor. */
    public GameBoard(String[] playerNames, int level) {
        this.playerNames   = playerNames;
        this.level         = level;
        this.currentPlayer = 0;
        int n = playerNames.length;
        playerPositions = new int[n];
        playerScores    = new int[n];
        playerStreaks    = new int[n];
        playerDucks     = new int[n];
        Arrays.fill(playerDucks, 1); // each player starts with 1 duck
        sessionId = db.createGameSession(playerNames, level);
        loadAssets();
        setupUI();
    }

    /** Load-game constructor — restores a previously saved session. */
    public GameBoard(String[] playerNames, int[] scores, int[] streaks,
                     int[] ducks, int currentPlayer, int level) {
        this.playerNames   = playerNames;
        this.level         = level;
        this.currentPlayer = currentPlayer;
        int n = playerNames.length;
        playerPositions = new int[n];   // positions stored in player_index; use 0 as default
        playerScores    = scores;
        playerStreaks    = streaks;
        playerDucks     = ducks;
        sessionId = db.createGameSession(playerNames, level);
        loadAssets();
        setupUI();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Asset loading
    // ─────────────────────────────────────────────────────────────────────

    private void loadAssets() {
        imgDuckSmall   = scaled("assets/transparentduck.png",   50,  50);
        imgDuckLarge   = scaled("assets/transparentduck.png",  100, 100);
        imgDuckMedium  = scaled("assets/transparentduck.png",   64,  64);
        imgPause       = scaled("assets/pauseButton.png",       40,  40);
        imgCheckmark   = scaled("assets/checkmark.png",        150, 150);
        imgRedX        = scaled("assets/redX.png",             150, 150);
        imgStreak      = scaled("assets/StreakIcon.png",         50,  50);
        imgArrow       = scaled("assets/arrow.png",             40,  40);
    }

    private ImageIcon scaled(String path, int w, int h) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("[Assets] Missing: " + path);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // UI setup
    // ─────────────────────────────────────────────────────────────────────

    private void setupUI() {
        setTitle("MathQuest — Level " + level);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // ── Top bar ───────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBackground(Color.WHITE);

        // Pause button top-left — Python: win.blit(self.scaled_pause, (10, 10))
        JButton pauseBtn = new JButton(imgPause != null ? imgPause : new ImageIcon());
        if (imgPause == null) pauseBtn.setText("||");
        pauseBtn.setPreferredSize(new Dimension(52, 52));
        pauseBtn.setFocusPainted(false);
        pauseBtn.setBorder(BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 2));
        pauseBtn.setToolTipText("Pause");
        pauseBtn.addActionListener(e -> showPauseMenu());
        top.add(pauseBtn, BorderLayout.WEST);

        infoLabel = new JLabel("Current Turn: " + playerNames[0], SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        infoLabel.setForeground(new Color(50, 50, 150));
        top.add(infoLabel, BorderLayout.CENTER);

        scoreLabel = new JLabel("", SwingConstants.RIGHT);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        top.add(scoreLabel, BorderLayout.EAST);
        updateScoreDisplay();

        // ── Board 3x5 ─────────────────────────────────────────────────────
        boardPanel = new JPanel(new GridLayout(3, 5, 12, 12));
        boardPanel.setBackground(Color.WHITE);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        refreshBoard();

        // ── Control bar ───────────────────────────────────────────────────
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        ctrl.setBackground(Color.WHITE);

        JButton answerBtn = btn("Answer Question", new Color(100, 150, 255));
        answerBtn.addActionListener(e -> showQuestion());

        duckButton = btn("Use Duck (" + playerDucks[0] + ")", new Color(255, 200, 80));
        if (imgDuckSmall != null) duckButton.setIcon(imgDuckSmall);
        duckButton.addActionListener(e -> useDuck());

        JButton saveBtn = btn("Save Game",    new Color(100, 200, 100));
        JButton quitBtn = btn("Quit to Menu", new Color(200, 100, 100));
        saveBtn.addActionListener(e -> saveGame());
        quitBtn.addActionListener(e -> quitToMenu());

        ctrl.add(answerBtn);
        ctrl.add(duckButton);
        ctrl.add(saveBtn);
        ctrl.add(quitBtn);

        main.add(top,        BorderLayout.NORTH);
        main.add(boardPanel, BorderLayout.CENTER);
        main.add(ctrl,       BorderLayout.SOUTH);
        add(main);
        setVisible(true);
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(BUTTON_TEXT_COLOR);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Board rendering
    // ─────────────────────────────────────────────────────────────────────

    private void refreshBoard() {
        boardPanel.removeAll();
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 5; col++)
                boardPanel.add(makeSquare(row, col, row * 5 + col));
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    /**
     * One board square.
     * Python Board.drawBoard():
     *   row 0 → REDSQUARE, row 1 → PURPLESQUARE, row 2 → BLUESQUARE
     *   square 14 (END) gets scaled_duck_end (100x100)
     *   player positions get scaled_duck_player (50x50) tokens
     */
    private JPanel makeSquare(int row, int col, int pos) {
        JPanel sq = new JPanel(new BorderLayout(2, 2));
        sq.setBackground(squareColor(row));
        sq.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel num = new JLabel("  " + (pos + 1));
        num.setFont(new Font("Arial", Font.BOLD, 13));
        num.setForeground(Color.WHITE);
        sq.add(num, BorderLayout.NORTH);

        if (pos == 0) {
            // START
            JLabel lbl = new JLabel("START", SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            lbl.setForeground(Color.WHITE);
            sq.add(lbl, BorderLayout.SOUTH);
        } else if (pos == 14) {
            // END — Python: self.screen.blit(self.scaled_duck_end, ...)  100x100 duck
            JLabel endDuck = new JLabel(imgDuckLarge != null ? imgDuckLarge : new ImageIcon());
            if (imgDuckLarge == null) endDuck.setText("🏆");
            endDuck.setHorizontalAlignment(SwingConstants.CENTER);
            sq.add(endDuck, BorderLayout.CENTER);
            JLabel endLbl = new JLabel("END", SwingConstants.CENTER);
            endLbl.setFont(new Font("Arial", Font.BOLD, 12));
            endLbl.setForeground(Color.WHITE);
            sq.add(endLbl, BorderLayout.SOUTH);
            return sq;
        }

        // Player duck tokens — Python renderPlayers: scaled_duck_player 50x50
        JPanel tokens = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        tokens.setOpaque(false);
        for (int i = 0; i < playerNames.length; i++) {
            if (playerPositions[i] == pos) {
                JLabel tok;
                if (imgDuckSmall != null) {
                    tok = new JLabel(imgDuckSmall);
                } else {
                    tok = new JLabel(PLAYER_LETTERS[i % PLAYER_LETTERS.length]);
                    tok.setFont(new Font("Arial", Font.BOLD, 20));
                    tok.setForeground(Color.WHITE);
                }
                tok.setToolTipText(playerNames[i] + " — " + playerScores[i] + " pts");
                tokens.add(tok);
            }
        }
        sq.add(tokens, BorderLayout.CENTER);
        return sq;
    }

    private static final String[] PLAYER_LETTERS = {"A", "B", "C", "D"};

    private Color squareColor(int row) {
        switch (row) {
            case 0:  return new Color(210,  70,  70); // Red    — basic
            case 1:  return new Color(140,  70, 190); // Purple — secondary
            default: return new Color( 55,  95, 210); // Blue   — advanced
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Score display
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Score panel — Python Board.show_player_scores():
     *   StreakIcon.png 50x50 next to streak count
     *   duck image 50x50 next to duck count
     */
    private void updateScoreDisplay() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (int i = 0; i < playerNames.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
            row.setOpaque(false);

            JLabel name = new JLabel(playerNames[i] + ":  " + playerScores[i] + " pts");
            name.setFont(new Font("Arial", Font.BOLD, 13));
            row.add(name);

            // Streak icon — Python: self.screen.blit(self.scaled_streak, ...)
            if (playerStreaks[i] > 0) {
                if (imgStreak != null) row.add(new JLabel(imgStreak));
                else row.add(new JLabel("🔥"));
                row.add(new JLabel("x" + playerStreaks[i]));
            }

            // Duck icon — Python: self.screen.blit(self.scaled_duck_player, ...)
            if (playerDucks[i] > 0) {
                if (imgDuckSmall != null) row.add(new JLabel(imgDuckSmall));
                else row.add(new JLabel("🦆"));
                row.add(new JLabel("x" + playerDucks[i]));
            }

            panel.add(row);
        }

        // Replace scoreLabel content with a full panel embedded in label
        scoreLabel.setText(null);
        // Rebuild the east panel directly
        Container cp = getContentPane();
        if (cp.getComponentCount() > 0) {
            JPanel main = (JPanel) cp.getComponent(0);
            if (main.getComponentCount() > 0) {
                JPanel top = (JPanel) main.getComponent(0);
                top.remove(scoreLabel);
                scoreLabel = new JLabel();
                // Rebuild HTML score string (images shown as emoji fallback)
                StringBuilder html = new StringBuilder("<html>");
                for (int i = 0; i < playerNames.length; i++) {
                    html.append("<b>").append(playerNames[i]).append("</b>: ")
                        .append(playerScores[i]).append(" pts");
                    if (playerStreaks[i] > 0) html.append(" 🔥x").append(playerStreaks[i]);
                    if (playerDucks[i]   > 0) html.append(" 🦆x").append(playerDucks[i]);
                    html.append("<br>");
                }
                html.append("</html>");
                scoreLabel.setText(html.toString());
                scoreLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                top.add(scoreLabel, BorderLayout.EAST);
                top.revalidate();
                top.repaint();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Question flow
    // ─────────────────────────────────────────────────────────────────────

    private void showQuestion() {
        int row = playerPositions[currentPlayer] / 5;
        QuestionGenerator.Question q =
                new QuestionGenerator(level).generateQuestion(row);

        QuestionDialog dlg = new QuestionDialog(
                this, playerNames[currentPlayer], q.getText(), level);
        dlg.setVisible(true);

        String answer = dlg.getAnswer();

        if (answer == null) {
            // Timed out — Python ANSWER_AWAIT timeout branch
            playerStreaks[currentPlayer] = 0;
            JOptionPane.showMessageDialog(this,
                    "TIME'S UP!\n\nCorrect answer: " + q.getAnswer(),
                    "Time's Up", JOptionPane.WARNING_MESSAGE);
            db.logQuestion(sessionId, playerNames[currentPlayer],
                    q.getText(), q.getAnswer(), null, false, 0, row);
            nextPlayer();
        } else if (isCorrect(answer, q.getAnswer())) {
            int pts = calcPoints(row);
            playerScores[currentPlayer]  += pts;
            playerStreaks[currentPlayer]++;
            if (playerStreaks[currentPlayer] % 3 == 0) {
                playerDucks[currentPlayer]++;
                showDuckEarned();
            }
            db.logQuestion(sessionId, playerNames[currentPlayer],
                    q.getText(), q.getAnswer(), answer, true, pts, row);
            showAnswerFeedback(true, answer, q.getAnswer(), pts);
            movePlayer();
        } else {
            playerStreaks[currentPlayer] = 0;
            db.logQuestion(sessionId, playerNames[currentPlayer],
                    q.getText(), q.getAnswer(), answer, false, 0, row);
            showAnswerFeedback(false, answer, q.getAnswer(), 0);
            nextPlayer();
        }
    }

    private boolean isCorrect(String given, String expected) {
        try {
            return Math.abs(Double.parseDouble(given.trim())
                          - Double.parseDouble(expected.trim())) < 0.05;
        } catch (NumberFormatException e) {
            return given.trim().equalsIgnoreCase(expected.trim());
        }
    }

    private int calcPoints(int row) {
        // Python Board.update_points(): base × 2^level × 1.1^streak
        int base = (row == 0) ? 50 : (row == 1) ? 75 : 100;
        base = base * (int) Math.pow(2, level);
        return (int)(base * Math.pow(1.1, playerStreaks[currentPlayer]));
    }

    // ─────────────────────────────────────────────────────────────────────
    // Answer feedback  (checkmark.png / redX.png)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Python Board.show_answer_feedback():
     *   correct   → self.screen.blit(self.scaled_checkMark, ...)  checkmark.png 150x150
     *   incorrect → self.screen.blit(self.scaled_RedX, ...)       redX.png 150x150
     */
    private void showAnswerFeedback(boolean correct,
                                    String given, String expected, int pts) {
        JDialog dlg = new JDialog(this, "Answer Feedback", true);
        dlg.setSize(460, 440);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        // checkmark.png or redX.png — the core Python images for feedback
        ImageIcon feedbackImg = correct ? imgCheckmark : imgRedX;
        JLabel imgLbl = new JLabel(feedbackImg != null ? feedbackImg : new ImageIcon());
        if (feedbackImg == null) imgLbl.setText(correct ? "✅" : "❌");
        imgLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        imgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(correct ? "CORRECT!" : "INCORRECT!");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(correct ? new Color(0, 160, 0) : new Color(200, 0, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel yourAns = new JLabel("Your answer:    " + given);
        JLabel corrAns = new JLabel("Correct answer: " + expected);
        for (JLabel lbl : new JLabel[]{yourAns, corrAns}) {
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 15));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        panel.add(imgLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(yourAns);
        panel.add(corrAns);

        if (correct) {
            JLabel ptsLbl = new JLabel("+" + pts + " points!");
            ptsLbl.setFont(new Font("Arial", Font.BOLD, 17));
            ptsLbl.setForeground(new Color(0, 150, 0));
            ptsLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(Box.createVerticalStrut(8));
            panel.add(ptsLbl);

            // Streak icon — Python: self.screen.blit(self.scaled_streak, ...)
            if (playerStreaks[currentPlayer] > 0) {
                JPanel streakRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                streakRow.setOpaque(false);
                if (imgStreak != null) streakRow.add(new JLabel(imgStreak));
                else streakRow.add(new JLabel("🔥"));
                JLabel streakTxt = new JLabel("Streak: " + playerStreaks[currentPlayer]);
                streakTxt.setFont(new Font("Arial", Font.BOLD, 14));
                streakRow.add(streakTxt);
                panel.add(Box.createVerticalStrut(6));
                panel.add(streakRow);
            }
        }

        JButton ok = new JButton("Continue");
        ok.setForeground(BUTTON_TEXT_COLOR);
        ok.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        ok.addActionListener(e -> dlg.dispose());
        dlg.add(panel, BorderLayout.CENTER);
        dlg.add(ok,    BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Duck earned dialog
    // ─────────────────────────────────────────────────────────────────────

    /** Python Board.show_player_scores() duck award block. */
    private void showDuckEarned() {
        JDialog dlg = new JDialog(this, "Duck Earned!", true);
        dlg.setSize(340, 270);
        dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel duckImg = new JLabel(imgDuckMedium != null ? imgDuckMedium : new ImageIcon());
        if (imgDuckMedium == null) duckImg.setText("🦆");
        duckImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        duckImg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("DUCK EARNED!");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(200, 130, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("3 correct answers in a row!");
        sub.setFont(new Font("Arial", Font.PLAIN, 13));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(duckImg);
        p.add(Box.createVerticalStrut(8));
        p.add(title);
        p.add(sub);

        JButton ok = new JButton("Awesome!");
        ok.setForeground(BUTTON_TEXT_COLOR);
        ok.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        ok.addActionListener(e -> dlg.dispose());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(ok, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Duck power-up use
    // ─────────────────────────────────────────────────────────────────────

    private void useDuck() {
        if (playerDucks[currentPlayer] <= 0) {
            JOptionPane.showMessageDialog(this,
                    "No ducks! Answer 3 in a row to earn one.",
                    "No Ducks", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object[] msg = buildImgMsg(imgDuckMedium,
                "Use a Duck Power-Up to skip this question and move forward?");
        if (JOptionPane.showConfirmDialog(this, msg,
                "Duck Power-Up", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        playerDucks[currentPlayer]--;
        playerStreaks[currentPlayer]++;
        JOptionPane.showMessageDialog(this,
                playerNames[currentPlayer] + " used a Duck!\nMoving forward!",
                "Duck Used!", JOptionPane.INFORMATION_MESSAGE);
        movePlayer();
        if (duckButton != null)
            duckButton.setText("Use Duck (" + playerDucks[currentPlayer] + ")");
    }

    // ─────────────────────────────────────────────────────────────────────
    // Player movement
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Moves the current player and shows arrow.png in the notification.
     * Python Board.movePlayers().
     */
    private void movePlayer() {
        if (playerPositions[currentPlayer] >= 14) return;
        playerPositions[currentPlayer]++;
        refreshBoard();
        updateScoreDisplay();

        // arrow.png — movement indicator
        Object[] moveMsg = buildImgMsg(imgArrow,
                playerNames[currentPlayer] + " moved to position "
                + (playerPositions[currentPlayer] + 1) + "!");
        JOptionPane.showMessageDialog(this, moveMsg, "Move!", JOptionPane.PLAIN_MESSAGE);

        if (playerPositions[currentPlayer] == 14) {
            showVictory();
            return;
        }
        nextPlayer();
    }

    private void showVictory() {
        db.addHighScore(playerNames[currentPlayer], playerScores[currentPlayer], level);
        db.endGameSession(sessionId, playerNames[currentPlayer]);

        Object[] msg = buildImgMsg(imgDuckLarge,
                "WINNER!\n\n" + playerNames[currentPlayer] + " reached the END!\n\n"
                + "Final Score: " + playerScores[currentPlayer] + " pts");
        JOptionPane.showMessageDialog(this, msg, "WINNER!", JOptionPane.PLAIN_MESSAGE);
        dispose();
        new MainMenu();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Turn management
    // ─────────────────────────────────────────────────────────────────────

    private void nextPlayer() {
        currentPlayer = (currentPlayer + 1) % playerNames.length;
        infoLabel.setText("Current Turn: " + playerNames[currentPlayer]);
        if (duckButton != null)
            duckButton.setText("Use Duck (" + playerDucks[currentPlayer] + ")");
        updateScoreDisplay();

        JOptionPane.showMessageDialog(this,
                playerNames[currentPlayer] + "'s turn!\n\n"
                + "Position: " + (playerPositions[currentPlayer] + 1) + " / 15\n"
                + "Score:    " + playerScores[currentPlayer]    + " pts\n"
                + "Streak:   " + playerStreaks[currentPlayer],
                "Next Player", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Pause menu  (pauseButton.png)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Python Board.pause():
     *   shows pause button image, Resume / Save & Quit / Quit Without Saving
     */
    private void showPauseMenu() {
        JDialog dlg = new JDialog(this, "Paused", true);
        dlg.setSize(360, 340);
        dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // pauseButton.png — Python: self.screen.blit(self.scaled_pause, ...)
        JLabel pauseImg = new JLabel(imgPause != null ? imgPause : new ImageIcon());
        if (imgPause == null) pauseImg.setText("⏸");
        pauseImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        pauseImg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("PAUSED");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(new Color(50, 50, 150));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton resume = btn("Resume",           new Color(80,  170, 80));
        JButton saveQ  = btn("Save & Quit",      new Color(100, 150, 255));
        JButton noSave = btn("Quit Without Saving", new Color(200, 80, 80));
        for (JButton b : new JButton[]{resume, saveQ, noSave}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(240, 44));
        }

        resume.addActionListener(e -> dlg.dispose());
        saveQ.addActionListener(e -> { dlg.dispose(); saveGame(); dispose(); new MainMenu(); });
        noSave.addActionListener(e -> { dlg.dispose(); dispose(); new MainMenu(); });

        p.add(pauseImg); p.add(Box.createVerticalStrut(6));
        p.add(title);    p.add(Box.createVerticalStrut(18));
        p.add(resume);   p.add(Box.createVerticalStrut(8));
        p.add(saveQ);    p.add(Box.createVerticalStrut(8));
        p.add(noSave);
        dlg.add(p);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Save / Quit
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Save to slot 1/2/3 — Python Board.save_game() → insert_game():
     * { game_id, level_number, player_index, players:[{name,streak,duck_count,score}] }
     */
    private void saveGame() {
        String[] slots = {"Save Slot 1", "Save Slot 2", "Save Slot 3"};
        int choice = JOptionPane.showOptionDialog(this, "Choose save slot:", "Save Game",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, slots, slots[0]);
        if (choice < 0) return;

        int gameId = choice + 1;
        List<Map<String, Object>> plist =
                DatabaseManager.buildPlayerList(playerNames, playerScores,
                                                playerStreaks, playerDucks);
        db.insertGame(gameId, level, currentPlayer, plist);

        JOptionPane.showMessageDialog(this,
                "Saved to slot " + gameId + "!", "Game Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void quitToMenu() {
        if (JOptionPane.showConfirmDialog(this,
                "Quit to main menu? Unsaved progress will be lost.",
                "Quit", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            dispose();
            new MainMenu();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────

    /** Builds a JOptionPane message array with an image label above text. */
    private Object[] buildImgMsg(ImageIcon icon, String text) {
        if (icon == null) return new Object[]{text};
        return new Object[]{new JLabel(icon), text};
    }
}
