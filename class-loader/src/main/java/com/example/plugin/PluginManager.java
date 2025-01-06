package com.example.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class PluginManager {
    private final String pluginPath;
    private final Map<String, Class<?>> loadedPlugins;
    private final Map<String, Long> lastModifiedTimes;

    public PluginManager(String pluginPath) {
        this.pluginPath = pluginPath;
        this.loadedPlugins = new HashMap<>();
        this.lastModifiedTimes = new HashMap<>();
    }

    public IPlugin loadPlugin(String pluginName) throws Exception {
        File pluginFile = new File(pluginPath + File.separator + pluginName + ".class");
        
        // 检查插件是否需要重新加载
        if (needReload(pluginName, pluginFile)) {
            URL[] urls = new URL[]{new File(pluginPath).toURI().toURL()};
            try (URLClassLoader loader = new URLClassLoader(urls)) {
                Class<?> pluginClass = loader.loadClass("com.example.plugin.examples." + pluginName);
                loadedPlugins.put(pluginName, pluginClass);
                lastModifiedTimes.put(pluginName, pluginFile.lastModified());
            }
        }

        Class<?> pluginClass = loadedPlugins.get(pluginName);
        return (IPlugin) pluginClass.getDeclaredConstructor().newInstance();
    }

    private boolean needReload(String pluginName, File pluginFile) {
        if (!loadedPlugins.containsKey(pluginName)) {
            return true;
        }
        long lastKnownModified = lastModifiedTimes.get(pluginName);
        return pluginFile.lastModified() > lastKnownModified;
    }
} 