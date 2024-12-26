package com.github.xdli.server;

import com.github.xdli.model.ServiceInstance;
import com.github.xdli.model.Configuration;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

public class RegistryServer {
    private static volatile RegistryServer instance;
    private final int port;
    private final Map<String, Map<ServiceInstance, Boolean>> serviceRegistry;
    private final Map<String, Configuration> configurationCenter;
    private final ScheduledExecutorService scheduler;
    private boolean started = false;
    private HttpServer httpServer;

    private RegistryServer(int port) {
        this.port = port;
        this.serviceRegistry = new ConcurrentHashMap<>();
        this.configurationCenter = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public static RegistryServer getInstance(int port) {
        if (instance == null) {
            synchronized (RegistryServer.class) {
                if (instance == null) {
                    instance = new RegistryServer(port);
                }
            }
        }
        return instance;
    }

    public synchronized void start() {
        if (!started) {
            try {
                // 启动 HTTP 服务器
                httpServer = HttpServer.create(new InetSocketAddress(port), 0);
                
                // 注册服务接口
                httpServer.createContext("/registry/register", exchange -> {
                    if ("POST".equals(exchange.getRequestMethod())) {
                        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                        BufferedReader br = new BufferedReader(isr);
                        String[] parts = br.readLine().split(",");
                        
                        ServiceInstance instance = new ServiceInstance(parts[0], parts[1], Integer.parseInt(parts[2]));
                        registerService(instance);
                        
                        exchange.sendResponseHeaders(200, 0);
                        exchange.close();
                    }
                });

                // 获取服务列表接口
                httpServer.createContext("/registry/services", exchange -> {
                    if ("GET".equals(exchange.getRequestMethod())) {
                        String serviceName = exchange.getRequestURI().getQuery().split("=")[1];
                        Set<ServiceInstance> instances = getServiceInstances(serviceName);
                        
                        StringBuilder response = new StringBuilder();
                        for (ServiceInstance instance : instances) {
                            response.append(instance.getServiceName())
                                   .append(",")
                                   .append(instance.getIp())
                                   .append(",")
                                   .append(instance.getPort())
                                   .append("\n");
                        }
                        
                        exchange.sendResponseHeaders(200, response.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.toString().getBytes());
                        }
                    }
                });

                // 心跳接口
                httpServer.createContext("/registry/heartbeat", exchange -> {
                    if ("POST".equals(exchange.getRequestMethod())) {
                        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                        BufferedReader br = new BufferedReader(isr);
                        String[] parts = br.readLine().split(",");
                        
                        String serviceName = parts[0];
                        String ip = parts[1];
                        int port = Integer.parseInt(parts[2]);
                        
                        for (ServiceInstance instance : getServiceInstances(serviceName)) {
                            if (instance.getIp().equals(ip) && instance.getPort() == port) {
                                instance.updateHeartbeat();
                                break;
                            }
                        }
                        
                        exchange.sendResponseHeaders(200, 0);
                        exchange.close();
                    }
                });

                httpServer.setExecutor(Executors.newFixedThreadPool(10));
                httpServer.start();
                
                System.out.println("Registry server starting on port " + port);
                startHealthCheck();
                started = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerService(ServiceInstance instance) {
        serviceRegistry.computeIfAbsent(instance.getServiceName(), k -> new ConcurrentHashMap<>())
                      .put(instance, Boolean.TRUE);
        System.out.println("Service registered: " + instance.getServiceName() + " at " + instance.getIp() + ":" + instance.getPort());
    }

    public Set<ServiceInstance> getServiceInstances(String serviceName) {
        Map<ServiceInstance, Boolean> instances = serviceRegistry.get(serviceName);
        if (instances == null) {
            return Collections.emptySet();
        }
        return instances.keySet();
    }

    public void updateConfig(String dataId, String group, String content) {
        String key = dataId + ":" + group;
        Configuration config = configurationCenter.get(key);
        if (config == null) {
            config = new Configuration(dataId, group, content);
            configurationCenter.put(key, config);
        } else {
            config.setContent(content);
        }
        System.out.println("Configuration updated: " + key);
    }

    public Configuration getConfig(String dataId, String group) {
        return configurationCenter.get(dataId + ":" + group);
    }

    private void startHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            serviceRegistry.forEach((serviceName, instances) -> {
                instances.entrySet().removeIf(entry -> {
                    ServiceInstance instance = entry.getKey();
                    if (now - instance.getLastHeartbeat() > 15000) { // 15秒没有心跳就认为服务不健康
                        System.out.println("Service instance removed due to no heartbeat: " + 
                            instance.getServiceName() + " at " + instance.getIp() + ":" + instance.getPort());
                        return true;
                    }
                    return false;
                });
            });
        }, 0, 5, TimeUnit.SECONDS);
    }
} 