package com.example.rpc.server;

import com.example.rpc.common.RpcRequest;
import com.example.rpc.common.RpcResponse;
import com.example.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.reflect.Method;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final ServiceRegistry serviceRegistry;
    
    public RpcServerHandler(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse response;
        try {
            Object service = serviceRegistry.getService(request.getInterfaceName());
            Method method = service.getClass().getMethod(
                request.getMethodName(), 
                request.getParameterTypes()
            );
            Object result = method.invoke(service, request.getParameters());
            response = RpcResponse.success(request.getRequestId(), result);
        } catch (Exception e) {
            response = RpcResponse.error(request.getRequestId(), e.getMessage());
        }
        
        ctx.writeAndFlush(response);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
} 