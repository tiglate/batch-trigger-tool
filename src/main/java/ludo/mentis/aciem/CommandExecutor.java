package ludo.mentis.aciem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.toedter.calendar.JDateChooser;

/**
 * Handles execution of external commands in a separate thread.
 * Manages process execution, output capture, and error handling.
 * Designed to be used with Swing UI components.
 * 
 * Allows different commands to be executed using the same executor instance.
 */
public class CommandExecutor {
    private final JDateChooser datePicker;
    private final JTextField txtDir;
    private final JTextArea console;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    /**
     * Constructs a CommandExecutor with the required UI components.
     *
     * @param datePicker the date picker component for execution metadata
     * @param txtDir the text field containing the target directory
     * @param console the console text area for output display
     * @param progressBar the progress bar for visual feedback
     * @param statusLabel the status label for status updates
     */
    public CommandExecutor(JDateChooser datePicker, JTextField txtDir, 
                          JTextArea console, JProgressBar progressBar, JLabel statusLabel) {
        this.datePicker = datePicker;
        this.txtDir = txtDir;
        this.console = console;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
    }

    /**
     * Executes a command with the given arguments.
     *
     * @param command the command and its arguments to execute (e.g., "ls", "-la", "/path")
     */
    public void execute(String... command) {
        console.setText(""); // Clears the console before starting
        progressBar.setIndeterminate(true);
        statusLabel.setText("Running process...");

        CommandWorker worker = new CommandWorker(command);
        worker.execute();
    }

    /**
     * Internal SwingWorker implementation for executing commands.
     */
    private class CommandWorker extends SwingWorker<Void, String> {
        private final String[] command;

        public CommandWorker(String[] command) {
            this.command = command;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                publishExecutionInfo();
                executeCommand();
            } catch (Exception e) {
                handleException(e);
            }
            return null;
        }

        /**
         * Publishes the execution metadata (date and target directory).
         */
        private void publishExecutionInfo() {
            String selectedDate = ((JTextField) datePicker.getDateEditor().getUiComponent()).getText();
            publish("Target Date: " + selectedDate);
            publish("Target Dir: " + txtDir.getText());
            publish("------------------------------------------");
        }

        /**
         * Creates and executes the external process command.
         */
        private void executeCommand() throws Exception {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            captureCommandOutput(process);
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                publish("------------------------------------------");
                publish("ERROR: Process exited with code " + exitCode);
            }
        }

        /**
         * Reads and publishes command output line by line.
         */
        private void captureCommandOutput(Process process) throws Exception {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    publish(line);
                }
            }
        }

        /**
         * Handles and formats exception details for console output.
         */
        private void handleException(Exception e) {
            publish("------------------------------------------");
            publish("ERROR: An exception occurred during execution");
            publish("Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            publish("------------------------------------------");
            publish("Stack Trace:");
            
            publishStackTrace(e.getStackTrace());
            
            // Print cause chain if available
            Throwable cause = e.getCause();
            while (cause != null) {
                publish("Caused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                publishStackTrace(cause.getStackTrace());
                cause = cause.getCause();
            }
        }

        /**
         * Publishes stack trace elements to console.
         */
        private void publishStackTrace(StackTraceElement[] stackTrace) {
            for (StackTraceElement element : stackTrace) {
                publish("  at " + element.toString());
            }
        }

        @Override
        protected void process(List<String> chunks) {
            for (String line : chunks) {
                console.append(line + "\n");
            }
        }

        @Override
        protected void done() {
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            statusLabel.setText("Execution Finished.");
        }
    }
}
