package com.github.xdli;

import com.github.xdli.server.*;

public class App {
    public static void main(String[] args) throws Exception {
        // 启动TCP服务器
        new Thread(() -> {
            try {
                new NettyServer(8080).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 启动HTTP服务器
        new Thread(() -> {
            try {
                new HttpServer(8081).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 启动WebSocket服务器
        new Thread(() -> {
            try {
                new WebSocketServer(8082).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 启动文件服务器
        new Thread(() -> {
            try {
                new FileServer(8083).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
