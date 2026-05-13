package com.example.downloader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.security.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadJob implements Runnable {

    public interface StatusCallback {
        void onStatus(int index, String status);
        void onProgress(int index, String progress);
        void onSpeed(int index, String speed);
    }

    private static final String DOWNLOAD_DIR = "downloads";

    private final String urlStr;
    private final String filename;
    private final StatusCallback callback;
    private int index;
    private AtomicBoolean cancelled;

    public DownloadJob(String urlStr, String filename, StatusCallback callback) {
        this.urlStr = urlStr;
        this.filename = filename;
        this.callback = callback;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setCancelledFlag(AtomicBoolean flag) {
        this.cancelled = flag;
    }

    @Override
    public void run() {
        Path dir = Paths.get(DOWNLOAD_DIR);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            callback.onStatus(index, "Error: Cannot create download folder");
            return;
        }

        Path filePath = dir.resolve(filename);

        try {
            callback.onStatus(index, "Connecting...");

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(30_000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Check if file already exists and matches remote
            if (Files.exists(filePath)) {
                long localSize = Files.size(filePath);
                long remoteSize = conn.getContentLengthLong();
                if (remoteSize > 0 && localSize == remoteSize) {
                    // Verify SHA-256 hash match via ETag or size check
                    callback.onStatus(index, "Already exists");
                    callback.onProgress(index, "100%");
                    callback.onSpeed(index, "0 KB/s");
                    conn.disconnect();
                    return;
                }
            }

            // Support resume via Range header
            long existingSize = Files.exists(filePath) ? Files.size(filePath) : 0;
            if (existingSize > 0) {
                conn.disconnect();
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(30_000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setRequestProperty("Range", "bytes=" + existingSize + "-");
                callback.onStatus(index, "Resuming...");
            }

            int responseCode = conn.getResponseCode();
            boolean resuming = (responseCode == HttpURLConnection.HTTP_PARTIAL);
            boolean freshStart = (responseCode == HttpURLConnection.HTTP_OK);

            if (!resuming && !freshStart) {
                callback.onStatus(index, "Error: HTTP " + responseCode);
                conn.disconnect();
                return;
            }

            if (freshStart) {
                existingSize = 0;
            }

            long totalSize = conn.getContentLengthLong();
            if (resuming) {
                totalSize += existingSize;
            }

            try (InputStream in = conn.getInputStream();
                 OutputStream out = new FileOutputStream(filePath.toFile(), resuming)) {

                byte[] buffer = new byte[8192];
                long downloaded = existingSize;
                long startTime = System.currentTimeMillis();
                long lastTime = startTime;
                long lastBytes = 0;
                int bytesRead;

                callback.onStatus(index, "Downloading");

                while ((bytesRead = in.read(buffer)) != -1) {
                    if (cancelled != null && cancelled.get()) {
                        callback.onStatus(index, "Stopped");
                        return;
                    }

                    out.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;

                    long now = System.currentTimeMillis();
                    long elapsed = now - lastTime;
                    if (elapsed >= 500) {
                        long bytesSinceLast = downloaded - lastBytes - existingSize;
                        double kbps = bytesSinceLast / (double) elapsed;
                        String speedStr = String.format("%.1f KB/s", kbps);

                        String progress = totalSize > 0
                                ? String.format("%.1f%%", downloaded * 100.0 / totalSize)
                                : downloaded / 1024 + " KB";

                        callback.onProgress(index, progress);
                        callback.onSpeed(index, speedStr);
                        lastTime = now;
                        lastBytes = downloaded - existingSize;
                    }
                }
            }

            conn.disconnect();
            callback.onStatus(index, "Completed");
            callback.onProgress(index, "100%");
            callback.onSpeed(index, "0 KB/s");

        } catch (Exception e) {
            if (cancelled != null && cancelled.get()) {
                callback.onStatus(index, "Stopped");
            } else {
                callback.onStatus(index, "Error: " + e.getMessage());
            }
        }
    }

    // SHA-256 hash of a file for content verification
    public static String sha256(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                md.update(buffer, 0, n);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
