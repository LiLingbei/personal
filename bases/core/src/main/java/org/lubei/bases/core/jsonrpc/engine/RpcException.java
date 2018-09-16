package org.lubei.bases.core.jsonrpc.engine;


import org.lubei.bases.core.jsonrpc.constant.Constant;
import org.lubei.bases.core.jsonrpc.tools.GroovyRpcServlet;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * RPC异常实体
 */
public class RpcException extends RuntimeException {

    public static final String NAME = GroovyRpcServlet.class.getName();
    private static final long serialVersionUID = -2216115576335686076L;
    /**
     * 请求ID
     */
    Object id;
    /**
     * 异常代码
     */
    int code;
    /**
     * 消息
     */
    String message;
    /**
     * 附加数据
     */
    Object data;

    public RpcException() {
    }

    public RpcException(Builder builder) {
        super(builder.message, null, false, false);
        id = builder.id;
        code = builder.code;
        message = builder.message;
        data = builder.data;
        setStackTrace(builder.stack);
    }

    public RpcException(String msg) {
        super(msg);
        this.code = Constant.EC_INTERNAL_ERROR;
    }

    public RpcException(Throwable cause) {
        super(cause);
        this.code = Constant.EC_INTERNAL_ERROR;
    }

    public RpcException(String msg, Throwable cause) {
        super(msg, cause);
        this.code = Constant.EC_INTERNAL_ERROR;
    }

    public static Builder newBuilder(String message) {
        return new Builder(message);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(RpcException.class)
                .add("id", this.id)
                .add("code", this.code)
                .add("message", this.message)
                .add("data", this.data)
                .toString();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        if (stackTrace == null) {
            return;
        }
        int i = 1;
        for (StackTraceElement element : stackTrace) {
            if (Objects.equal(element.getClassName(), NAME)) {
                StackTraceElement[] ns = new StackTraceElement[i];
                System.arraycopy(stackTrace, 0, ns, 0, i);
                super.setStackTrace(ns);
                break;
            }
            i++;
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static class Builder {

        Object id;
        int code = Constant.EC_INTERNAL_ERROR;
        String message;
        Object data;
        StackTraceElement[] stack;
        Throwable cause;

        public Builder(String message) {
            this.message = message;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder stack(StackTraceElement[] stack) {
            this.stack = stack;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            this.stack = cause.getStackTrace();
            return this;
        }

        public RpcException build() {
            return new RpcException(this);
        }
    }

}
