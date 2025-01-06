package com.example.config.examples;

import java.io.FileOutputStream;
import java.util.Properties;

import com.example.config.ConfigLoader;

public class ConfigDemo {
    public static void main(String[] args) {
        try {
            // 创建测试配置文件
            createTestConfig("test.properties");
            
            // 初始化配置加载器
            ConfigLoader loader = new ConfigLoader();
            
            System.out.println("=== 配置动态加载演示 ===");
            
            // 开始监控配置文件
            loader.startMonitor("test.properties");
            
            // 加载初始配置
            Properties props = loader.loadConfig("test.properties");
            System.out.println("初始配置: " + props.getProperty("version"));
            
            // 等待2秒，然后更新配置
            Thread.sleep(2000);
            updateTestConfig("test.properties");
            
            // 等待配置重新加载
            Thread.sleep(1000);
            props = loader.getConfig("test.properties");
            System.out.println("更新后配置: " + props.getProperty("version"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void createTestConfig(String filename) throws Exception {
        Properties props = new Properties();
        props.setProperty("version", "1.0");
        try (FileOutputStream out = new FileOutputStream(filename)) {
            props.store(out, "Test Config");
        }
    }
    
    private static void updateTestConfig(String filename) throws Exception {
        Properties props = new Properties();
        props.setProperty("version", "2.0");
        try (FileOutputStream out = new FileOutputStream(filename)) {
            props.store(out, "Updated Test Config");
        }
    }
} 