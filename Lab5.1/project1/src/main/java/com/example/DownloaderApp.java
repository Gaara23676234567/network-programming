package com.example;

import java.io.IOException;

/**
 * Entry point for the single-thread console download manager.
 *
 * Usage:
 *   java -cp target/classes com.example.DownloaderApp <url> <outputFile>
 *
 * Example:
 *   java -cp target/classes com.example.DownloaderApp \
 *       https://picsum.photos/600/400 output.jpg
 */
public class DownloaderApp {

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }

        String url        = args[0];
        String outputFile = args[1];

        try {
            FileDownloader.download(url, outputFile);
            System.out.println("[Single] Download complete.");
        } catch (IOException e) {
            System.err.println("[Single] Error: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp target/classes com.example.DownloaderApp <url> <outputFile>");
        System.out.println("  url        – direct link to the file");
        System.out.println("  outputFile – local path to save the file");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -cp target/classes com.example.DownloaderApp https://picsum.photos/600/400 photo.jpg");
    }
}
