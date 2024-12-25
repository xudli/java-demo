package com.example.rpc.common;

import java.io.Serializable;

public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String requestId;    // 对应请求的ID
    private Object result;       // 返回结果
    private String error;        // 错误信息
    private boolean success;     // 调用是否成功
    
    public static RpcResponse success(String requestId, Object result) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setResult(result);
        response.setSuccess(true);
        return response;
    }
    
    public static RpcResponse error(String requestId, String error) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setError(error);
        response.setSuccess(false);
        return response;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
} 