package com.example.moneybox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolSingleton {
    private ThreadPoolSingleton() {}

    private static final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public static ExecutorService getThreadPool() {
        return threadPool;
    }

}
