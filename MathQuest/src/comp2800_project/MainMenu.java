package comp2800_project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame {
    private static final Color BUTTON_TEXT_COLOR = Color.BLACK;
    private static final Color BUTTON_BORDER_COLOR = new Color(45, 45, 45);
    
    public MainMenu() {
        // Setup the main window
        setTitle("MathQuest - Main Menu");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);
        
        // Create main panel with background color
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(255, 255, 255)); // White background like Python version
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(100, 200, 100, 200));
        
        // Title / Logo area (like the duck in Python version)
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(255, 255, 255));
        
        JLabel duckLabel = new JLabel("🦆");
        duckLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        titlePanel.add(duckLabel);
        
        JLabel titleLabel = new JLabel("MATHQUEST");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(50, 50, 150));
        titlePanel.add(titleLabel);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Master Math Through Play!");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        titlePanel.add(subtitleLabel);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(new Color(255, 255, 255));
        buttonsPanel.setLayout(new GridLayout(6, 1, 10, 15));
        
        // Create buttons (like the Python version)
        JButton startButton = createStyledButton("Start Game", new Color(100, 150, 255));
        JButton tutorialButton = createStyledButton("Tutorial", new Color(150, 100, 255));
        JButton loadButton = createStyledButton("Load Save", new Color(100, 200, 100));
        JButton highScoresButton = createStyledButton("High Scores", new Color(255, 180, 100));
        JButton devButton = createStyledButton("Developer Login", new Color(200, 100, 100));
        JButton quitButton = createStyledButton("Quit", new Color(150, 150, 150));
        
        // Add action listeners
        startButton.addActionListener(e -> startNewGame());
        tutorialButton.addActionListener(e -> showTutorial());
        loadButton.addActionListener(e -> loadGame());
        highScoresButton.addActionListener(e -> showHighScores());
        devButton.addActionListener(e -> developerLogin());
        quitButton.addActionListener(e -> System.exit(0));
        
        // Add buttons to panel
        buttonsPanel.add(startButton);
        buttonsPanel.add(tutorialButton);
        buttonsPanel.add(loadButton);
        buttonsPanel.add(highScoresButton);
        buttonsPanel.add(devButton);
        buttonsPanel.add(quitButton);
        
        // Assemble everything
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacing
        mainPanel.add(buttonsPanel);
        
        add(mainPanel);
        setVisible(true);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void startNewGame() {
        try {
            // Ask for number of players
            String playerCountStr = JOptionPane.showInputDialog(this, 
                "How many players? (1-4)", 
                "Player Setup", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (playerCountStr == null) {
                return; // User cancelled
            }
            
            int playerCount = Integer.parseInt(playerCountStr);
            if (playerCount >= 1 && playerCount <= 4) {
                // Create player list
                String[] playerNames = new String[playerCount];
                for (int i = 0; i < playerCount; i++) {
                    playerNames[i] = JOptionPane.showInputDialog(this, 
                        "Enter name for Player " + (i + 1) + ":", 
                        "Player Name", 
                        JOptionPane.QUESTION_MESSAGE);
                    if (playerNames[i] == null || playerNames[i].trim().isEmpty()) {
                        playerNames[i] = "Player " + (i + 1);
                    }
                }
                
                // Ask for difficulty level
                String[] levels = {"Level 1 (Easy)", "Level 2 (Medium)", "Level 3 (Hard)"};
                int levelChoice = JOptionPane.showOptionDialog(this,
                    "Select Difficulty Level:",
                    "Game Difficulty",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    levels,
                    levels[0]);
                
                if (levelChoice == JOptionPane.CLOSED_OPTION) {
                    return; // User cancelled
                }
                
                int level = levelChoice + 1;
                
                // Launch the game board
                System.out.println("Starting game with " + playerCount + " players at level " + level);
                dispose(); // Close menu
                
                // Create GameBoard in Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    try {
                        new GameBoard(playerNames, level);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, 
                            "Error starting game: " + ex.getMessage(),
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        // Reopen main menu if game fails
                        new MainMenu();
                    }
                });
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a number between 1 and 4", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid number", 
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showTutorial() {
        String tutorialText = "HOW TO PLAY MATHQUEST\n\n" +
                              "═══════════════════════════════════\n\n" +
                              "🎮 GAME OVERVIEW\n" +
                              "Players take turns answering math questions to move across the board.\n\n" +
                              "🎲 BOARD LAYOUT\n" +
                              "• Row 1 (Red): Addition & Subtraction\n" +
                              "• Row 2 (Purple): Multiplication & Division\n" +
                              "• Row 3 (Blue): Quadratics & Linear Equations\n\n" +
                              "⏱️ TIMER\n" +
                              "You have 30 seconds to answer each question!\n" +
                              "Answer quickly for bonus points!\n\n" +
                              "🦆 DUCK POWER-UP\n" +
                              "Get 3 correct answers in a row to earn a duck!\n" +
                              "Ducks let you skip a hard question automatically.\n\n" +
                              "⭐ SCORING\n" +
                              "• Row 1: 50 points × Level\n" +
                              "• Row 2: 75 points × Level\n" +
                              "• Row 3: 100 points × Level\n" +
                              "• Streak Bonus: +10% per correct answer!\n\n" +
                              "🏆 WINNING\n" +
                              "First player to reach the END square wins!\n\n" +
                              "Good luck, and have fun! 🎉";
        
        JTextArea textArea = new JTextArea(tutorialText);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setMargin(new Insets(20, 20, 20, 20));
        textArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        
        JOptionPane.showMessageDialog(this, 
            scrollPane, 
            "📚 Tutorial - How to Play MathQuest", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    @SuppressWarnings("unchecked")
    private void loadGame() {
        DatabaseManager db = DatabaseManager.getInstance();
        java.util.List<Integer> occupied = db.getOccupiedSlots();
        if (occupied.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No saved games found.\nStart a new game and save it first!",
                "No Saves", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] slotLabels = {"Save Slot 1", "Save Slot 2", "Save Slot 3"};
        int choice = JOptionPane.showOptionDialog(this,
            "Select a save slot to load:",
            "Load Game",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, slotLabels, slotLabels[0]);
        if (choice < 0) return;
        int gameId = choice + 1;
        java.util.Map<String, Object> save = db.findGameById(gameId);
        if (save == null) {
            JOptionPane.showMessageDialog(this,
                "Save slot " + gameId + " is empty.",
                "Empty Slot", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int level       = DatabaseManager.getInt(save, "level_number");
        int playerIndex = DatabaseManager.getInt(save, "player_index");
        java.util.List<java.util.Map<String,Object>> players =
            (java.util.List<java.util.Map<String,Object>>) save.get("players");
        String[] names = new String[players.size()];
        int[] scores   = new int[players.size()];
        int[] streaks  = new int[players.size()];
        int[] ducks    = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            names[i]   = (String) players.get(i).get("name");
            scores[i]  = DatabaseManager.getInt(players.get(i), "score");
            streaks[i] = DatabaseManager.getInt(players.get(i), "streak");
            ducks[i]   = DatabaseManager.getInt(players.get(i), "duck_count");
        }
        dispose();
        SwingUtilities.invokeLater(() ->
            new GameBoard(names, scores, streaks, ducks, playerIndex, level));
    }
    
    private void showHighScores() {
        DatabaseManager db = DatabaseManager.getInstance();
        java.util.List<java.util.Map<String,Object>> top = db.getTopHighScores(10);
        StringBuilder sb = new StringBuilder("🏆  TOP HIGH SCORES  🏆\n\n");
        if (top.isEmpty()) {
            sb.append("No scores yet — finish a game to get on the board!");
        } else {
            for (java.util.Map<String,Object> entry : top) {
                sb.append(String.format("%-3s %-15s %,d pts   (Level %s)%n",
                    entry.get("rank") + ".",
                    entry.get("player_name"),
                    entry.get("score"),
                    entry.get("level")));
            }
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(20, 20, 20, 20));
        JOptionPane.showMessageDialog(this, textArea,
            "High Scores", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void developerLogin() {
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(this, 
            passwordField, 
            "Enter Developer Password:", 
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            if (password.equals("admin123")) {
                showDeveloperTools();
            } else if (password != null) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Invalid Password\nAccess Denied!", 
                    "Developer Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showDeveloperTools() {
        JDialog devDialog = new JDialog(this, "Developer Tools", true);
        devDialog.setSize(500, 400);
        devDialog.setLocationRelativeTo(this);
        devDialog.setLayout(new BorderLayout());
        
        JTextArea devArea = new JTextArea();
        devArea.setEditable(false);
        devArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        devArea.setText("🔧 DEVELOPER TOOLS 🔧\n\n" +
                        "Available Features:\n" +
                        "• View Player Statistics\n" +
                        "• Modify Game Parameters\n" +
                        "• Edit Question Database\n" +
                        "• View MongoDB Connection\n" +
                        "• Debug Game State\n\n" +
                        "Coming Soon: Full developer console\n" +
                        "with real-time game manipulation!");
        devArea.setMargin(new Insets(20, 20, 20, 20));
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> devDialog.dispose());
        
        devDialog.add(new JScrollPane(devArea), BorderLayout.CENTER);
        devDialog.add(closeButton, BorderLayout.SOUTH);
        
        devDialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        // Run the menu in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MainMenu());
    }
}
