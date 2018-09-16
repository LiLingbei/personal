package org.lubei.bases.core.jsonrpc.tools

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.parser.ParserConfig
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer
import com.google.common.base.Charsets
import com.google.common.collect.ImmutableMap
import org.lubei.bases.core.jsonrpc.constant.Constant
import org.lubei.bases.core.jsonrpc.engine.Request
import org.lubei.bases.core.jsonrpc.engine.Response
import org.lubei.bases.core.jsonrpc.engine.RpcException
import org.lubei.bases.core.util.ToJsonWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import wslite.http.HTTP
import wslite.http.HTTPMethod
import wslite.http.HTTPRequest
import wslite.http.HTTPResponse
import wslite.rest.RESTClient

import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.GZIPOutputStream
/**
 * JSONRPC的HTTP客户端，扩展自wslite
 *
 * @author liwenheng@ruijie.com.cn
 */
public class RpcClient extends RESTClient {

    static {
        def instance = ParserConfig.getGlobalInstance();
        instance.putDeserializer(RpcException.class,
                                 new JavaBeanDeserializer(instance, RpcException.class));
    }

    static final String ACCEPT_ENCODING = "accept-encoding";
    static final String CONTENT_ENCODING = "Content-Encoding";
    static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    // HTTP request headers, for performance
    static final Map<String, String> DEFAULT_REQUEST_HEADER = ImmutableMap.of(
            HTTP.getCONTENT_TYPE_HEADER(), Constant.DEFAULT_CONTENT_TYPE,
            HTTP.getACCEPT_HEADER(), Constant.DEFAULT_CONTENT_TYPE,
            ACCEPT_ENCODING, Constant.GZIP
    );

    final AtomicInteger id = new AtomicInteger();
    final String rpcUrl;
    final URL rpcURL;

    /**
     * 构造函数
     *
     * @param rpcUrl Rpc地址
     */
    public RpcClient(String rpcUrl) {
        this(rpcUrl, rpcUrl);
    }

    /**
     * 构造函数
     *
     * @param rpcUrl Rpc地址
     * @param restUrl rest地址
     */
    public RpcClient(String rpcUrl, String restUrl) {
        super(restUrl, new CookiedHttpClient());
        this.rpcUrl = rpcUrl;
        this.rpcURL = parseUrl(rpcUrl);
    }

    private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data);
        gzip.finish();
        gzip.close();
        byte[] bytes = bos.toByteArray();
        bos.close();
        return bytes;
    }

    private URL parseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("url格式非法", e);
        }
    }

    private Response callType(String method, Type type, Object... params) {
        int cid = id.incrementAndGet();
        Request request = new Request(method, params, cid);
        String json = callRequest(request);
        if (logger.isTraceEnabled()) {
            logger.trace(">> {}", json);
        }
        Response response = JSON.parseObject(json, type);
        if (response.getError() != null) {
            logger.debug("error {}", response.getError());
            throw response.getError();
        }
        return response;
    }

    /**
     * 调用rpc
     *
     * @param type 返回值类型
     * @param method 方法名
     * @param params 方法参数
     * @param < T >          返回值类型
     */
    public <T> T call(Type type, String method, Object... params) {
        type = JsonUtil.responseTypeOf(type);
        return (T) callType(method, type, params).getResult();
    }

    /**
     * 调用rpc
     *
     * @param clazz 返回值类型
     * @param method 方法名
     * @param params 方法参数
     * @param < T >          返回值类型
     */
    public <T> T call(Class<T> clazz, String method, Object... params) {
        Type type = JsonUtil.responseTypeOf(clazz);
        return (T) callType(method, type, params).getResult();
    }

    /**
     * 调用RPC
     *
     * @param method 方法名
     * @param params 方法参数
     * @return 远程方法的执行结果
     * @throws RpcException RPC异常
     */
    public Object call(String method, Object... params) {
        return callType(method, JsonUtil.RESPONSE_OBJECT_TYPE, params).getResult();
    }

    /**
     * 当通过groovy调用方法时，直接调用远程方法
     */
    public Object methodMissing(String name, Object args) {
        Object[] argsArray = (Object[]) args;
        return (argsArray.length == 0) ? call(name) : call(name, args);
    }

    /**
     * RPC通知
     *
     * @param method 方法名
     * @param params 方法参数
     */
    public void notify(String method, Object... params) {
        Request request = new Request(method, params, null);
        callRequest(request);
    }

    /**
     * 批量RPC
     *
     * @param requests RPC请求
     * @return RPC调用结果的列表
     */
    public List<Response> batch(Request... requests) {
        String json;
        try {
            json = callRaw(requests);
        } catch (IOException e) {
            throw new RpcException("IO错误", e);
        }
        if (json.charAt(0) == '[') {
            return JSON.parseArray(json, JsonUtil.RESPONSE_OBJECT_TYPE);
        } else if (json.charAt(0) == '{') {
            Response response = JSON.parseObject(json, JsonUtil.RESPONSE_OBJECT_TYPE);
            throw response.getError();
        } else {
            throw new RpcException(JsonUtil.NOT_VALID_JSON);
        }
    }

    private String callRaw(Object req) throws IOException {
        HTTPRequest request = new HTTPRequest();
        request.setMethod(HTTPMethod.POST);
        request.setSslTrustAllCerts(true);
        request.setUrl(rpcURL);

        byte[] data = JSON.toJSONBytes(req);

        Map headers = request.getHeaders();
        headers.putAll(DEFAULT_REQUEST_HEADER);
        if (data.length > Constant.MIN_GZIP_LEN) {
            headers.put(CONTENT_ENCODING, Constant.GZIP);
            byte[] bytes = gzip(data);
            logger.trace("send gzip: {} to {}", data.length, bytes.length);
            request.setData(bytes);
        } else {
            request.setData(data);
        }
        HTTPResponse resp = this.getHttpClient().execute(request);
        return new String(resp.getData(), Charsets.UTF_8);
    }

    /**
     * 执行RPC请求
     */
    String callRequest(Request request) {
        logger.trace("request {}", ToJsonWrapper.wrap(request));
        try {
            return callRaw(request);
        } catch (Throwable t) {
            throw new RpcException("rpc通讯错误", t);
        }
    }

}
