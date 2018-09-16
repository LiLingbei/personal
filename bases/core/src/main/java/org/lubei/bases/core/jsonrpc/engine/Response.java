package org.lubei.bases.core.jsonrpc.engine;

import org.lubei.bases.core.exception.BusinessException;
import org.lubei.bases.core.jsonrpc.constant.Constant;

import com.google.common.base.Function;
import groovy.lang.MissingMethodException;

import java.lang.reflect.InvocationTargetException;

/**
 * 响应定义
 */
public class Response<T> {

    private String jsonrpc = Constant.VERSION;
    private T result;
    private RpcException error;
    private Object id;

    public Response() {
    }

    public Response(Object id, T result) {
        this.id = id;
        this.result = result;
    }

    public static Response newError(RpcException.Builder builder) {
        Response response = new Response();
        response.error = builder.build();
        return response;
    }

    public static Response newError(Object id, int code, String message) {
        Response response = new Response();
        response.error = new RpcException.Builder(message).code(code).id(id).build();
        return response;
    }

    public static Response newError(Object id, int code, String message, Object t) {
        Response response = new Response();
        response.error = new RpcException.Builder(message).code(code).id(id).data(t).build();
        return response;
    }

    public static RpcException exception(Throwable throwable,
                                         Function<Throwable, AppException> appExceptionHandler) {
        if (throwable instanceof InvocationTargetException) {
            throwable = throwable.getCause();
        }
        if (appExceptionHandler != null) {
            AppException ae = appExceptionHandler.apply(throwable);
            if (ae != null) {
                ae.setStackTrace(throwable.getStackTrace());
                throwable = ae;
            }
        }
        RpcException exception;
        String msg = throwable.getMessage();
        if (throwable instanceof BusinessException) {
            BusinessException be = (BusinessException) throwable;
            exception = new RpcException.Builder(msg).code(be.getStatus())
                    .data(be.getData()).cause(throwable).build();
        } else if (throwable instanceof AppException) {
            AppException appException = (AppException) throwable;
            exception = new RpcException.Builder(msg).code(appException.getCode())
                    .data(appException.getData()).cause(throwable).build();
        } else if (throwable instanceof IllegalArgumentException) {
            exception = new RpcException.Builder(msg).code(Constant.EC_INVALID_PARAMS)
                    .data("Invalid params").build();
        } else if (throwable instanceof MissingMethodException) {
            exception = new RpcException.Builder(msg).code(Constant.EC_METHOD_NOT_FOUND)
                    .data("Method not found").build();
        } else {
            exception = new RpcException.Builder(msg).code(Constant.EC_DEFAULT_APP_ERROR)
                    .data("系统内部错误").cause(throwable).build();
        }
        return exception;
    }

    public static Response exception(Object id, Throwable t,
                                     Function<Throwable, AppException> appExceptionHandler) {
        RpcException exception = exception(t, appExceptionHandler);
        exception.setId(id);
        Response response = new Response();
        response.error = exception;
        return response;
    }

    public RpcException getError() {
        return error;
    }

    public void setError(RpcException error) {
        this.error = error;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
