package com.example.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadEngine {

    private final ConcurrentHashMap<Integer, DownloadJob> tasks = new ConcurrentHashMap<>();
    private ExecutorService executor;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final int maxThreads;

    public DownloadEngine(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void loadTasks(List<DownloadJob> taskList, int indexOffset) {
        tasks.clear();
        int idx = indexOffset;
        for (DownloadJob task : taskList) {
            task.setIndex(idx);
            task.setCancelledFlag(cancelled);
            tasks.put(idx, task);
            idx++;
        }
    }

    public void start() {
        if (executor != null && !executor.isShutdown()) {
            return;
        }
        cancelled.set(false);
        int threads = Math.min(maxThreads, Math.max(1, tasks.size()));
        executor = Executors.newFixedThreadPool(threads);
        for (DownloadJob task : tasks.values()) {
            executor.submit(task);
        }
        executor.shutdown(); // no new tasks, but finish existing ones
    }

    public void stop() {
        cancelled.set(true);
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public boolean isRunning() {
        return executor != null && !executor.isTerminated() && !executor.isShutdown();
    }

    public List<DownloadJob> getTasks() {
        return new ArrayList<>(tasks.values());
    }
}
