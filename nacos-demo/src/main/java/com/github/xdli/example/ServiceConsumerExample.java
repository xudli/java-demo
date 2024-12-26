package com.github.xdli.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ServiceConsumerExample {
    public static void main(String[] args) throws Exception {
        while (true) {
            // 查询服务
            URL url = new URL("http://localhost:8848/registry/services?name=user-service");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
                );
                
                String line;
                boolean found = false;
                while ((line = reader.readLine()) != null) {
                    found = true;
                    String[] parts = line.split(",");
                    System.out.println("Found service: " + parts[0] + " at " + parts[1] + ":" + parts[2]);
                }
                
                if (!found) {
                    System.out.println("No available user-service instances");
                }
            }
            
            TimeUnit.SECONDS.sleep(3);
        }
    }
} 