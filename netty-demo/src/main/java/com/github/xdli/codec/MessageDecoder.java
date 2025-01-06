package com.github.xdli.codec;

import com.github.xdli.protocol.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 5) { // 4字节长度 + 1字节类型
            return;
        }
        
        in.markReaderIndex();
        int length = in.readInt();
        byte type = in.readByte();
        
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        
        byte[] content = new byte[length];
        in.readBytes(content);
        
        MessageProtocol message = new MessageProtocol();
        message.setLength(length);
        message.setType(type);
        message.setContent(content);
        
        out.add(message);
    }
} 