package org.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Manages parallel downloading of multiple files using a thread pool.
 * Each URL is downloaded by a separate DownloadTask on its own thread.
 */
public class MultiDownloadManager {

    private final int threadPoolSize;

    public MultiDownloadManager(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * Downloads all given URLs into outputDir concurrently.
     * Files are named file_1.bin, file_2.bin, ... (extension preserved if detectable).
     *
     * @param urls      array of URL strings to download
     * @param outputDir directory where files will be saved
     * @throws InterruptedException if the thread pool is interrupted
     */
    public void downloadAll(String[] urls, String outputDir) throws InterruptedException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("[Manager] Created output directory: " + outputDir);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<String>> futures = new ArrayList<>();

        long globalStart = System.currentTimeMillis();

        for (int i = 0; i < urls.length; i++) {
            String outputFile = buildOutputPath(outputDir, urls[i], i + 1);
            DownloadTask task = new DownloadTask(urls[i], outputFile);
            futures.add(executor.submit(task));
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.MINUTES);

        if (!finished) {
            System.err.println("[Manager] Timeout reached — some downloads may be incomplete.");
            executor.shutdownNow();
        }

        long globalElapsed = System.currentTimeMillis() - globalStart;

        // Summary
        System.out.println("\n========== Download Summary ==========");
        int ok = 0, fail = 0;
        for (Future<String> f : futures) {
            try {
                String result = f.get();
                if (result.contains("FAILED")) fail++; else ok++;
            } catch (ExecutionException e) {
                fail++;
            }
        }
        System.out.printf("Successful: %d  |  Failed: %d  |  Total time: %d ms%n",
                ok, fail, globalElapsed);
        System.out.println("======================================");
    }

    /**
     * Builds output file path, preserving file extension from URL when possible.
     */
    private String buildOutputPath(String dir, String url, int index) {
        String ext = "";
        int dot = url.lastIndexOf('.');
        int slash = url.lastIndexOf('/');
        if (dot > slash && dot < url.length() - 1) {
            String candidate = url.substring(dot); // e.g. ".jpg"
            // keep only alphanumeric extensions
            if (candidate.matches("\\.[a-zA-Z0-9]{1,5}")) {
                ext = candidate;
            }
        }
        if (ext.isEmpty()) ext = ".bin";
        return dir + File.separator + "file_" + index + ext;
    }
}
