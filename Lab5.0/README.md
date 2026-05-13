# Individual Work 5.0 — Working with URL Resources

## Description
A Maven project demonstrating the use of `HttpURLConnection` to send HTTP GET requests and read server responses.

## Task
Connect to a public REST API using `HttpURLConnection`, read the HTTP status code, response headers, and response body.

## How it works
1. Creates a connection to `https://jsonplaceholder.typicode.com/posts`
2. Sets request method `GET` with connection and read timeouts
3. Reads HTTP status code and Content-Type header
4. Prints first 500 characters of the JSON response
5. Prints total response length

## Run
mvn compile
java -cp target/classes com.example.HttpURLConnectionExample

## Expected Output
Connecting to: https://jsonplaceholder.typicode.com/posts
Response Code: 200
Content-Type : application/json; charset=utf-8
Response (first 500 chars):
[...]
Total response length: 27521 chars

## Technologies
- Java 17
- Maven
- java.net.HttpURLConnection
- java.net.URL
