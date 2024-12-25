package com.example.rpc.client;

import com.example.rpc.codec.RpcDecoder;
import com.example.rpc.codec.RpcEncoder;
import com.example.rpc.common.RpcRequest;
import com.example.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RpcClient {
    private final String host;
    private final int port;
    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private Channel channel;
    private final Map<String, CompletableFuture<RpcResponse>> pendingRequests = new ConcurrentHashMap<>();
    
    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcDecoder(RpcResponse.class));
                        pipeline.addLast(new RpcEncoder());
                        pipeline.addLast(new RpcClientHandler(pendingRequests));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true);
    }
    
    public void connect() throws InterruptedException {
        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();
    }
    
    public RpcResponse sendRequest(RpcRequest request) throws Exception {
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        pendingRequests.put(request.getRequestId(), future);
        
        channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                future.completeExceptionally(channelFuture.cause());
                channel.close();
            }
        });
        
        return future.get(5, TimeUnit.SECONDS);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceClass) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
            serviceClass.getClassLoader(),
            new Class<?>[]{serviceClass},
            (proxy, method, args) -> {
                RpcRequest request = new RpcRequest();
                request.setRequestId(UUID.randomUUID().toString());
                request.setInterfaceName(serviceClass.getName());
                request.setMethodName(method.getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);
                
                RpcResponse response = sendRequest(request);
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getError());
                }
                return response.getResult();
            }
        );
    }
    
    public void close() {
        group.shutdownGracefully();
    }
} 