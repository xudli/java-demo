package com.github.xdli.example;

import com.github.xdli.client.NettyClient;
import com.github.xdli.protocol.MessageProtocol;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ClientExample {
    public static void main(String[] args) throws Exception {
        // 创建客户端并连接服务器
        NettyClient client = new NettyClient("localhost", 8080);
        client.run();
        Channel channel = client.getChannel();

        // 示例1: 发送心跳
        sendHeartbeat(channel);
        TimeUnit.SECONDS.sleep(1);

        // 示例2: 发送业务消息
        sendBusinessMessage(channel, "Hello, Server!");
        TimeUnit.SECONDS.sleep(1);

        // 示例3: 发送文件数据
        sendFileData(channel, "This is file content".getBytes(StandardCharsets.UTF_8));
        TimeUnit.SECONDS.sleep(1);

        // 保持程序运行一段时间以接收响应
        TimeUnit.SECONDS.sleep(5);
    }

    private static void sendHeartbeat(Channel channel) {
        MessageProtocol message = new MessageProtocol();
        message.setType((byte) 0);
        message.setContent(new byte[0]);
        message.setLength(0);
        channel.writeAndFlush(message);
        System.out.println("发送心跳包");
    }

    private static void sendBusinessMessage(Channel channel, String content) {
        MessageProtocol message = new MessageProtocol();
        message.setType((byte) 1);
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        message.setContent(contentBytes);
        message.setLength(contentBytes.length);
        channel.writeAndFlush(message);
        System.out.println("发送业务消息: " + content);
    }

    private static void sendFileData(Channel channel, byte[] fileData) {
        MessageProtocol message = new MessageProtocol();
        message.setType((byte) 2);
        message.setContent(fileData);
        message.setLength(fileData.length);
        channel.writeAndFlush(message);
        System.out.println("发送文件数据，大小: " + fileData.length + " bytes");
    }
} 