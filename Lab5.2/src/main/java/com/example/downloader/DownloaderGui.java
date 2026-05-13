package com.example.downloader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DownloaderGui extends JFrame implements DownloadJob.StatusCallback {

    private final JTextArea urlText = new JTextArea(6, 60);
    private final JButton startButton = new JButton("▶  Start Download");
    private final JButton stopButton  = new JButton("■  Stop");
    private final JButton clearButton = new JButton("✕  Clear");
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final DownloadEngine engine;

    public DownloaderGui() {
        super("Individual #5.2 — Swing Multi-Thread File Downloader");

        String[] columns = {"#", "URL", "File", "Status", "Progress", "Speed"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        engine = new DownloadEngine(4);

        buildUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 500));
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── URL input panel ──────────────────────────────────────────────────
        urlText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        urlText.setLineWrap(true);
        urlText.setWrapStyleWord(true);
        JScrollPane urlScroll = new JScrollPane(urlText);
        urlScroll.setBorder(BorderFactory.createTitledBorder("Enter URLs (one per line)"));

        // ── Button bar ───────────────────────────────────────────────────────
        startButton.setBackground(new Color(60, 130, 60));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);

        stopButton.setBackground(new Color(180, 50, 50));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false);

        clearButton.setFocusPainted(false);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnPanel.add(startButton);
        btnPanel.add(stopButton);
        btnPanel.add(clearButton);

        JPanel topPanel = new JPanel(new BorderLayout(4, 4));
        topPanel.add(urlScroll, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────────────
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Status bar ───────────────────────────────────────────────────────
        JLabel statusBar = new JLabel(" Ready");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);

        // ── Listeners ────────────────────────────────────────────────────────
        startButton.addActionListener(e -> startDownloads(statusBar));
        stopButton.addActionListener(e -> stopDownloads(statusBar));
        clearButton.addActionListener(e -> {
            urlText.setText("");
            tableModel.setRowCount(0);
            statusBar.setText(" Ready");
        });
    }

    private void styleTable() {
        table.setRowHeight(22);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(184, 207, 229));

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(30);   // #
        cm.getColumn(1).setPreferredWidth(300);  // URL
        cm.getColumn(2).setPreferredWidth(150);  // File
        cm.getColumn(3).setPreferredWidth(100);  // Status
        cm.getColumn(4).setPreferredWidth(80);   // Progress
        cm.getColumn(5).setPreferredWidth(80);   // Speed
    }

    private void startDownloads(JLabel statusBar) {
        if (engine.isRunning()) {
            JOptionPane.showMessageDialog(this,
                    "Downloads are already in progress. Please wait or press Stop.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<DownloadJob> jobs = new ArrayList<>();
        int rowStart = tableModel.getRowCount();
        int jobNum = rowStart + 1;

        for (String line : urlText.getText().split("\\r?\\n")) {
            String url = line.trim();
            if (url.isEmpty()) continue;
            String filename = safeFilename(url);
            tableModel.addRow(new Object[]{jobNum++, url, filename, "Queued", "0%", "—"});
            jobs.add(new DownloadJob(url, filename, this));
        }

        if (jobs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No URLs entered.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        engine.loadTasks(jobs, rowStart);
        engine.start();

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusBar.setText(" Downloading " + jobs.size() + " file(s)…");

        // Re-enable Start button once all tasks finish
        new Thread(() -> {
            while (engine.isRunning()) {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                statusBar.setText(" All tasks finished.");
            });
        }).start();
    }

    private void stopDownloads(JLabel statusBar) {
        engine.stop();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusBar.setText(" Stopped by user.");
    }

    // ── StatusCallback ────────────────────────────────────────────────────────

    @Override
    public void onStatus(int index, String status) {
        SwingUtilities.invokeLater(() -> tableModel.setValueAt(status, index, 3));
    }

    @Override
    public void onProgress(int index, String progress) {
        SwingUtilities.invokeLater(() -> tableModel.setValueAt(progress, index, 4));
    }

    @Override
    public void onSpeed(int index, String speed) {
        SwingUtilities.invokeLater(() -> tableModel.setValueAt(speed, index, 5));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String safeFilename(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            String path = u.getPath();
            if (path == null || path.isEmpty() || path.endsWith("/")) return "index.html";
            String file = path.substring(path.lastIndexOf('/') + 1);
            if (file.isEmpty()) return "index.html";
            return file.replaceAll("[^A-Za-z0-9._-]", "_");
        } catch (Exception ex) {
            return "downloaded_file";
        }
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new DownloaderGui().setVisible(true));
    }
}
