# Individual Work 5.1 — Console Download Manager

## Description
Two Maven projects implementing console download managers using `URL` and `URLConnection` objects.

## Project 1 — Single-thread downloader
Downloads a single file sequentially with progress display.

### Run
mvn compile
java -cp target/classes com.example.DownloaderApp <url> <outputFile>

### Example
java -cp target/classes com.example.DownloaderApp https://picsum.photos/600/400 photo.jpg

## Project 2 — Multi-thread downloader
Downloads multiple files simultaneously using a thread pool (4 threads).

### Run
mvn compile
java -cp target/classes org.example.DownloaderApp <outputDir> <url1> <url2> ...

### Example
java -cp target/classes org.example.DownloaderApp ./downloads https://picsum.photos/200/300 https://picsum.photos/400/300 https://picsum.photos/600/400

## Technologies
- Java 17
- Maven
- java.net.URL / URLConnection
- ExecutorService / Callable
