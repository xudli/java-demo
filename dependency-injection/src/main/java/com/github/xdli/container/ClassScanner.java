package com.github.xdli.container;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.github.xdli.exception.BeanException;

public class ClassScanner {
    public Set<Class<?>> scan(String basePackage) throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        
        if (resource == null) {
            throw new BeanException("找不到包: " + basePackage);
        }
        
        File directory = new File(resource.getFile());
        scanDirectory(directory, basePackage, classes);
        return classes;
    }

    private void scanDirectory(File directory, String basePackage, Set<Class<?>> classes) throws Exception {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, basePackage + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = basePackage + "." + 
                        file.getName().substring(0, file.getName().length() - 6);
                    classes.add(Class.forName(className));
                }
            }
        }
    }
} 