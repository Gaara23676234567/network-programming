package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Demonstrates the use of HttpURLConnection to send a GET request
 * and read the server response.
 *
 * Usage:
 *   java -cp target/classes com.example.HttpURLConnectionExample
 *
 * The program connects to a public REST API, sends a GET request,
 * reads the HTTP status code and response body, then prints them.
 */
public class HttpURLConnectionExample {

    private static final String TARGET_URL =
            "https://jsonplaceholder.typicode.com/posts";

    public static void main(String[] args) {
        System.out.println("Connecting to: " + TARGET_URL);

        try {
            // 1. Create URL and open HttpURLConnection
            URL url = new URL(TARGET_URL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            // 2. Set request method and timeouts
            httpConn.setRequestMethod("GET");
            httpConn.setConnectTimeout(5000);
            httpConn.setReadTimeout(5000);

            // 3. Set request headers
            httpConn.setRequestProperty("Accept", "application/json");
            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // 4. Read HTTP status code
            int responseCode = httpConn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            System.out.println("Content-Type : " + httpConn.getContentType());
            System.out.println("Content-Length: " + httpConn.getContentLength());
            System.out.println();

            // 5. Read response body if status is 200 OK
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpConn.getInputStream()));

                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n");
                }
                in.close();

                // Print first 500 characters to keep output readable
                String body = response.toString();
                System.out.println("Response (first 500 chars):");
                System.out.println(body.substring(0, Math.min(500, body.length())));
                System.out.println("...");
                System.out.println("\nTotal response length: " + body.length() + " chars");

            } else {
                System.out.println("Request failed with response code: " + responseCode);
            }

            httpConn.disconnect();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
