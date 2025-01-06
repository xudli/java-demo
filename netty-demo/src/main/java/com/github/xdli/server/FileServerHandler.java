package com.github.xdli.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedFile;
import java.io.File;
import java.io.RandomAccessFile;

public class FileServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        File file = new File(msg);
        if (file.exists() && file.isFile()) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            ctx.writeAndFlush(new ChunkedFile(randomAccessFile));
        } else {
            ctx.writeAndFlush("文件不存在: " + msg + "\n");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
} 