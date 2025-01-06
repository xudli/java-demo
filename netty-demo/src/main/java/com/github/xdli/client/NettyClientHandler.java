package com.github.xdli.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.github.xdli.protocol.MessageProtocol;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof MessageProtocol) {
            MessageProtocol message = (MessageProtocol) msg;
            System.out.println("收到服务器响应: type=" + message.getType() + 
                             ", length=" + message.getLength());
            
            if (message.getType() == 0) {
                System.out.println("收到心跳响应");
            } else {
                System.out.println("收到消息内容: " + new String(message.getContent()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
} 