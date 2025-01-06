package com.example.plugin.examples;

import com.example.plugin.IPlugin;
import com.example.plugin.PluginManager;

public class PluginDemo {
    public static void main(String[] args) {
        try {
            // 初始化插件管理器
            PluginManager manager = new PluginManager("target/classes/com/example/plugin/examples");
            
            System.out.println("=== 插件系统演示 ===");
            
            // 加载并执行插件
            IPlugin plugin = manager.loadPlugin("DemoPlugin");
            plugin.execute();
            
            // 等待2秒，模拟插件更新
            System.out.println("\n等待2秒，模拟插件更新...\n");
            Thread.sleep(2000);
            
            // 重新加载并执行插件
            plugin = manager.loadPlugin("DemoPlugin");
            plugin.execute();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 