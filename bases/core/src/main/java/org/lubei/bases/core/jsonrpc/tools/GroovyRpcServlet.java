package org.lubei.bases.core.jsonrpc.tools;

import static com.alibaba.fastjson.serializer.SerializerFeature.DisableCircularReferenceDetect;
import static com.codahale.metrics.MetricRegistry.name;

import org.lubei.bases.core.jsonrpc.constant.Constant;
import org.lubei.bases.core.jsonrpc.engine.AppException;
import org.lubei.bases.core.jsonrpc.engine.GroovyMethodCache;
import org.lubei.bases.core.jsonrpc.engine.IMethodCache;
import org.lubei.bases.core.jsonrpc.engine.Request;
import org.lubei.bases.core.jsonrpc.engine.RequestDeserializer;
import org.lubei.bases.core.jsonrpc.engine.Response;
import org.lubei.bases.core.jsonrpc.engine.RpcException;
import org.lubei.bases.core.util.TimerBean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import groovy.lang.GroovyClassLoader;
import groovy.servlet.GroovyServlet;
import groovy.util.GroovyScriptEngine;
import io.netty.buffer.ByteBuf;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 支持JSON RPC的servlet
 *
 * 接收到http post json时，执行rpc调用，method的命名空间为rpc.json文件中映射的类名，方法名即类内的方法名
 *
 * rpc执行时，如果方法为静态方法，直接执行。否则调用无参数构造方法，然后在该实例上执行方法。
 *
 * 特殊情况: 当url中指定method时，如“/rpc?method=namespace”，传递给方法的参数为：request、response
 *
 * 用来处理文件上下传等自行处理报文的状况
 */
public class GroovyRpcServlet extends GroovyServlet {

