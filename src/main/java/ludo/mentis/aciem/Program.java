package ludo.mentis.aciem;

import javax.swing.SwingUtilities;

/**
 * Application entry point.
 * Responsible for initializing and launching the main application window.
 */
public class Program {

    public static void main(String[] args) {
        // Ensures that Locale is US before any rendering
        java.util.Locale.setDefault(java.util.Locale.US);
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}