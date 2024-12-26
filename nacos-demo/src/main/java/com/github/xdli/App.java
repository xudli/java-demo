package com.github.xdli;

import com.github.xdli.server.RegistryServer;

public class App {
    public static void main(String[] args) {
        RegistryServer server = RegistryServer.getInstance(8848);
        server.start();
    }
}
