package ludo.mentis.aciem;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {

    private JTextArea console;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JDateChooser datePicker;
    private JTextField txtDir;

    public MainFrame() {
        // --- FORÇAR LOCALE US PARA DATA E INTERFACE ---
        Locale.setDefault(Locale.US);

        setTitle("Batch Executive Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        // --- 1. CABEÇALHO (Estilo Win Antigo / Banner) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.GRAY));

        // Painel interno para agrupar ícone e texto com espaçamento
        JPanel leftHeaderGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)); // 15px de hgap e vgap
        leftHeaderGroup.setOpaque(false); // Mantém o fundo branco do pai

        // Carregamento do ícone (ajustado para ficar próximo ao texto)
        JLabel iconLabel;
        java.net.URL imgURL = getClass().getResource("/logo.png");
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage()
                    .getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            iconLabel = new JLabel(icon);
        } else {
            iconLabel = new JLabel("[ICO]");
        }

        // Configuração do Título
        JLabel titleLabel = new JLabel("Batch Processing Utility");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));

        // Adiciona os dois no grupo da esquerda
        leftHeaderGroup.add(iconLabel);
        leftHeaderGroup.add(titleLabel);

        // Adiciona o grupo ao lado oeste do headerPanel
        headerPanel.add(leftHeaderGroup, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createCenterPanel() {
        // --- 2. PAINEL CENTRAL ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Configurações no topo: Diretório e Data
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        configPanel.add(new JLabel("Output Dir:"));
        txtDir = new JTextField(25);
        configPanel.add(txtDir);

        // Botão de busca de diretório (Browse)
        JButton btnBrowse = new JButton("...");
        btnBrowse.setToolTipText("Select Output Directory");
        btnBrowse.setPreferredSize(new Dimension(30, 22));
        btnBrowse.addActionListener(e -> selectDirectory());
        configPanel.add(btnBrowse);

        configPanel.add(new JLabel("  Reference Date (MM/dd/yyyy):"));
        datePicker = new JDateChooser();
        datePicker.setLocale(Locale.US); // Força o calendário em Inglês
        datePicker.setDateFormatString("MM/dd/yyyy"); // Formato americano
        datePicker.setDate(new Date());
        datePicker.setPreferredSize(new Dimension(140, 22));
        configPanel.add(datePicker);

        centerPanel.add(configPanel, BorderLayout.NORTH);

        // Botões à Esquerda (10 unidades equidistantes)
        JPanel buttonPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 10));
        for (int i = 1; i <= 10; i++) {
            JButton btn = new JButton("Execute Job " + i);
            btn.addActionListener(e -> runExternalCommand());
            buttonPanel.add(btn);
        }
        centerPanel.add(buttonPanel, BorderLayout.WEST);

        // Console à Direita
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
        // --- 3. RODAPÉ ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        statusLabel = new JLabel("System Ready.");
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        footerPanel.add(statusLabel, BorderLayout.NORTH);
        footerPanel.add(progressBar, BorderLayout.SOUTH);

        return footerPanel;
    }

    // Método para abrir o seletor de pastas gráfico
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
        console.setText(""); // Limpa o console antes de começar
        progressBar.setIndeterminate(true);
        statusLabel.setText("Running process...");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Exemplo pegando o valor da data e do dir para o log
                    String selectedDate = ((JTextField) datePicker.getDateEditor().getUiComponent()).getText();
                    publish("Target Date: " + selectedDate);
                    publish("Target Dir: " + txtDir.getText());
                    publish("------------------------------------------");

                    ProcessBuilder pb = new ProcessBuilder("lsx", "-la",
                            txtDir.getText().isEmpty() ? "." : txtDir.getText());
                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            publish(line);
                        }
                    }
                    int exitCode = process.waitFor();
                    
                    if (exitCode != 0) {
                        publish("------------------------------------------");
                        publish("ERROR: Process exited with code " + exitCode);
                    }
                } catch (Exception e) {
                    publish("------------------------------------------");
                    publish("ERROR: An exception occurred during execution");
                    publish("Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    publish("------------------------------------------");
                    publish("Stack Trace:");
                    
                    // Print stack trace to console
                    for (StackTraceElement element : e.getStackTrace()) {
                        publish("  at " + element.toString());
                    }
                    
                    // Print cause chain if available
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        publish("Caused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                        for (StackTraceElement element : cause.getStackTrace()) {
                            publish("  at " + element.toString());
                        }
                        cause = cause.getCause();
                    }
                }
                return null;
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
        };
        worker.execute();
    }

    public static void main(String[] args) {
        // Garante que o Locale seja US antes de qualquer renderização
        Locale.setDefault(Locale.US);
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}