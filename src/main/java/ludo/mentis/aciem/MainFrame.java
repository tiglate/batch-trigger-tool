package ludo.mentis.aciem;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;
import java.util.Locale;

public class MainFrame extends JFrame {

    private JTextArea console;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JDateChooser datePicker;
    private JTextField txtDir;
    private CommandExecutor commandExecutor;

    public MainFrame() {
        // --- FORCE US LOCALE FOR DATE AND INTERFACE ---
        Locale.setDefault(Locale.US);

        setTitle("Batch Processing Utility");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
        
        // Initialize the command executor after UI components are created
        commandExecutor = new CommandExecutor(datePicker, txtDir, console, progressBar, statusLabel);
    }

    private JPanel createHeaderPanel() {
        // --- 1. HEADER (Old Windows Style / Banner) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.GRAY));

        // Internal panel to group icon and text with spacing
        JPanel leftHeaderGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)); // 15px of hgap and vgap
        leftHeaderGroup.setOpaque(false); // Keeps the white background of the parent

        // Icon loading (adjusted to be close to the text)
        JLabel iconLabel;
        java.net.URL imgURL = getClass().getResource("/logo.png");
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage()
                    .getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            iconLabel = new JLabel(icon);
        } else {
            iconLabel = new JLabel("[ICO]");
        }

        // Title Configuration
        JLabel titleLabel = new JLabel("Batch Processing Utility");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));

        // Adds both to the left group
        leftHeaderGroup.add(iconLabel);
        leftHeaderGroup.add(titleLabel);

        // Adds the group to the west side of headerPanel
        headerPanel.add(leftHeaderGroup, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createCenterPanel() {
        // --- 2. CENTER PANEL ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top configurations: Directory and Date
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        configPanel.add(new JLabel("Output Dir:"));
        txtDir = new JTextField(25);
        configPanel.add(txtDir);

        // Directory browse button
        JButton btnBrowse = new JButton("...");
        btnBrowse.setToolTipText("Select Output Directory");
        btnBrowse.setPreferredSize(new Dimension(30, 22));
        btnBrowse.addActionListener(e -> selectDirectory());
        configPanel.add(btnBrowse);

        configPanel.add(new JLabel("  Reference Date (MM/dd/yyyy):"));
        datePicker = new JDateChooser();
        datePicker.setLocale(Locale.US); // Forces the calendar in English
        datePicker.setDateFormatString("MM/dd/yyyy"); // American format
        datePicker.setDate(new Date());
        datePicker.setPreferredSize(new Dimension(140, 22));
        configPanel.add(datePicker);

        centerPanel.add(configPanel, BorderLayout.NORTH);

        // Left buttons (10 equally spaced units)
        JPanel buttonPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 10));
        for (int i = 1; i <= 10; i++) {
            JButton btn = new JButton("Execute Job " + i);
            btn.addActionListener(e -> runExternalCommand());
            buttonPanel.add(btn);
        }
        centerPanel.add(buttonPanel, BorderLayout.WEST);

        // Right console
        console = new JTextArea();
        console.setBackground(Color.BLACK);
        console.setForeground(Color.WHITE);
        console.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(console);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createFooterPanel() {
        // --- 3. FOOTER ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        statusLabel = new JLabel("System Ready.");
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        footerPanel.add(statusLabel, BorderLayout.NORTH);
        footerPanel.add(progressBar, BorderLayout.SOUTH);

        return footerPanel;
    }

    // Method to open the graphical folder selector
    private void selectDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Output Folder");

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            txtDir.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void runExternalCommand() {
        String targetDir = txtDir.getText().isEmpty() ? "." : txtDir.getText();
        commandExecutor.execute("ls", "-la", targetDir);
    }
}