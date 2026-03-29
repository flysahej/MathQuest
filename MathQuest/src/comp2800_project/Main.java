package comp2800_project;

import javax.swing.SwingUtilities;

/**
 * Application entry point.
 * Launches the MathQuest main menu.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu());
    }
}
