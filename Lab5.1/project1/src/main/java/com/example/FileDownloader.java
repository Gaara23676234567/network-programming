package com.example;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utility class for downloading a single file from a URL.
 * Uses java.net.URL and URLConnection objects.
 */
public class FileDownloader {

    private static final int BUFFER_SIZE = 8192;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";

    /**
     * Downloads a file from the given URL and saves it to outputPath.
     * Prints download progress and elapsed time upon completion.
     *
     * @param urlString  source URL string
     * @param outputPath destination file path
     * @throws IOException if connection or IO fails
     */
    public static void download(String urlString, String outputPath) throws IOException {
        System.out.println("[Single] Starting download: " + urlString);
        long startTime = System.currentTimeMillis();

        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(30_000);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        long fileSize = connection.getContentLengthLong();

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(outputPath)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                if (fileSize > 0) {
                    int percent = (int) (totalRead * 100 / fileSize);
                    System.out.printf("\r[Single] Progress: %d%%  (%d / %d bytes)", percent, totalRead, fileSize);
                }
            }
            System.out.println(); // newline after progress
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("[Single] Saved to: %s  (time: %d ms)%n", outputPath, elapsed);
    }
}
