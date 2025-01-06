package com.github.xdli.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.github.xdli.protocol.MessageProtocol;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof MessageProtocol) {
            MessageProtocol message = (MessageProtocol) msg;
            System.out.println("收到消息: type=" + message.getType() + 
                             ", length=" + message.getLength());
            
            // 处理消息
            switch (message.getType()) {
                case 0: // 心跳
                    handleHeartbeat(ctx);
                    break;
                case 1: // 业务消息
                    handleBusinessMessage(ctx, message);
                    break;
                case 2: // 文件传输
                    handleFileTransfer(ctx, message);
                    break;
            }
        }
    }

    private void handleHeartbeat(ChannelHandlerContext ctx) {
        MessageProtocol response = new MessageProtocol();
        response.setType((byte) 0);
        response.setContent(new byte[0]);
        response.setLength(0);
        ctx.writeAndFlush(response);
    }

    private void handleBusinessMessage(ChannelHandlerContext ctx, MessageProtocol msg) {
        // 处理业务消息
        System.out.println("处理业务消息: " + new String(msg.getContent()));
    }

    private void handleFileTransfer(ChannelHandlerContext ctx, MessageProtocol msg) {
        // 处理文件传输
        System.out.println("处理文件传输: " + msg.getLength() + " bytes");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
} 