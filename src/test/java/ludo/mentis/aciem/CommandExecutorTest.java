package ludo.mentis.aciem;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.toedter.calendar.JDateChooser;

/**
 * Integration tests for CommandExecutor class.
 * Tests both happy paths and various exception scenarios.
 * Uses real Swing components to test actual behavior.
 */
class CommandExecutorTest {

    private JDateChooser datePicker;
    private JTextField txtDir;
    private JTextArea console;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private CommandExecutor commandExecutor;

    @BeforeEach
    void setUp() {
        // Create real Swing components on EDT
        SwingUtilities.invokeLater(() -> {
            datePicker = new JDateChooser();
            Calendar cal = Calendar.getInstance();
            cal.set(2026, Calendar.APRIL, 24);
            datePicker.setDate(cal.getTime());

            txtDir = new JTextField("/tmp/test");
            console = new JTextArea();
            progressBar = new JProgressBar();
            statusLabel = new JLabel();

            commandExecutor = new CommandExecutor(datePicker, txtDir, console, progressBar, statusLabel);
        });

        // Wait for EDT to complete
        try {
            SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testSuccessfulCommandExecution() throws Exception {
        // Act
        SwingUtilities.invokeAndWait(() -> {
            commandExecutor.execute("echo", "test");
        });

        // Wait for SwingWorker to complete
        waitForSwingWorker();

        // Assert
        SwingUtilities.invokeAndWait(() -> {
            String consoleText = console.getText();
            assertTrue(consoleText.contains("test"), "Console should contain command output");
            assertTrue(consoleText.contains("Target Date:"), "Console should contain target date");
            assertTrue(consoleText.contains("Target Dir:"), "Console should contain target directory");
            assertEquals("Execution Finished.", statusLabel.getText(), "Status should be finished");
            assertEquals(100, progressBar.getValue(), "Progress bar should be at 100%");
        });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testCommandWithNonZeroExitCode() throws Exception {
        // Act
        SwingUtilities.invokeAndWait(() -> {
            commandExecutor.execute("false"); // Command that exits with code 1
        });

        // Wait for SwingWorker to complete
        waitForSwingWorker();

        // Assert
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(console.getText().contains("ERROR: Process exited with code"),
                      "Console should contain error message for non-zero exit code");
        });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testCommandNotFound() throws Exception {
        // Act
        SwingUtilities.invokeAndWait(() -> {
            commandExecutor.execute("nonexistent-command-12345");
        });

        // Wait for SwingWorker to complete
        waitForSwingWorker();

        // Assert
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(console.getText().contains("ERROR: An exception occurred during execution"),
                      "Console should contain exception message");
            assertTrue(console.getText().contains("IOException"),
                      "Console should contain IOException details");
        });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testEmptyCommand() throws Exception {
        // Act
        SwingUtilities.invokeAndWait(() -> {
            commandExecutor.execute(); // Empty command
        });

        // Wait for SwingWorker to complete
        waitForSwingWorker();

        // Assert
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(console.getText().contains("ERROR: An exception occurred during execution"),
                      "Console should contain exception message for empty command");
        });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testNullCommandArguments() throws Exception {
        // Act
        SwingUtilities.invokeAndWait(() -> {
            commandExecutor.execute((String[]) null);
        });

        // Wait for SwingWorker to complete
        waitForSwingWorker();

        // Assert - Should handle null gracefully or throw appropriate exception
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(console.getText().contains("ERROR: An exception occurred during execution"),
                      "Console should contain exception message for null command");
        });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMultipleCommandArguments() throws Exception {
        // Act
        SwingUtilities.invokeAndWait(() -> {
            commandExecutor.execute("echo", "arg1", "arg2", "arg3");
        });

        // Wait for SwingWorker to complete
        waitForSwingWorker();

        // Assert
        SwingUtilities.invokeAndWait(() -> {
            String consoleText = console.getText();
            assertTrue(consoleText.contains("arg1"), "Console should contain first argument");
            assertTrue(consoleText.contains("arg2"), "Console should contain second argument");
            assertTrue(consoleText.contains("arg3"), "Console should contain third argument");
        });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testCommandWithOutput() throws Exception {
        // Act
        SwingUtilities.invokeAndWait(() -> {
            commandExecutor.execute("printf", "line1\\nline2\\nline3");
        });

        // Wait for SwingWorker to complete
        waitForSwingWorker();

        // Assert
        SwingUtilities.invokeAndWait(() -> {
            String consoleText = console.getText();
            assertTrue(consoleText.contains("line1"), "Console should contain first line");
            assertTrue(consoleText.contains("line2"), "Console should contain second line");
            assertTrue(consoleText.contains("line3"), "Console should contain third line");
        });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testProgressBarAndStatusUpdates() throws Exception {
        // Act
        SwingUtilities.invokeAndWait(() -> {
            commandExecutor.execute("true"); // Simple command that succeeds
        });

        // Check initial state
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(progressBar.isIndeterminate(), "Progress bar should be indeterminate initially");
            assertEquals("Running process...", statusLabel.getText(), "Status should show running");
        });

        // Wait for completion
        waitForSwingWorker();

        // Assert final state
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(progressBar.isIndeterminate(), "Progress bar should not be indeterminate when finished");
            assertEquals(100, progressBar.getValue(), "Progress bar should be at 100%");
            assertEquals("Execution Finished.", statusLabel.getText(), "Status should be finished");
        });
    }

    /**
     * Helper method to wait for SwingWorker completion.
     * Since SwingWorker runs asynchronously, we need to wait for it to finish.
     */
    private void waitForSwingWorker() throws Exception {
        // Give some time for the SwingWorker to complete
        Thread.sleep(2000);

        // Force any pending EDT events to be processed
        SwingUtilities.invokeAndWait(() -> {});
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
}