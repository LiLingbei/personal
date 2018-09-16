package org.lubei.bases.core.jsonrpc.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.any;
import static org.lubei.bases.core.jsonrpc.constant.Constant.NAME_SERVICE;

import org.lubei.bases.core.annotation.BoolType;
import org.lubei.bases.core.GlobalRes;
import org.lubei.bases.core.app.action.GlobalContext;
import org.lubei.bases.core.app.action.bean.proxy.BaseProxy;
import org.lubei.bases.core.jsonrpc.constant.Constant;
import org.lubei.bases.core.msg.MsgBaseEvent;
import org.lubei.bases.core.plugin.IPluginService;
import org.lubei.bases.core.service.ServicesFactory;
import org.lubei.bases.core.util.ToJsonWrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * 基于类的方法查找缓存
 *
 * @author liwenheng@ruijie.com.cn
 */
public class ClassMethodCache implements IMethodCache {

    public static final int CACHE_SIZE = 256;
    static final String NAME = "rpc.json";
    static final Logger LOGGER = LoggerFactory.getLogger(RequestDeserializer.class);
    private static final String STRING_SPLIT = "/";
    LoadingCache<String, ClassMethod> cache;
    Map<String, String> pathToClazz;
    Map<String, AuditConf> auditConfMap;
    Map<String, String> pathToPlugin;

    /**
     * 构造方法
     */
    public ClassMethodCache() {
        CacheLoader<String, ClassMethod> loader = new CacheLoader<String, ClassMethod>() {
            @Override
            public ClassMethod load(String key) throws Exception {
                return findMethod(key);
            }
        };
        this.cache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(loader);

        loadRpcPaths();  // 加载RPC路径（方法）与类名关系，从各jar包下的rpc.json

        new Thread(this::refresh, "RPC-Method-Cache").start();  // 单线程异步监听插件加载，加载RPC路径关系
    }

    /**
     * 加载类
     *
     * 如果提供插件ID将尝试从插件加载类
     *
     * @param className 类名
     * @param pluginId  插件ID
     * @return 类
     */
    public static synchronized Class<?> loadClass(String className, String pluginId) {
        checkArgument(!isNullOrEmpty(className));

        try {
            Class<?> loadedClass = tryLoadClass(className, pluginId);
            LOGGER.trace("loadClass {}/{}:{}", className, pluginId, loadedClass.getClassLoader());
            return loadedClass;
        } catch (Throwable e) {
            LOGGER.warn("LoadClass fail: className {} pluginId {}", className, pluginId, e);
            return null;
        }
    }

    @Nullable
    private static Class<?> tryLoadClass(String className, String pluginId)
            throws ClassNotFoundException {
        if (pluginId != null) { // 从插件的classLoader加载
            IPluginService service = ServicesFactory.getService(IPluginService.class);
            return service.getPlugin(pluginId).getPluginClassLoader().loadClass(className);
        }

        return getClassLoader().loadClass(className);
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return ClassMethodCache.class.getClassLoader();
    }

    @Override
    public ClassMethod get(String key) throws ExecutionException {
        return cache.get(key);
    }

    @Override
    public void clean() {
        this.cache.invalidateAll();
        this.cache.cleanUp();
    }

    @Override
    public CacheStats getStats() {
        return this.cache.stats();
    }

    @Override
    public ConcurrentMap<String, ClassMethod> ls() {
        return this.cache.asMap();
    }

    private void refresh() {
        LOGGER.info("插件rpc.json跟踪线程启动");
        BlockingQueue queue = GlobalRes.getQueue(Constant.RPC_CONTROL_QUEUE);
        while (true) {
            try {
                MsgBaseEvent event = (MsgBaseEvent) queue.take();
                if (event == null) {
                    continue;
                }
                LOGGER.info("插件rpc.json消息:{}", ToJsonWrapper.wrap(event));
                onRpcMsgHandle(event);
            } catch (Throwable e) {
                LOGGER.warn("发生预料外异常:", e);
            }
        }
    }

