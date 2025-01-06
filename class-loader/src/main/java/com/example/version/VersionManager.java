package com.example.version;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class VersionManager {
    private final Map<String, ClassVersionLoader> versionLoaders;

    public VersionManager() {
        this.versionLoaders = new HashMap<>();
    }

    public void loadVersion(String version, String path) throws Exception {
        URL[] urls = new URL[]{new File(path).toURI().toURL()};
        ClassVersionLoader loader = new ClassVersionLoader(urls);
        versionLoaders.put(version, loader);
    }

    public Class<?> loadClass(String version, String className) throws Exception {
        ClassVersionLoader loader = versionLoaders.get(version);
        if (loader == null) {
            throw new IllegalArgumentException("Version " + version + " not found");
        }
        return loader.loadClass(className);
    }

    public void unloadVersion(String version) {
        ClassVersionLoader loader = versionLoaders.remove(version);
        if (loader != null) {
            try {
                loader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
} 