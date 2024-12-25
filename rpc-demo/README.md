# 简单RPC框架实现

这是一个基于Java实现的轻量级RPC（远程过程调用）框架。

## 架构设计

### 核心组件

1. **RPC请求/响应模型**
   - `RpcRequest`: 封装调用方法的相关信息
     - 接口名称
     - 方法名称
     - 参数类型
     - 参数值
   - `RpcResponse`: 封装调用结果
     - 返回结果
     - 异常信息

2. **服务注册中心**
   - `ServiceRegistry`: 管理服务端提供的服务实现
   - 使用ConcurrentHashMap存储服务实现
   - 支持服务注册和查找

3. **网络通信层**
   - 基于Netty实现
   - 支持长连接
   - 异步非阻塞IO

4. **序列化层**
   - 使用Jackson进行JSON序列化
   - 支持对象序列化和反序列化

### 工作流程

1. **服务端流程**
   - 服务提供者注册服务实现到ServiceRegistry
   - 启动Netty服务器监听请求
   - 接收到请求后反序列化为RpcRequest
   - 通过反射调用目标方法
   - 将结果封装为RpcResponse并返回

2. **客户端流程**
   - 创建接口的代理对象
   - 将方法调用信息封装为RpcRequest
   - 通过Netty发送请求到服务端
   - 等待并接收RpcResponse
   - 返回调用结果

## 技术特点

1. **高性能**
   - 基于Netty的高性能网络框架
   - 异步非阻塞通信
   - 长连接复用

2. **可扩展性**
   - 模块化设计
   - 可插拔的序列化实现
   - 灵活的服务注册机制

3. **易用性**
   - 简单的API设计
   - 透明的RPC调用
   - 完善的异常处理

## 使用方式

1. **定义服务接口**
```java
public interface HelloService {
    String sayHello(String name);
}
```

2. **实现服务接口**
```java
public class HelloServiceImpl implements HelloService {
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
```

3. **启动服务端**
```java
ServiceRegistry registry = new ServiceRegistry();
registry.register(HelloService.class.getName(), new HelloServiceImpl());
RpcServer server = new RpcServer(registry);
server.start();
```

4. **客户端调用**
```java
RpcClient client = new RpcClient();
HelloService helloService = client.create(HelloService.class);
String result = helloService.sayHello("World");
```

## 待优化项目

1. 服务注册中心可以改用ZooKeeper或Redis实现
2. 添加负载均衡功能
3. 实现服务的自动发现
4. 添加超时重试机制
5. 支持异步调用
6. 添加服务监控和统计功能

## 技术栈

- Java 8
- Netty 4.1.42.Final
- Jackson 2.12.3
- JUnit 4.13.1
```
