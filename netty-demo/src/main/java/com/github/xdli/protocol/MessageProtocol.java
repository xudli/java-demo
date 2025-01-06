package com.github.xdli.protocol;

import lombok.Data;

@Data
public class MessageProtocol {
    private int length;
    private byte type;  // 0: 心跳, 1: 业务消息, 2: 文件传输
    private byte[] content;
} 