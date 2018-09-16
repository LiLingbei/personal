package org.lubei.bases.core.jsonrpc.constant;

/**
 * RPC相关的静态变量定义
 */
public class Constant {

    // JSONRPC version, only support 2.0
    public static final String VERSION = "2.0";
    // JSONRPC filed define
    public static final String FIELD_ID = "id";
    public static final String FIELD_JSONRPC = "jsonrpc";
    public static final String FIELD_METHOD = "method";
    public static final String FIELD_PARAMS = "params";
    public static final String FIELD_RESULT = "result";
    public static final String FIELD_ERROR = "error";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_DATA = "data";
    // JSONRPC filed define
    public static final String METHOD_RPC = "rpc.";
    public static final String NAME_SERVICE = "ns.";
    // JSONRPC error code define
    public static final int EC_PARSE_ERROR = -32700;
    public static final int EC_INVALID_REQUEST = -32600;
    public static final int EC_METHOD_NOT_FOUND = -32601;
    public static final int EC_INVALID_PARAMS = -32602;
    public static final int EC_INTERNAL_ERROR = -32603;
    public static final int EC_DEFAULT_APP_ERROR = -1;
    // JSONRPC error message define
    public static final String MSG_INVALID_REQUEST = "Invalid Request:";
    public static final String MSG_PARSE_ERROR = "Parse error:";
    // others
    public static final byte[] EMPTY_BYTES = new byte[]{};
    // HTTP HEADERS
    public static final String JSON = "json";
    public static final String DEFAULT_CONTENT_TYPE = "application/json; charset=UTF-8";
    public static final int MIN_GZIP_LEN = 256;
    public static final String GZIP = "gzip";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    // RPC Dymatic Register Event Type
    public static final String RPC_RESOURCE_QUEUE = "rpc.resource.queue";
    public static final String RPC_CONTROL_QUEUE = "rpc.control.queue";
    public static final String RPC_MSG_REG = "rpc_register";
    public static final String RPC_MSG_UNREG = "rpc_unregister";


    private Constant() {
    }
}
