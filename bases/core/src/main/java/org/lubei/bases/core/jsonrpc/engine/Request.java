package org.lubei.bases.core.jsonrpc.engine;

import org.lubei.bases.core.jsonrpc.constant.Constant;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

/**
 * rpc请求对象
 */
public class Request {

    private String jsonrpc;
    private String method;
    private Object params;
    private Object id;

    public Request() {
    }

    public Request(String method, Object params, Object id) {
        this.jsonrpc = Constant.VERSION;
        this.method = method;
        this.params = params;
        this.id = id;
    }

    public static Response checkOne(Request request, Object id) {
        // check 1: jsonrpc
        String jsonrpc = request.getJsonrpc();
        if (jsonrpc == null) {
            return Response.newError(id, Constant.EC_INVALID_REQUEST,
                                        Constant.MSG_INVALID_REQUEST + "jsonrpc field not found");
        }
        if (!jsonrpc.equals(Constant.VERSION)) {
            return Response.newError(id, Constant.EC_INVALID_REQUEST,
                                        Constant.MSG_INVALID_REQUEST + "jsonrpc must "
                                        + Constant.VERSION);
        }
        // check 2: method
        String methodName = request.getMethod();
        if (Strings.isNullOrEmpty(methodName)) {
            return Response.newError(id, Constant.EC_INVALID_REQUEST,
                                        Constant.MSG_INVALID_REQUEST + "method field empty");
        }
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("jsonrpc", jsonrpc)
                .add("method", method)
                .add("params", params)
                .add("id", id)
                .toString();
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }
}