    public static final MetricRegistry REGISTRY = new MetricRegistry();
    protected static final Logger LOGGER = LoggerFactory.getLogger(GroovyRpcServlet.class);
    /**
     * 业务异常转换类配置项名称，servlet配置该参数时，转换异常为业务异常。该类为Function<Throwable, AppException>
     */
    protected static final String APP_EXCEPTION_HANDLER = "AppExceptionHandler";
    /**
     * rpc命令名称
     */
    protected static final String RPC = "rpc.";
    /**
     * rpc命令名称，清理缓存
     */
    protected static final String RPC_CLEAN = "rpc.clean";
    /**
     * rpc命令名称，该命令为返回已缓存的所有方法
     */
    protected static final String RPC_LS = "rpc.ls";
    /**
     * rpc命令名称，打印日志，供调用方记录调试信息
     */
    protected static final String RPC_LOG = "rpc.log";
    /**
     * metric
     */
    protected static final String RPC_METRIC = "rpc.metric";
    private static final long serialVersionUID = -3999864425225572000L;
    /**
     * JSON序列化配置，先放在这里，以备以后定制序列化。（如：日期类型的序列化等）
     */
    protected final SerializeConfig mapping = new SerializeConfig();
    protected GroovyClassLoader groovyClassLoader;
    protected GroovyScriptEngine engine;
    protected GroovyMethodCache groovyMethodCache;
    Function<Throwable, AppException> appExceptionHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        initAppExceptionHandler(config);
        groovyMethodCache = new GroovyMethodCache(this.engine);
        registerDeserializer();
        LOGGER.info("RpcServlet init ok");
    }

    /**
     * 主体方法，该方法内根据调用类型分别按jsonrpc或者普通http参数两种方式处理
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        before(request);
        try {
            String uri = request.getServletPath();
            String queryString = request.getQueryString();
            LOGGER.trace("rpc call: uri[{}], query[{}]", uri, queryString);
            boolean hasMethod =
                    ((queryString != null) && queryString.contains(Constant.FIELD_METHOD + "="));
            String contentType = request.getContentType();
            if (hasMethod) {
                serviceMethod(request, response, uri, contentType);
            } else if (contentType != null && contentType.contains(Constant.JSON)) {
                serviceRpc(request, response, uri);
            } else {
                super.service(request, response);
            }
        } finally {
            after(request);
            LOGGER.debug("use time {}", stopwatch.stop());
        }
    }

    /**
     * 创建Groovy脚本引擎，此处决定脚本代码以UTF-8格式解析。Groovy默认的脚本引擎解析字符集可能与操作系统有关。
     */
    @Override
    protected GroovyScriptEngine createGroovyScriptEngine() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");
        ClassLoader classLoader = GroovyRpcServlet.class.getClassLoader();
        GroovyClassLoader cl = new GroovyClassLoader(classLoader, config);
        this.groovyClassLoader = cl;
        this.engine = new GroovyScriptEngine(this, cl);
        return this.engine;
    }

    /**
     * 在service的最开始执行。一般实现为从session中获取用户放到threadLocal中
     *
     * @param request HTTP请求
     */
    protected void before(HttpServletRequest request) {
        // place user info in a thread local, override this
    }

    /**
     * 在service的最后执行。一般实现为从threadLocal中删除变量
     *
     * @param request HTTP请求
     */
    protected void after(HttpServletRequest request) {
        // remove user info from thread local, override this
    }

    protected void registerDeserializer() {
        ParserConfig globalInstance = ParserConfig.getGlobalInstance();
        RequestDeserializer requestDeserializer =
                new RequestDeserializer(globalInstance, Request.class, groovyMethodCache);
        globalInstance.putDeserializer(Request.class, requestDeserializer);
    }

    /**
     * 初始化业务异常转换器
     */
    protected void initAppExceptionHandler(ServletConfig config) {
        String className = config.getInitParameter(APP_EXCEPTION_HANDLER);
        if (Strings.isNullOrEmpty(className)) {
            LOGGER.info("not defined {}", APP_EXCEPTION_HANDLER);
            return;
        }
        LOGGER.info("load {} from {}", APP_EXCEPTION_HANDLER, className);
        try {
            Class<?> clazz = TypeUtils.loadClass(className);
            // noinspection unchecked
            this.appExceptionHandler = (Function<Throwable, AppException>) clazz.newInstance();
        } catch (Throwable t) {
            LOGGER.error("load AppExceptionHandler fail", t);
        }
    }

    private void serviceParams(HttpServletRequest request, HttpServletResponse response,
                               String uri, String method, Object params,
                               IMethodCache.ClassMethod classMethod) {
        try {
            Object result = invoke(params, classMethod);
            if (result != null) {
                ServletUtil.writeJson(result, response);
            }
        } catch (Throwable t) {
            returnError(t, response);
        }
    }


    /**
     * 按普通的HTTP请求处理
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param uri         请求URI
     * @param contentType HTTP请求类型
     */
    protected void serviceMethod(HttpServletRequest request, HttpServletResponse response,
                                 String uri, String contentType) {
        String method = request.getParameter(Constant.FIELD_METHOD);
        if (method.startsWith(RPC)) {
            serviceMethodRpc(request, response, method);
            return;
        }

        IMethodCache.ClassMethod classMethod;
        try {
            classMethod = groovyMethodCache.get(method);
        } catch (ExecutionException e) {
            returnError(new IllegalArgumentException("方法未找到" + method), response);
            return;
        }
        Object[] realParams = new Object[]{request, response};
        serviceParams(request, response, uri, method, realParams, classMethod);
    }

    private void serviceMethodRpc(HttpServletRequest request, HttpServletResponse response,
                                  String method) {
        Response callRpc = callRpc(method, null, null, request);
        if (callRpc != null) {
            Object result = callRpc.getResult();
            if (result != null) {
                ServletUtil.writeJson(result, response);
            } else {
                ServletUtil.writeJson(callRpc, response);
            }
        }
    }

    /**
     * 按JSONRPC进行处理
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param uri      请求URI
     */
    protected void serviceRpc(HttpServletRequest request, HttpServletResponse response, String uri)
            throws IOException {
        ByteBuf byteBuf = ServletUtil.getBytes(request);
        byte firstByte = byteBuf.getByte(0);
        if (!(firstByte == '{' || firstByte == '[')) {
            response.sendError(500, JsonUtil.NOT_VALID_JSON);
            return;
        }
        String json = byteBuf.toString(Charsets.UTF_8);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("<< {}", json);
        }

        Object ret = null;
        if (firstByte == '{') {
            try {
                Request requestObject = JSON.parseObject(json, Request.class);
                LOGGER.trace("Request {}", requestObject);
                ret = callOne(request, requestObject);
            } catch (JSONException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RpcException) {
                    RpcException ex = (RpcException) cause;
                    RpcException.Builder builder =
                            RpcException.newBuilder(ex.getMessage()).id(ex.getId())
                                    .code(ex.getCode()).stack(ex.getStackTrace());
                    ret = Response.newError(builder);
                } else {
                    RpcException.Builder builder =
                            RpcException.newBuilder(Constant.MSG_PARSE_ERROR)
                                    .code(Constant.EC_METHOD_NOT_FOUND).stack(e.getStackTrace())
                                    .data(e.getMessage());
                    ret = Response.newError(builder);
                }
            }
        } else if (firstByte == '[') {
            List<Request> requests = JSON.parseArray(json, Request.class);
            ret = callArray(request, requests);
        }

        if (ret != null) {
            byte[] data =
                    JSON.toJSONBytes(ret, mapping, DisableCircularReferenceDetect);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("rpc write: {}", new String(data, Charsets.UTF_8));
            }
            ServletUtil.writeData(request, data, response);
        }
    }

    /**
     * 处理JSONRPC批请求
     *
     * @param requests 批量请求的列表
     * @return Response列表，当请求均为通知（notify）时返回null
     */
    private Object callArray(HttpServletRequest servletRequest, final List<Request> requests) {
        if (requests.size() == 0) {
            return Response.newError(null, Constant.EC_INVALID_REQUEST,
                                     Constant.MSG_INVALID_REQUEST + "empty batch");
        }
        Response[] responses = new Response[requests.size()];
        for (int i = 0, size = requests.size(); i < size; i++) {
            responses[i] = callOne(servletRequest, requests.get(i));
        }
        return responses;
    }

    /**
     * 处理单个请求，根据是否为通知返回结果
     *
     * @param req 请求对象
     * @return Response，当为通知时返回null
     */
    private Response callOne(HttpServletRequest servletRequest, Request req) {
        Object id = req.getId();
        Response resp = Request.checkOne(req, id);
        if (resp != null) {
            return resp;
        }
        resp = call(servletRequest, req, id);
        return id == null ? null : resp;
    }


    /**
     * 处理单个请求，返回结果（即使是通知也返回结果）
     *
     * @param req 请求对象
     * @param id  请求ID
     * @return Response
     */
    private Response call(HttpServletRequest servletRequest, Request req, Object id) {
        String methodName = req.getMethod();
        if (methodName.startsWith(RPC)) {
            return callRpc(methodName, req, id, servletRequest);
        }
        return callMethod(methodName, id, req);
    }

    /**
     * 调用脚本中的方法
     *
     * @param methodName 方法名，当方法名称带有“.”时，执行class（非脚本）中的方法
     */
    private Response callMethod(final String methodName, final Object id,
                                final Request req) {
        Object params = req.getParams();
        IMethodCache.ClassMethod classMethod;
        try {
            classMethod = groovyMethodCache.get(methodName);
        } catch (ExecutionException e) {
            return Response.newError(id, Constant.EC_METHOD_NOT_FOUND, "方法未找到" + methodName);
        }
        try {
            Object ret = invoke(params, classMethod);
            return new Response(id, ret);
        } catch (Throwable t) {
            return Response.exception(id, t, appExceptionHandler);
        }
    }

    public Object invoke(Object params, IMethodCache.ClassMethod classMethod) throws Throwable {
        Object ret;
        String endPoint = classMethod.getEndPoint();
        LOGGER.trace("invoke start {}", endPoint);

        final Timer timer = REGISTRY.timer(name("jsonrpc", endPoint));
        final Timer.Context context = timer.time();

        try {
            Object[] parameters = InvokerHelper.asArray(params);
            ret = classMethod.getFunction().apply(parameters);
            LOGGER.trace("invoke end {}", endPoint);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                LOGGER.warn("InvocationTargetException ", cause);
                throw cause;
            }
            throw e;
        } finally {
            context.stop();
        }
        return ret;
    }


    /**
     * 执行以“rpc.”开始的系统保留方法
     *
     * @param method 方法名
     * @param id     调用ID  @return 执行结果
     */
    private Response callRpc(final String method, Request req, final Object id,
                             HttpServletRequest servletRequest) {
        switch (method) {
            case RPC_CLEAN:
                try {
                    this.groovyMethodCache.clean();
                } catch (Throwable e) {
                    return Response.newError(null, Constant.EC_INTERNAL_ERROR,
                                             "rpc: 清理失败", e);
                }
                return new Response(id, true);
            case RPC_LS:
                return new Response(id, this.groovyMethodCache.lss());
            case RPC_LOG:
                return ClientLog.rpcLog(req, servletRequest);
            case RPC_METRIC:
                return rpcMetric(id);
            default:
                return Response.newError(id, Constant.EC_METHOD_NOT_FOUND, "Method not found");
        }
    }

    Response rpcMetric(Object id) {
        SortedMap<String, Timer> meters = REGISTRY.getTimers();
        SortedMap<String, TimerBean> map = Maps.transformValues(meters, TimerBean.Companion::build);
        return new Response(id, map);
    }

    private void returnError(Throwable throwable, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        RpcException exception = Response.exception(throwable, appExceptionHandler);
        ServletUtil.writeJson(exception, response);
    }
}