    private void onRpcMsgHandle(MsgBaseEvent event) {
        Map<String, String> data = (Map) event.getData();

        String eventType = event.getType();
        switch (eventType) {
            case Constant.RPC_MSG_REG:
                addRpcMapper(data);
                break;
            case Constant.RPC_MSG_UNREG:
                removeRpcMapper(data);
                break;
            default:
                LOGGER.warn("未知的事件类型: {}", eventType);
        }
    }

    public Map<String, String> lss() {
        ConcurrentMap<String, ClassMethod> map = this.ls();
        Map<String, String> mm = Maps.newTreeMap(); // TreeMap 是经过排序的
        for (Map.Entry<String, ClassMethod> entry : map.entrySet()) {
            String key = entry.getKey();
            int indexOf = key.lastIndexOf('/');
            String prefix = key.substring(0, indexOf);
            IMethodCache.ClassMethod value = entry.getValue();
            Class clazz = value.getClazz();
            String name = (clazz == null) ? "" : clazz + " " + clazz.getClassLoader();
            mm.put(prefix, name);
        }
        return mm;
    }

    private void loadRpcPaths() {
        this.pathToClazz = Maps.newConcurrentMap();
        this.auditConfMap = Maps.newConcurrentMap();
        this.pathToPlugin = Maps.newConcurrentMap();

        ClassLoader classLoader = getClassLoader();

        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(NAME);
        } catch (IOException e) {
            LOGGER.warn("getResources fail {}", NAME, e);
            return;
        }
        List<URL> urls = Lists.newArrayList();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            urls.add(url);
            try {
                addRpcFile(url);
            } catch (IOException e) {
                LOGGER.warn("addRpcFile fail url:{}", url, e);
            }
        }
        LOGGER.debug("addRpcFile {}", urls);
    }

    private void addRpcFile(URL url) throws IOException {
        String json = Resources.toString(url, Charsets.UTF_8);
        addRpcMapper(json, null);
    }

    private void removeRpcMapper(Map<String, String> data) {
        Set<String> pluginIdSet = data.keySet();

        Set<String> pathToRemove = Maps.filterEntries(
                pathToPlugin, it -> pluginIdSet.contains(it.getValue())
        ).keySet();

        Predicate<String> startWithRemove = it -> any(pathToRemove, path -> it.startsWith(path));

        cache.invalidateAll(Sets.filter(cache.asMap().keySet(), startWithRemove));
        auditConfMap.entrySet().removeIf(it -> startWithRemove.apply(it.getKey()));
        pathToPlugin.entrySet().removeIf(it -> pathToRemove.contains(it.getKey()));
        pathToClazz.entrySet().removeIf(it -> pathToRemove.contains(it.getKey()));
    }

    private void addRpcMapper(Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String pluginId = entry.getKey();
            String content = entry.getValue();
            addRpcMapper(content, pluginId);
        }
    }

    private void addRpcMapper(String content, String pluginId) {
        JSONObject jsonObject = JSON.parseObject(content);
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            Object entryValue = entry.getValue();
            String key = entry.getKey();
            if (pluginId != null) {
                this.pathToPlugin.put(key, pluginId);
            }
            if (entryValue instanceof String) {
                String value = (String) entryValue;
                String oldValue = this.pathToClazz.put(key, value);
                if (oldValue != null) {
                    if (oldValue.equals(value)) {
                        LOGGER.info("rpc重复定义，key {} : {} -> {} {}",
                                    key, oldValue, value, pluginId);
                    } else {
                        LOGGER.warn("rpc定义冲突，key {} : {} -> {} {}",
                                    key, oldValue, value, pluginId);
                    }
                }
            } else if (entryValue instanceof JSONObject) {
                JSONObject object = (JSONObject) entryValue;
                String clazz = object.getString(AuditConf.FIELD_CLAZZ);
                if (!isNullOrEmpty(clazz)) {
                    this.pathToClazz.put(key, clazz);
                    parseMethods(key, object);
                } else {
                    LOGGER.warn("格式不正确, 没有提供clazz, %s", entryValue);
                }
            } else {
                LOGGER.warn("格式不正确 {}", entryValue);
            }
        }
    }

    private void parseMethods(String key, JSONObject object) {
        JSONArray methods = object.getJSONArray(AuditConf.FIELD_METHODS);
        if (methods == null) {
            return;
        }
        String clazzModule = object.getString(AuditConf.FIELD_MODULE);
        BoolType clazzNoLogin = BoolType.from(object.getBoolean(AuditConf.FIELD_NO_LOGIN));
        for (Object method : methods) {
            if (method instanceof JSONObject) {
                try {
                    parseMethod(key, clazzModule, clazzNoLogin, method);
                } catch (Throwable t) {
                    LOGGER.warn("parseMethods fail, {}", object, t);
                }
            }
        }
    }

    private void parseMethod(String key, String clazzModule, BoolType clazzNoLogin, Object method) {
        JSONObject methodObject = (JSONObject) method;
        String methodName = methodObject.getString(AuditConf.FIELD_METHOD);
        String name = methodObject.getString(AuditConf.FIELD_NAME);
        checkArgument(!isNullOrEmpty(methodName), "method为空，%s", method);
        String module = methodObject.getString(AuditConf.FIELD_MODULE);
        if (Strings.isNullOrEmpty(module)) {
            module = clazzModule;
        }
        String type = methodObject.getString(AuditConf.FIELD_TYPE);
        String request = methodObject.getString(AuditConf.FIELD_REQUEST);
        String response = methodObject.getString(AuditConf.FIELD_RESPONSE);
        Boolean noLogin = methodObject.getBoolean(AuditConf.FIELD_NO_LOGIN);
        AuditConf auditConf = new AuditConf(module, type, name, request, response);
        if (noLogin == null) {
            auditConf.noLogin = clazzNoLogin;
        } else {
            auditConf.noLogin = BoolType.from(noLogin);
        }

        auditConfMap.put(key + '/' + methodName, auditConf);
    }

    ClassMethod findMethod(String endPoint) {
        if (endPoint.startsWith(NAME_SERVICE)) {
            return getRegisterMethod(endPoint);
        }
        int lastIndexOf = endPoint.lastIndexOf('/');
        if (lastIndexOf == -1) {
            throw new RpcException.Builder("方法未找到").code(Constant.EC_METHOD_NOT_FOUND).build();
        }
        String className = endPoint.substring(0, lastIndexOf);
        String classNameMapped = pathToClazz.get(className);
        String pluginId = pathToPlugin.getOrDefault(className, null);
        if (classNameMapped != null) {
            className = classNameMapped;
        }
        String methodName = endPoint.substring(lastIndexOf + 1, endPoint.length());
        Class<?> clazz = loadClass(className, pluginId);
        checkNotNull(clazz, "没有对应的类%s", className);
        ClassMethod classMethod = getClassMethod(endPoint, methodName, clazz);
        checkNotNull(classMethod, "方法未找到%s", endPoint);

        return classMethod;
    }

    private ClassMethod getRegisterMethod(String endPoint) {
        endPoint = endPoint.substring(NAME_SERVICE.length(), endPoint.length());
        String key = endPoint.substring(0, endPoint.lastIndexOf(STRING_SPLIT));
        String methodName = endPoint.substring(endPoint.lastIndexOf(STRING_SPLIT) + 1,
                                               endPoint.length());
        BaseProxy bean = GlobalContext.INSTANCE.getBean(key);
        IMethodCache.ClassMethod method = new IMethodCache.ClassMethod();
        method.setEndPoint(endPoint);
        method.setMethodName(methodName);
        method.setParameterTypes(bean.getArgumentTypes(methodName));
        method.setFunction(params -> bean.call(methodName, params));
        return method;
    }

    ClassMethod getClassMethod(String endPint, String methodName, Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (Objects.equals(method.getName(), methodName)) {
                AuditConf auditConf = auditConfMap.get(endPint);
                boolean isStatic = Modifier.isStatic(method.getModifiers());
                ClassMethod classMethod = new ClassMethod(auditConf, endPint, clazz, method);
                classMethod.setFunction(params -> {
                    if (isStatic) {
                        return method.invoke(null, params);
                    } else {
                        Object instance = clazz.newInstance();
                        return method.invoke(instance, params);
                    }
                });
                return classMethod;
            }
        }
        return null;
    }
}
