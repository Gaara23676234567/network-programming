package org.example;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

/**
 * A Callable task that downloads one file from a URL.
 * Returns a result message (success or failure).
 */
public class DownloadTask implements Callable<String> {

    private static final int    BUFFER_SIZE  = 8192;
    private static final String USER_AGENT   = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";

    private final String urlString;
    private final String outputPath;

    public DownloadTask(String urlString, String outputPath) {
        this.urlString  = urlString;
        this.outputPath = outputPath;
    }

    @Override
    public String call() {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[%s] Starting: %s%n", threadName, urlString);
        long startTime = System.currentTimeMillis();

        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            connection.setRequestProperty("User-Agent", USER_AGENT);

            try (InputStream in  = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(outputPath)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalRead = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }

                long elapsed = System.currentTimeMillis() - startTime;
                String msg = String.format("[%s] Done: %s -> %s (%d ms, %d bytes)",
                        threadName, urlString, outputPath, elapsed, totalRead);
                System.out.println(msg);
                return msg;
            }

        } catch (IOException e) {
            String err = String.format("[%s] FAILED: %s — %s", threadName, urlString, e.getMessage());
            System.err.println(err);
            return err;
        }
    }
}
