package com.example.config;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfigMonitor {
    private final ConfigLoader loader;
    private final ScheduledExecutorService executor;
    private long lastModified;

    public ConfigMonitor(ConfigLoader loader) {
        this.loader = loader;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void startMonitoring(File configFile) {
        this.lastModified = configFile.lastModified();
        
        executor.scheduleAtFixedRate(() -> {
            try {
                long currentModified = configFile.lastModified();
                if (currentModified > lastModified) {
                    loader.loadConfig(configFile.getPath());
                    lastModified = currentModified;
                    System.out.println("配置已更新: " + configFile.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
} 