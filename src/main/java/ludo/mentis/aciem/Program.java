package ludo.mentis.aciem;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

/**
 * Application entry point.
 * Responsible for initializing and launching the main application window.
 */
public class Program {

    public static void main(String[] args) {
        // Ensures that Locale is US before any rendering
        java.util.Locale.setDefault(java.util.Locale.US);

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        FlatLightLaf.setup();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Failed to initialize Look and Feel. The application may not display correctly.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}