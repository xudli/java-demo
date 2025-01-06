package com.github.xdli.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.github.xdli.codec.MessageDecoder;
import com.github.xdli.codec.MessageEncoder;

public class NettyClient {
    private final String host;
    private final int port;
    private Channel channel;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline()
                       .addLast(new MessageDecoder())
                       .addLast(new MessageEncoder())
                       .addLast(new NettyClientHandler());
                 }
             });

            channel = b.connect(host, port).sync().channel();
            System.out.println("连接服务器成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    public Channel getChannel() {
        return channel;
    }
} 