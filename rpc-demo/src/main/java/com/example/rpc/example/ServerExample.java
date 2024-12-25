package com.example.rpc.example;

import com.example.rpc.registry.ServiceRegistry;
import com.example.rpc.server.RpcServer;

public class ServerExample {
    public static void main(String[] args) throws InterruptedException {
        ServiceRegistry registry = new ServiceRegistry();
        registry.register(HelloService.class.getName(), new HelloServiceImpl());
        
        RpcServer server = new RpcServer(8080, registry);
        server.start();
    }
} 