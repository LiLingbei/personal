package org.lubei.bases.core.exception;

/**
 * 系统业务异常
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private Integer status = -1;
    private Object data = null;


    /**
     * 构造方法，此时错误码为-1
     * 
     * @param cause 异常
     */
    public BusinessException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructors.
     * 
     * @param status 错误码
     */
    public BusinessException(final Integer status) {
        this.status = status;
    }
    /**
     * Constructors，此时错误码为-1
     *
     * @param msg 错误信息
     */
    public BusinessException(final String msg) {
        super(msg);
    }
    /**
     * Constructors.
     * 
     * @param status 错误码
     * @param msg 错误信息
     */
    public BusinessException(final Integer status, final String msg) {
        super(msg);
        this.status = status;
    }

    /**
     * 构造方法，此时错误码为-1
     * 
     * @param message 消息
     * @param cause 异常
     */
    public BusinessException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructors.
     * 
     * @param status 错误码
     * @param cause 异常
     */
    public BusinessException(final Integer status, final Throwable cause) {
        super(cause);
        this.status = status;
    }

    /**
     * Constructors.
     * 
     * @param status 错误码
     * @param msg 错误信息
     * @param cause 异常
     */
    public BusinessException(final Integer status, final String msg, final Throwable cause) {
        super(msg, cause);
        this.status = status;
    }

    /**
     * @return data - 异常信息包含对象
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data - 异常信息包含对象.
     */
    public void setData(final Object data) {
        this.data = data;
    }

    /**
     * @return status - 错误码
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status - 错误码
     */
    public void setStatus(final Integer status) {
        this.status = status;
    }

}
