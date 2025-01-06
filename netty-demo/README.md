# Netty 学习示例项目

这是一个全面的Netty学习示例项目，展示了Netty的核心功能和最佳实践。项目实现了TCP、HTTP、WebSocket和文件传输等多种网络通信方式。

## Netty的优势

### 1. 高性能

- **Zero-Copy**: 使用直接内存，减少内存拷贝
- **池化内存管理**: ByteBuf池化，减少GC压力
- **高效的事件循环模型**: 基于Reactor模式，支持异步非阻塞I/O
- **优化的线程模型**: 采用主从Reactor多线程模型

### 2. 设计优势

- **灵活的Pipeline**: 支持动态添加和删除处理器
- **丰富的协议支持**: HTTP、WebSocket、SSL等开箱即用
- **优秀的编解码框架**: 支持自定义协议，解决TCP粘包/拆包问题
- **组件复用性强**: 提供多种可重用的组件

### 3. 易用性

- **详细的文档**: 完善的官方文档和示例
- **简洁的API**: 封装了复杂的NIO操作
- **强大的扩展性**: 支持自定义协议和处理器
- **社区活跃**: 持续更新和维护

### 4. 稳定性

- **经过验证**: 被大量企业使用
- **完善的测试**: 高测试覆盖率
- **成熟的异常处理**: 完善的异常处理机制
- **可靠的安全性**: 内置SSL/TLS支持

## 项目特性

- 自定义协议的TCP服务器/客户端
- HTTP服务器
- WebSocket实时通信
- 文件传输服务器
- 心跳检测
- 编解码器的使用

## 项目结构

```yaml
src/
├── main/
│ └── java/
│ └── com/
│ └── github/
│ └── xdli/
│ ├── server/ # 服务器端实现
│ ├── client/ # 客户端实现
│ ├── codec/ # 编解码器
│ ├── protocol/ # 协议定义
│ └── example/ # 使用示例
```

## 核心实现思路

### 1. 自定义协议设计

我们设计了一个简单但实用的协议格式：

```java
public class MessageProtocol {
    private int length;      // 消息长度
    private byte type;       // 消息类型：0心跳、1业务消息、2文件传输
    private byte[] content;  // 消息内容
}
```

### 2. 编解码器实现

#### 编码器 (MessageEncoder)

将Java对象转换为字节流：

```java
protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) {
    out.writeInt(msg.getLength());    // 写入长度
    out.writeByte(msg.getType());     // 写入类型
    out.writeBytes(msg.getContent()); // 写入内容
}
```

#### 解码器 (MessageDecoder)

将字节流转换回Java对象：

```java
protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    if (in.readableBytes() < 5) { // 最小长度：4(length) + 1(type)
        return;
    }
    
    // 标记当前读位置
    in.markReaderIndex();
    int length = in.readInt();
    byte type = in.readByte();
    
    // 检查消息体是否完整
    if (in.readableBytes() < length) {
        in.resetReaderIndex();
        return;
    }
    
    // 读取消息内容
    byte[] content = new byte[length];
    in.readBytes(content);
    
    // 构造消息对象
    MessageProtocol message = new MessageProtocol();
    message.setLength(length);
    message.setType(type);
    message.setContent(content);
    
    out.add(message);
}
```

### 3. 服务器实现

#### TCP服务器

```java
public class NettyServer {
    private final int port;
    
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline()
                       .addLast(new MessageDecoder())
                       .addLast(new MessageEncoder())
                       .addLast(new NettyServerHandler());
                 }
             });
            
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
```

### 4. 客户端实现

#### TCP客户端示例

```java
public class ClientExample {
    public static void main(String[] args) throws Exception {
        NettyClient client = new NettyClient("localhost", 8080);
        client.run();
        Channel channel = client.getChannel();

        // 发送心跳
        sendHeartbeat(channel);
        
        // 发送业务消息
        sendBusinessMessage(channel, "Hello, Server!");
        
        // 发送文件数据
        sendFileData(channel, "This is file content".getBytes());
    }
}
```

## 关键技术点

1. **Pipeline 设计**
   - 合理安排处理器顺序
   - 编解码器的正确位置
   - 处理器的职责划分

2. **内存管理**
   - 使用 ByteBuf 而不是 byte[]
   - 注意释放资源
   - 防止内存泄漏

3. **并发处理**
   - boss线程负责接收连接
   - worker线程负责处理IO
   - 合理配置线程池大小

4. **协议设计**
   - 处理粘包/拆包问题
   - 消息边界的确定
   - 协议的可扩展性

## 运行说明

1. 启动所有服务器：

```bash
mvn exec:java -Dexec.mainClass="com.github.xdli.App"
```

2. 运行TCP客户端示例：

```bash
mvn exec:java -Dexec.mainClass="com.github.xdli.example.ClientExample"
```

3. 运行WebSocket客户端示例：

```bash
mvn exec:java -Dexec.mainClass="com.github.xdli.example.WebSocketClientExample"
```

## 注意事项

1. 确保正确处理异常
2. 注意资源的释放
3. 合理配置线程池
4. 处理好消息边界
5. 实现优雅关闭

## 扩展建议

1. 添加 SSL/TLS 支持
2. 实现更复杂的业务逻辑
3. 添加连接池管理
4. 实现更多编解码器
5. 添加监控统计功能

## 参考资料

- [Netty官方文档](https://netty.io/wiki/user-guide.html)
- [Netty实战](https://book.douban.com/subject/27038538/)

