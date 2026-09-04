package com.example.walletjournal.model;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared background/main-thread executors so Room I/O never runs on the UI thread.
 */
public final class AppExecutors {

    private static final ExecutorService DISK_IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    public static void diskIO(Runnable task) {
        DISK_IO.execute(task);
    }

    public static void mainThread(Runnable task) {
        MAIN_THREAD.post(task);
    }
}
