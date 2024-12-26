package com.github.xdli.model;

public class ServiceInstance {
    private String serviceName;
    private String ip;
    private int port;
    private boolean healthy;
    private long lastHeartbeat;

    public ServiceInstance(String serviceName, String ip, int port) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
        this.healthy = true;
        this.lastHeartbeat = System.currentTimeMillis();
    }

    // Getters and setters
    public String getServiceName() {
        return serviceName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }
} 