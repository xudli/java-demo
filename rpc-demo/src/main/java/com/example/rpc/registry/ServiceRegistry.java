package com.example.rpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    
    public void register(String serviceName, Object serviceImpl) {
        serviceMap.put(serviceName, serviceImpl);
        System.out.println("注册服务: " + serviceName);
    }
    
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RuntimeException("服务未找到: " + serviceName);
        }
        return service;
    }
    
    public boolean hasService(String serviceName) {
        return serviceMap.containsKey(serviceName);
    }
} 