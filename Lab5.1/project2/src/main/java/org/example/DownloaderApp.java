package org.example;

/**
 * Entry point for the multi-thread console download manager.
 *
 * Usage:
 *   java -cp target/classes org.example.DownloaderApp <outputDir> <url1> [url2] [url3] ...
 *
 * Example:
 *   java -cp target/classes org.example.DownloaderApp ./downloads \
 *       https://picsum.photos/200/300 \
 *       https://picsum.photos/400/300 \
 *       https://picsum.photos/600/400
 */
public class DownloaderApp {

    /** Number of worker threads in the pool. */
    private static final int THREAD_POOL_SIZE = 4;

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        String outputDir = args[0];
        String[] urls    = new String[args.length - 1];
        System.arraycopy(args, 1, urls, 0, urls.length);

        System.out.printf("[App] Launching multi-thread downloader (pool size: %d)%n", THREAD_POOL_SIZE);
        System.out.printf("[App] Output directory : %s%n", outputDir);
        System.out.printf("[App] URLs to download : %d%n%n", urls.length);

        MultiDownloadManager manager = new MultiDownloadManager(THREAD_POOL_SIZE);

        try {
            manager.downloadAll(urls, outputDir);
            System.out.println("[App] All tasks finished.");
        } catch (InterruptedException e) {
            System.err.println("[App] Interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            System.exit(2);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp target/classes org.example.DownloaderApp <outputDir> <url1> [url2] ...");
        System.out.println("  outputDir – directory to save downloaded files");
        System.out.println("  url1 ...  – one or more direct download URLs");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -cp target/classes org.example.DownloaderApp ./downloads \\");
        System.out.println("      https://picsum.photos/200/300 https://picsum.photos/400/300");
    }
}
