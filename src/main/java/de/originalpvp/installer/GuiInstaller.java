package de.originalpvp.installer;

import de.originalpvp.util.ModConstants;
import de.originalpvp.util.ModLogger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

/**
 * Swing-based installer GUI for the Original PvP mod.
 *
 * <p>Launched when the user runs {@code java -jar OriginalPvP-2.0.jar}.
 * Delegates installation steps to {@link InstallerLogic}.</p>
 */
public class GuiInstaller extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String TITLE = ModConstants.FULL_DISPLAY_NAME + " Installer";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 300;

    private JTextField pathField;
    private JButton installButton;
    private JButton cancelButton;
    private JButton browseButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new GuiInstaller().setVisible(true);
        });
    }

    public GuiInstaller() {
        setTitle(TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);

        JLabel title = new JLabel("Install Original PvP for Minecraft 1.8.9");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(title, gbc);

        JLabel subtitle = new JLabel("Zoom \u2022 Toggle Sprint \u2022 Fog Control \u2022 Sky Toggle \u2022 FOV Effects");
        subtitle.setForeground(Color.GRAY);
        gbc.gridy = 1;
        root.add(subtitle, gbc);

        JLabel pathLabel = new JLabel("Minecraft directory:");
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        root.add(pathLabel, gbc);

        pathField = new JTextField(getDefaultMinecraftDir().getAbsolutePath());
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        root.add(pathField, gbc);

        browseButton = new JButton("Browse...");
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        root.add(browseButton, gbc);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setPreferredSize(new Dimension(0, 22));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        root.add(progressBar, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        gbc.gridy = 5;
        root.add(statusLabel, gbc);

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

        browseButton.addActionListener(e -> onBrowse());
        installButton.addActionListener(e -> onInstall());
        cancelButton.addActionListener(e -> System.exit(0));
    }

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
                                "Select the \"" + ModConstants.INSTALLER_VERSION_ID + "\" profile in your launcher\n" +
                                "and click Play.",
                                "Installation Successful",
                                JOptionPane.INFORMATION_MESSAGE)
                );
                System.exit(0);

            } catch (Exception ex) {
                ModLogger.error("Installation failed", ex);
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
        return new File(System.getProperty("user.home"), ".minecraft");
    }
}
