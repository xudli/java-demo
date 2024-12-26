# 简易版 Nacos 实现

这是一个简化版的注册中心和配置中心实现，模仿了 Nacos 的核心功能。

## 功能特性

1. 服务注册与发现
   - 服务注册
   - 服务发现
   - 服务健康检查
   - 心跳机制

2. 配置中心
   - 配置发布
   - 配置获取
   - 配置版本管理

## 项目结构

```
src/main/java/com/github/xdli/
├── App.java                    # 启动类
├── model/
│   ├── ServiceInstance.java    # 服务实例模型
│   └── Configuration.java      # 配置模型
├── server/
│   └── RegistryServer.java     # 注册中心服务器实现
└── example/
    ├── ServiceProviderExample.java    # 服务提供者示例
    ├── ServiceConsumerExample.java    # 服务消费者示例
    └── ConfigurationExample.java      # 配置中心使用示例
```

## 核心实现

1. 注册中心服务器 (RegistryServer)
   - 使用单例模式确保只有一个服务器实例
   - 基于 HttpServer 提供 RESTful API
   - 使用 ConcurrentHashMap 存储服务实例和配置信息
   - 实现定时健康检查机制

2. HTTP API 接口
   - POST /registry/register - 服务注册
   - GET /registry/services?name={serviceName} - 服务发现
   - POST /registry/heartbeat - 服务心跳

## 快速开始

1. 启动注册中心服务器

```bash
java -cp target/nacos-demo-1.0-SNAPSHOT.jar com.github.xdli.example.ConfigurationExample
```

2. 运行服务提供者示例

```bash
java -cp target/nacos-demo-1.0-SNAPSHOT.jar com.github.xdli.App
java -cp target/nacos-demo-1.0-SNAPSHOT.jar com.github.xdli.example.ServiceProviderExample
```

3. 运行服务消费者示例

```bash
java -cp target/nacos-demo-1.0-SNAPSHOT.jar com.github.xdli.example.ServiceConsumerExample
```

## 实现细节

1. 服务注册
   - 服务实例包含服务名、IP、端口等信息
   - 支持同一服务多实例注册

2. 服务发现
   - 支持根据服务名查询实例列表
   - 自动过滤不健康的实例

3. 健康检查
   - 服务提供者定期发送心跳
   - 超过 15 秒未收到心跳则移除服务实例

4. 配置管理
   - 支持配置的发布和更新
   - 记录配置版本信息

## 技术栈

- Java 8
- com.sun.net.httpserver
- Maven

## 注意事项

1. 这是一个简化版实现，仅用于学习和演示
2. 生产环境建议使用官方 Nacos
3. 当前实现没有持久化存储
4. 服务发现采用点对点模式，没有实现集群

## 扩展方向

1. 添加服务负载均衡
2. 实现配置变更通知机制
3. 添加持久化存储
4. 实现集群功能
5. 添加权限认证
6. 实现服务分组功能

这个 README.md 文件详细说明了项目的结构、功能和使用方法，可以帮助其他开发者快速理解和使用这个简化版的注册中心实现。您可以根据需要进一步补充或修改内容。
