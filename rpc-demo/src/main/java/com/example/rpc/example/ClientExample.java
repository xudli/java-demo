package com.example.rpc.example;

import com.example.rpc.client.RpcClient;

public class ClientExample {
    public static void main(String[] args) throws InterruptedException {
        RpcClient client = new RpcClient("localhost", 8080);
        client.connect();
        
        HelloService helloService = client.create(HelloService.class);
        String result = helloService.sayHello("World");
        System.out.println(result);
        
        client.close();
    }
} 