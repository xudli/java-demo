package com.example.version;

import java.net.URL;
import java.net.URLClassLoader;

public class ClassVersionLoader extends URLClassLoader {
    public ClassVersionLoader(URL[] urls) {
        super(urls, null);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.")) {
            return ClassLoader.getSystemClassLoader().loadClass(name);
        }
        
        try {
            Class<?> c = findClass(name);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } catch (ClassNotFoundException e) {
            return ClassLoader.getSystemClassLoader().loadClass(name);
        }
    }
} 