package comp2800_project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class QuestionDialog extends JDialog {
    private static final Color BUTTON_TEXT_COLOR = Color.BLACK;
    private static final Color BUTTON_BORDER_COLOR = new Color(45, 45, 45);

    private String answer;
    private boolean answered = false;
    private JTextField answerField;
    private JProgressBar timerBar;
    private Timer timer;
    private int timeLeft;
    
    public QuestionDialog(JFrame parent, String playerName, String question, int level) {
        super(parent, "Math Question - Level " + level, true);
        this.timeLeft = 30; // 30 seconds
        setupUI(playerName, question);
    }
    
    private void setupUI(String playerName, String question) {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Top panel with player name and duck
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(playerName + "'s Question");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel duckLabel = new JLabel("🦆");
        duckLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        duckLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        topPanel.add(nameLabel, BorderLayout.CENTER);
        topPanel.add(duckLabel, BorderLayout.EAST);
        
        // Question panel
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBackground(new Color(240, 240, 240));
        questionPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        JTextArea questionArea = new JTextArea(question);
        questionArea.setFont(new Font("Monospaced", Font.BOLD, 20));
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setBackground(new Color(240, 240, 240));
        questionArea.setMargin(new Insets(40, 40, 40, 40));
        
        questionPanel.add(questionArea, BorderLayout.CENTER);
        
        // Answer panel
        JPanel answerPanel = new JPanel(new BorderLayout(10, 10));
        answerPanel.setBorder(BorderFactory.createTitledBorder("Your Answer"));
        
        answerField = new JTextField();
        answerField.setFont(new Font("Monospaced", Font.PLAIN, 18));
        answerField.addActionListener(e -> checkAnswer());
        
        JButton submitButton = new JButton("Submit Answer");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBackground(new Color(100, 150, 255));
        submitButton.setForeground(BUTTON_TEXT_COLOR);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        submitButton.addActionListener(e -> checkAnswer());
        
        answerPanel.add(answerField, BorderLayout.CENTER);
        answerPanel.add(submitButton, BorderLayout.EAST);
        
        // Timer panel
        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.setBorder(BorderFactory.createTitledBorder("Time Remaining"));
        
        timerBar = new JProgressBar(0, 30);
        timerBar.setValue(30);
        timerBar.setStringPainted(true);
        timerBar.setFont(new Font("Arial", Font.BOLD, 14));
        timerBar.setForeground(new Color(100, 200, 100));
        timerBar.setBackground(Color.LIGHT_GRAY);
        
        timerPanel.add(timerBar, BorderLayout.CENTER);
        
        // Assemble main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(questionPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.add(answerPanel, BorderLayout.NORTH);
        southPanel.add(timerPanel, BorderLayout.SOUTH);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Start timer
        startTimer();
        
        // Focus on answer field
        answerField.requestFocusInWindow();
        
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
    
    private void startTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerBar.setValue(timeLeft);
                timerBar.setString(timeLeft + " seconds left");
                
                // Change color as time runs out
                if (timeLeft <= 10) {
                    timerBar.setForeground(new Color(255, 100, 100));
                } else if (timeLeft <= 20) {
                    timerBar.setForeground(new Color(255, 200, 100));
                }
                
                if (timeLeft <= 0) {
                    timer.stop();
                    answer = null; // Time's up = wrong answer
                    answered = true;
                    dispose();
                }
            }
        });
        timer.start();
    }
    
    private void checkAnswer() {
        if (!answered) {
            timer.stop();
            answer = answerField.getText().trim();
            answered = true;
            dispose();
        }
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public boolean wasAnswered() {
        return answered && answer != null;
    }
}
