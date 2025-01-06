package com.example.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigLoader {
    private final ConcurrentHashMap<String, Properties> configCache;
    private final ConfigMonitor monitor;

    public ConfigLoader() {
        this.configCache = new ConcurrentHashMap<>();
        this.monitor = new ConfigMonitor(this);
    }

    public void startMonitor(String configFile) {
        monitor.startMonitoring(new File(configFile));
    }

    public Properties loadConfig(String configFile) throws Exception {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            configCache.put(configFile, props);
            return props;
        }
    }

    public Properties getConfig(String configFile) {
        return configCache.get(configFile);
    }
} 