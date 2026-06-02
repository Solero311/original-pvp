package de.originalpvp.installer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Swing-based installer GUI for the Original PvP mod.
 *
 * <p>Launched when the user runs {@code java -jar OriginalPvP-1.1.4.jar}.
 * It delegates the actual heavy lifting to {@link InstallerLogic}.</p>
 */
public class GuiInstaller extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String TITLE = "Original PvP 1.1.4 Installer";
    private static final int WIDTH = 500;
    private static final int HEIGHT = 300;

    private JTextField pathField;
    private JButton installButton;
    private JButton cancelButton;
    private JButton browseButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    // ── public entry point ──────────────────────────────────────────────

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new GuiInstaller().setVisible(true);
        });
    }

    // ── constructor ─────────────────────────────────────────────────────

    public GuiInstaller() {
        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    // ── UI setup ────────────────────────────────────────────────────────

    private void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);

        // ── Title ───────────────────────────────────────────────────────
        JLabel title = new JLabel("Install Original PvP for Minecraft 1.8.9");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(title, gbc);

        // ── Subtitle ────────────────────────────────────────────────────
        JLabel subtitle = new JLabel("Zoom \u2022 Toggle Sprint \u2022 Fog Control \u2022 Sky Toggle \u2022 FOV Effects");
        subtitle.setForeground(Color.GRAY);
        gbc.gridy = 1;
        root.add(subtitle, gbc);

        // ── Path label ──────────────────────────────────────────────────
        JLabel pathLabel = new JLabel("Minecraft directory:");
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        root.add(pathLabel, gbc);

        // ── Path field ──────────────────────────────────────────────────
        pathField = new JTextField(getDefaultMinecraftDir().getAbsolutePath());
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        root.add(pathField, gbc);

        // ── Browse button ───────────────────────────────────────────────
        browseButton = new JButton("Browse...");
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        root.add(browseButton, gbc);

        // ── Progress bar ────────────────────────────────────────────────
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setPreferredSize(new Dimension(0, 22));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        root.add(progressBar, gbc);

        // ── Status label ────────────────────────────────────────────────
        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        gbc.gridy = 5;
        root.add(statusLabel, gbc);

        // ── Button row ──────────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        installButton = new JButton("Install");
        cancelButton = new JButton("Cancel");
        installButton.setPreferredSize(new Dimension(110, 30));
        cancelButton.setPreferredSize(new Dimension(110, 30));
        buttonPanel.add(installButton);
        buttonPanel.add(cancelButton);
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(buttonPanel, gbc);

        setContentPane(root);

        // ── Listeners ───────────────────────────────────────────────────
        browseButton.addActionListener(e -> onBrowse());
        installButton.addActionListener(e -> onInstall());
        cancelButton.addActionListener(e -> System.exit(0));
    }

    // ── event handlers ──────────────────────────────────────────────────

    private void onBrowse() {
        JFileChooser chooser = new JFileChooser(pathField.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select .minecraft directory");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onInstall() {
        File mcDir = new File(pathField.getText().trim());
        if (!mcDir.exists() || !mcDir.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    "The selected directory does not exist:\n" + mcDir.getAbsolutePath(),
                    "Invalid Directory", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check that 1.8.9 exists
        File vanillaJar = new File(mcDir, "versions/1.8.9/1.8.9.jar");
        if (!vanillaJar.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Minecraft 1.8.9 was not found!\n\n" +
                    "Please launch Minecraft 1.8.9 at least once using the vanilla launcher,\n" +
                    "then try again.\n\n" +
                    "Expected file:\n" + vanillaJar.getAbsolutePath(),
                    "Minecraft 1.8.9 Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setInputEnabled(false);

        new Thread(() -> {
            try {
                InstallerLogic.install(mcDir, (progress, status) ->
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(progress);
                            progressBar.setString(progress + "%");
                            statusLabel.setText(status);
                        })
                );

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Original PvP has been installed successfully!\n\n" +
                                "Select the \"1.8.9-OriginalPvP\" profile in your launcher\n" +
                                "and click Play.",
                                "Installation Successful",
                                JOptionPane.INFORMATION_MESSAGE)
                );
                System.exit(0);

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Installation failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    setInputEnabled(true);
                    progressBar.setValue(0);
                    progressBar.setString("");
                    statusLabel.setText("Installation failed.");
                });
            }
        }, "OriginalPvP-Installer").start();
    }

    private void setInputEnabled(boolean enabled) {
        installButton.setEnabled(enabled);
        browseButton.setEnabled(enabled);
        pathField.setEnabled(enabled);
    }

    // ── .minecraft detection ────────────────────────────────────────────

    private static File getDefaultMinecraftDir() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            if (appdata != null) {
                return new File(appdata, ".minecraft");
            }
        } else if (os.contains("mac") || os.contains("darwin")) {
            return new File(System.getProperty("user.home"),
                    "Library/Application Support/minecraft");
        }
        // Linux / fallback
        return new File(System.getProperty("user.home"), ".minecraft");
    }
}
