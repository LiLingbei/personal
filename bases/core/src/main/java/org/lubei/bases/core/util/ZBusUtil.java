package org.lubei.bases.core.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.zbus.broker.Broker;
import org.zbus.broker.BrokerConfig;
import org.zbus.broker.ha.TrackServer;
import org.zbus.mq.server.MqServer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;

/**
 * 单例模式的工具类,需要通过调用getInstance接口获取单例对象,然后才能获取公共的zbus对象
 *
 * @author sany
 */
public class ZBusUtil {

    private static final String ZBUS_CONFIG = "zbus_config.json";
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ZBusUtil.class);
    private static boolean isReady = false;
    private static ZBusUtil instance;
    private Broker broker;
    private MqServer server;
    private TrackServer trackServer;
    private String rpcMq = "myRpc";
    private int serviceThreadCount = 2;
    private int rpcTimeOut = 10000;
    private Map<String, Integer> mqMap = Maps.newHashMap();

    /**
     * 单例，防止外部创建
     */
    private ZBusUtil() {

    }

    /**
     * 单例实现
     */
    public synchronized static ZBusUtil getInstance() {
        if (instance == null) {
            instance = new ZBusUtil();
            instance.loadConfig();
        }
        return instance;
    }

    public static boolean isReady() {
        return isReady;
    }

    /**
     * zbus 单独创建MQ列表，key为MQ名称，value为接收数据的线程数
     */
    public Map<String, Integer> getMqMap() {
        return mqMap;
    }

    public int getRpcTimeOut() {
        return rpcTimeOut;
    }

    public void loadConfig() {
        JSONObject config = null;
        try {
            String configStr = ResourceUtil.getString(ZBUS_CONFIG);
            config = JSONObject.parseObject(configStr);
        } catch (Exception e) {
            LOGGER.warn("zbus init failed, 将使用默认设置:", e);
        }
        if (config == null) {
            ImmutableMap<String, ImmutableMap<String, String>> of = ImmutableMap.of(
                    "broker", ImmutableMap.of(
                            "class", "org.zbus.broker.SingleBroker",
                            "brokerAddress", "127.0.0.1:15555"
                    ),
                    "server", ImmutableMap.of(
                            "port", "15555",
                            "store", "dummy",
                            "openBrowser", "false"
                    )
            );
            config = new JSONObject();
            config.putAll(of);
            LOGGER.warn("zbus 正在使用默认设置: {}", config);
        }
        init(config);
    }

    /**
     * 按照配置文件中的对象加载zbus对象
     */
    private void init(JSONObject config) {
        try {
            try {
                initServer(config);
            } catch (Exception e) {
                LOGGER.warn("启动zbus Server 失败，如果是测试代码可以忽略", e);
            }
            if (config.containsKey("broker")) {
                initBroker(config);
            }
            if (config.containsKey("rpc")) {
                initRpc(config);
            }
            if (config.containsKey("msg")) {
                Map<String, Object> map = config.getJSONObject("msg");
                this.mqMap = Maps.transformValues(map, value -> (Integer) value);
            }
        } catch (Exception e) {
            LOGGER.error("start Server failed", e);
        }
        isReady = true;
    }


    private void initRpc(JSONObject config) {
        JSONObject rpcConfig = config.getJSONObject("rpc");
        if (rpcConfig.containsKey("rpcTimeOut")) {
            this.rpcTimeOut = rpcConfig.getInteger("rpcTimeOut");
        }
        if (rpcConfig.containsKey("serviceThreadCount")) {
            this.serviceThreadCount = rpcConfig.getInteger("serviceThreadCount");
        }
        if (rpcConfig.containsKey("rpcMq")) {
            this.rpcMq = rpcConfig.getString("rpcMq");
        }
    }

    private void initBroker(JSONObject config)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
                   IllegalAccessException, java.lang.reflect.InvocationTargetException {
        JSONObject broker = config.getJSONObject("broker");
        Class tmp = Class.forName(broker.getString("class"));
        BrokerConfig bkCfg = new BrokerConfig();
        if (broker.containsKey("trackAddrList")) {
            bkCfg.setTrackServerList(broker.getString("trackAddrList"));
        }
        if (broker.containsKey("brokerAddress")) {
            bkCfg.setServerAddress(broker.getString("brokerAddress"));
        }
        Constructor con = tmp.getConstructor(bkCfg.getClass());
        LOGGER.error("创建消息代理broker");
        this.broker = (Broker) con.newInstance(bkCfg);
    }

    private void initServer(JSONObject config) throws Exception {
        if (config.containsKey("server")) {
            JSONObject jsonObject = config.getJSONObject("server");
            String[] serverArgs = toServerArgs(jsonObject);
            LOGGER.info("serverArgs {}", serverArgs);
            MqServer.main(serverArgs);
        }
        if (config.containsKey("trackServer")) {
            JSONObject jsonObject = config.getJSONObject("trackServer");
            String[] serverArgs = toServerArgs(jsonObject);
            TrackServer.main(serverArgs);
        }
    }

    private String[] toServerArgs(JSONObject jsonObject) {
        ArrayList<String> args = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            args.add("port".equals(key) ? "-p" : "-" + key);
            args.add(String.valueOf(entry.getValue()));
        }
        return Iterables.toArray(args, String.class);
    }

    /**
     * 获取全局broker代理对象。按照配置文件确定SingleBroker还是HaBroker
     *
     * @return broker对象
     */
    public Broker getBroker() {
        return broker;
    }

    /**
     * zbus中进行rpc请求的通道名称，目前为固定配置"myRpc",后续如果存在性能问题，提供自动生成算法来实现通道分配
     */
    public String getRpcMq() {
        return this.rpcMq;
    }

    /**
     * 停止Zbus服务
     */
    public void stop() {
        try {
            this.broker.close();
            if (server != null) {
                this.server.close();
            }
            if (trackServer != null) {
                this.trackServer.close();
            }
        } catch (Exception e) {
            LOGGER.warn("stop zbus exception:", e);
        }
    }

    /**
     * zbus中 Service中处理rpc请求的线程数,默认为2
     */
    public int getServiceThreadCount() {
        return this.serviceThreadCount;
    }
}
