package org.lubei.bases.core.jsonrpc.engine;


import org.lubei.bases.core.jsonrpc.constant.Constant;

/**
 * RPC应用异常实体
 */
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 8060861060114521973L;
    /**
     * 详细对象
     */
    public Object data;
    /**
     * 异常代码
     */
    private int code = Constant.EC_DEFAULT_APP_ERROR;

    public AppException() {
    }

    /**
     * 构造器
     *
     * @param code    代码
     * @param message 消息
     */
    public AppException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造器
     *
     * @param message 消息
     */
    public AppException(String message) {
        super(message);
    }

    public AppException(Throwable t) {
        super(t);
    }

    /**
     * 构造器
     *
     * @param code 代码
     * @param t    源异常
     */
    public AppException(int code, Throwable t) {
        super(t);
        this.code = code;
    }

    /**
     * 构造器
     *
     * @param message 消息
     * @param t       源异常
     */
    public AppException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * 构造器
     *
     * @param code    代码
     * @param message 消息
     * @param t       源异常
     */
    public AppException(int code, String message, Throwable t) {
        super(message, t);
        this.code = code;
    }

    /**
     * 构造器
     *
     * @param code    代码
     * @param message 消息
     * @param data    附加对象
     */
    public AppException(int code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    /**
     * 构造器
     *
     * @param code    代码
     * @param message 消息
     * @param data    附加对象
     * @param t       源异常
     */
    public AppException(int code, String message, Object data, Throwable t) {
        super(message, t);
        this.code = code;
        this.data = data;
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
}
