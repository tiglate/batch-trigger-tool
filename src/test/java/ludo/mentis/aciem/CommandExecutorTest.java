package ludo.mentis.aciem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.toedter.calendar.JDateChooser;

import static org.junit.jupiter.api.Assertions.*;

class CommandExecutorTest {

    private JTextArea console;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private CommandExecutor executor;

    @BeforeEach
    void setUp() {
        var datePicker = new JDateChooser();
        ((JTextField) datePicker.getDateEditor().getUiComponent()).setText("2026-04-25");
        var txtDir = new JTextField("/tmp/work");
        console = new JTextArea("old output");
        progressBar = new JProgressBar();
        statusLabel = new JLabel();
        executor = new CommandExecutor(datePicker, txtDir, console, progressBar, statusLabel);
    }

    @Test
    void executeShouldUpdateInitialUiState() {
        executor.execute("sh", "-c", "echo hello");

        assertEquals("", console.getText());
        assertTrue(progressBar.isIndeterminate());
        assertEquals("Running process...", statusLabel.getText());
    }

    @Test
    void publishExecutionInfoShouldPublishDateAndDirectory() {
        TestCommandWorker worker = new TestCommandWorker(new String[] { "sh", "-c", "echo hello" });

        worker.publishExecutionInfo();

        assertEquals(List.of(
            "Target Date: 2026-04-25",
            "Target Dir: /tmp/work",
            "------------------------------------------"
        ), worker.publishedLines);
    }

    @Test
    void executeCommandShouldPublishErrorWhenExitCodeIsNotZero() throws Exception {
        TestCommandWorker worker = new TestCommandWorker(new String[] { "sh", "-c", "echo hi; exit 7" });

        worker.executeCommand();

        assertTrue(worker.publishedLines.contains("hi"));
        assertTrue(worker.publishedLines.contains("ERROR: Process exited with code 7"));
    }

    @Test
    void handleExceptionShouldPublishExceptionAndCauseDetails() {
        TestCommandWorker worker = new TestCommandWorker(new String[] { "sh", "-c", "echo hello" });
        IllegalArgumentException cause = new IllegalArgumentException("inner cause");
        IOException exception = new IOException("outer failure", cause);

        worker.handleException(exception);

        assertTrue(worker.publishedLines.contains("ERROR: An exception occurred during execution"));
        assertTrue(worker.publishedLines.contains("Exception: IOException: outer failure"));
        assertTrue(worker.publishedLines.contains("Caused by: IllegalArgumentException: inner cause"));
        assertTrue(worker.publishedLines.stream().anyMatch(line -> line.startsWith("  at ")));
    }

    @Test
    void doneShouldUpdateFinalUiState() {
        CommandExecutor.CommandWorker worker = executor.new CommandWorker(new String[] { "sh", "-c", "echo hello" });

        worker.done();

        assertFalse(progressBar.isIndeterminate());
        assertEquals(100, progressBar.getValue());
        assertEquals("Execution Finished.", statusLabel.getText());
    }

    private class TestCommandWorker extends CommandExecutor.CommandWorker {
        private final List<String> publishedLines = new ArrayList<>();

        TestCommandWorker(String[] command) {
            executor.super(command);
        }

        @Override
        protected void publishLine(String line) {
            publishedLines.add(line);
        }
    }
}