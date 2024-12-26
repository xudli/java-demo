package com.github.xdli.example;

import com.github.xdli.model.Configuration;
import com.github.xdli.server.RegistryServer;
import java.util.concurrent.TimeUnit;

public class ConfigurationExample {
    public static void main(String[] args) throws InterruptedException {
        // 获取注册中心实例
        RegistryServer registryServer = RegistryServer.getInstance(8848);
        registryServer.start();

        // 发布配置
        String dataId = "application.properties";
        String group = "DEFAULT_GROUP";
        
        // 更新配置
        registryServer.updateConfig(dataId, group, 
            "server.port=8080\n" +
            "spring.datasource.url=jdbc:mysql://localhost:3306/test\n" +
            "spring.datasource.username=root"
        );

        // 模拟配置变更
        while (true) {
            // 读取配置
            Configuration config = registryServer.getConfig(dataId, group);
            if (config != null) {
                System.out.println("Current configuration:");
                System.out.println("DataId: " + config.getDataId());
                System.out.println("Group: " + config.getGroup());
                System.out.println("Content: \n" + config.getContent());
                System.out.println("Version: " + config.getVersion());
            }

            TimeUnit.SECONDS.sleep(5);

            // 更新配置
            registryServer.updateConfig(dataId, group, 
                "server.port=8081\n" +
                "spring.datasource.url=jdbc:mysql://localhost:3306/test\n" +
                "spring.datasource.username=root\n" +
                "spring.datasource.password=123456\n" +
                "# Updated at: " + System.currentTimeMillis()
            );
        }
    }
} 