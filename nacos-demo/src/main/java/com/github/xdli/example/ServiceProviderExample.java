package com.github.xdli.example;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ServiceProviderExample {
    public static void main(String[] args) throws Exception {
        String serviceName = "user-service";
        String ip = "192.168.1.100";
        int port = 8080;

        // 注册服务
        URL registerUrl = new URL("http://localhost:8848/registry/register");
        HttpURLConnection conn = (HttpURLConnection) registerUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        
        String data = String.format("%s,%s,%d", serviceName, ip, port);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes());
        }
        conn.getResponseCode();

        // 发送心跳
        while (true) {
            URL heartbeatUrl = new URL("http://localhost:8848/registry/heartbeat");
            conn = (HttpURLConnection) heartbeatUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
            }
            conn.getResponseCode();
            
            System.out.println("Heartbeat sent for " + serviceName);
            TimeUnit.SECONDS.sleep(5);
        }
    }
} 