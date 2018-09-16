package org.lubei.bases.core.rpc.zbus;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.lubei.bases.core.Server;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zbus.broker.Broker;
import org.zbus.mq.Consumer;
import org.zbus.mq.Producer;
import org.zbus.mq.Protocol;
import org.zbus.net.core.Session;
import org.zbus.net.http.Message;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by postgres on 2015/10/30.
 */
public class PingPong extends AbstractScheduledService {

    public static final String TOPIC_PING = "ping";
    public static final String TOPIC_PONG = "pong";
    public static final String SERVER_ID = "serverid";
    public static final String SERVICES = "services";
    public static final String SERVICE = "service";
    public static final String SERVER_NAME = "servername";
    private static final Logger LOGGER = LoggerFactory.getLogger(PingPong.class);
    private final Broker broker;
    private final String mq;
    private final ConcurrentMap<String, WaitBean> serviceMap;
    private final Multimap<Server, String> serverServiceMap;
    private final Set<String> localService;
    private final String serverId;
    private Server server;
    private Producer pong;
    private Producer ping;
    private volatile long lastPongTime;
    private volatile long lastNotifyPongTime;
    private volatile boolean first = true;
    private Object waitServerBean = new Object();

    public PingPong(Broker broker, String mq, Set<String> localService, Server server) {
        this.localService = localService;
        this.server = server;
        this.serverId = server.getId();
        this.broker = broker;
        this.mq = mq;
        this.serviceMap = Maps.newConcurrentMap();
        serverServiceMap = ArrayListMultimap.create();
        try {
            initPingPong();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    public String findServer(String service) {
        WaitBean waitBean = serviceMap.computeIfAbsent(service, (String key) -> new WaitBean());
        String remoteServerId = waitBean.getServerId();
        if (!Strings.isNullOrEmpty(remoteServerId)) {
            return remoteServerId;
        }
        synchronized (waitBean) {
            // double check
            remoteServerId = waitBean.getServerId();
            if (!Strings.isNullOrEmpty(remoteServerId)) {
                return remoteServerId;
            }
            sendPing(service);
            try {
                waitBean.wait(5000L);
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
            remoteServerId = waitBean.getServerId();
            Preconditions.checkState(!Strings.isNullOrEmpty(remoteServerId), "没有找到目标");
            return remoteServerId;
        }
    }

    public List<Server> findServers(String service) {
        List<Server> ret = Lists.newArrayList();
        sendPing(service);
        if (first) {
            //第一次默认等5秒
            synchronized (waitServerBean) {
                if (first) {
                    try {
                        waitServerBean.wait(5000L);
                    } catch (InterruptedException e) {
                        Throwables.propagate(e);
                    }
                    waitServerBean.notifyAll();
                    first = false;
                }
            }
        }
        synchronized (serverServiceMap) {
            for (Map.Entry<Server, String> entry : serverServiceMap.entries()) {
                if (entry.getValue().equals(service)) {
                    ret.add(entry.getKey());
                }
            }
        }
        if (this.localService.contains(service)) {
            ret.add(server);
        }
        //默认ping一下，保证下次缓存更新成功
        sendPing(service);

        return ret;
    }

    public void sendPing(String service) {
        Message response = new Message();
        response.setTopic(TOPIC_PING);
        response.setHead(SERVICE, service);
        try {
            ping.sendAsync(response);
        } catch (IOException e) {
            LOGGER.warn("pong fail", e);
        }
    }


    /**
     * 通知进行pong操作，延迟一段时间后执行
     */
    public void notifyPong() {
        this.lastNotifyPongTime = System.currentTimeMillis();
    }

    public void sendPong() {
        this.lastNotifyPongTime = System.currentTimeMillis();
        Message response = new Message();
        response.setTopic(TOPIC_PONG);
        response.setHead(SERVER_ID, server.getId());
        response.setHead(SERVER_NAME, server.getName());
        response.setHead(SERVICES, Joiner.on(',').join(localService));
        try {
            pong.sendAsync(response);
        } catch (IOException e) {
            LOGGER.warn("pong fail", e);
        }
    }

    private void initPingPong() throws IOException, InterruptedException {
        ping = new Producer(broker, mq, Protocol.MqMode.PubSub);
        ping.createMQ();
        pong = new Producer(broker, mq, Protocol.MqMode.PubSub);
        pong.createMQ();

        Consumer pingConsumer = new Consumer(broker, mq, Protocol.MqMode.PubSub);
        pingConsumer.setTopic(TOPIC_PING);
        pingConsumer.onMessage(this::handlePing);
        pingConsumer.start();
        Consumer pongConsumer = new Consumer(broker, mq, Protocol.MqMode.PubSub);
        pongConsumer.setTopic(TOPIC_PONG);
        pongConsumer.onMessage(this::handlePong);
        pongConsumer.start();
    }

    public void handlePing(Message msg, Session sess) throws IOException {
        String service = msg.getHead(SERVICE);
        LOGGER.debug("handlePing, service:{}, msg: {}", service, msg);
        if (localService.contains(service)) {
            sendPong();
        }
    }

    public void handlePong(Message msg, Session sess) throws IOException {
        String remoteServerId = msg.getHead(SERVER_ID);
        String remoteServerName = msg.getHead(SERVER_NAME);
        if (Objects.equals(remoteServerId, serverId)) {
            return;
        }
        LOGGER.debug("before pong: {}", serviceMap);
        String remoteServices = msg.getHead(SERVICES);
        LOGGER.debug("handlePong remoteServerId:{}, remoteServices:{}, msg:{}", remoteServerId,
                     remoteServices, msg);
        Iterable<String> remoteServiceNames = Splitter.on(',').split(remoteServices);
        synchronized (serverServiceMap) {
            Server server = new Server();
            server.setId(remoteServerId);
            server.setName(remoteServerName);
            //更新serverid对应的服务名称
            serverServiceMap.removeAll(server);
            serverServiceMap.putAll(server, remoteServiceNames);
        }
        for (String name : remoteServiceNames) {
            WaitBean old =
                    serviceMap.computeIfAbsent(name, (String key) -> new WaitBean());
            if (!Objects.equals(old.getServerId(), remoteServerId)) {
                old.setServerId(remoteServerId);
            }

        }

        LOGGER.debug("after pong: {}", serviceMap);
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (lastPongTime > lastNotifyPongTime) {
            return;
        }
        sendPong();
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(5, 5, SECONDS);
    }

    class WaitBean {

        String serverId;

        public String getServerId() {
            return serverId;
        }

        public void setServerId(String serverId) {
            this.serverId = serverId;
            synchronized (this) {
                this.notifyAll();
            }
        }

        @Override
        public String toString() {
            return "WaitBean{" + this.hashCode() +
                   ", serverId='" + serverId + '\'' +
                   '}';
        }
    }
}
