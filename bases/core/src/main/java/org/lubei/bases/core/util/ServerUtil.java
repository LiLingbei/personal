package org.lubei.bases.core.util;/**
 * Created by sany on 16-10-12.
 */

import org.lubei.bases.core.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.UUID;

public enum ServerUtil {
    INSTANCE;
    private final String SERVER_ID = "ServerId";
    private final String SERVER_NAME = "ServerName";
    private final String TMP_PREFIX = "临时";
    private final String PID_PROPERTIES = "pid.properties";
    private final String PID_LOCK = PID_PROPERTIES + ".lock";
    private final Logger logger = LoggerFactory.getLogger(ServerUtil.class);
    private Server server;

    ServerUtil() {
        server = initServerInfo();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> _destroy()));
    }


    /**
     * 获取服务器ipv4的Ip地址“服务单”详情界面中，对服务单的响应时长和完成时长需要有一个清晰易读的展现形式。设计效果如下图所示：
     */
    private String getFirstIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private void _destroy() {
        if (!server.getId().startsWith(TMP_PREFIX)) {
            new File(PID_LOCK).deleteOnExit();
        }
    }

    public Server getServer() {
        return server;
    }


    private Server initServerInfo() {
        // 自动生成ServerId和ServerName
        String serverId = UUID.randomUUID().toString();
        String serverName = getFirstIP();
        if (!new File(PID_PROPERTIES).exists()) {
            saveServerToFile(serverId, serverName);
            return newServer(serverId, serverName);
        }

        if (!new File(PID_LOCK).exists()) {
            Properties properties = null;
            try {
                properties = ResourceUtil.getProperties(PID_PROPERTIES);
                serverId = properties.getProperty(SERVER_ID);
                serverName = properties.getProperty(SERVER_NAME);
                logger.debug("服务器id为 {},服务名为 {}", serverId, serverName);
            } catch (IOException e) {
                logger.warn("文件应该存在但是不存在了", e);
            }

            try {
                ResourceUtil.overWriteToFile(PID_LOCK, new Properties());
            } catch (IOException e) {
                logger.warn("写入lock失败", e);
            }
            return newServer(serverId, serverName);
        }

        logger.debug("pid文件已经被其他进程使用，重新生成");
        return newServer(TMP_PREFIX + serverId, serverName);

    }

    private void saveServerToFile(String serverId, String serverName) {
        Properties properties;
        properties = new Properties();
        properties.setProperty(SERVER_ID, serverId);
        properties.setProperty(SERVER_NAME, serverName);
        logger.debug("服务器id为 {},服务名为 {}", serverId, serverName);
        //将ServerId和ServerName存入文件，下次启动自动沿用
        try {
            ResourceUtil.overWriteToFile(PID_PROPERTIES, properties);
            logger.debug("服务Server信息持久化到配置文件{}", PID_PROPERTIES);
        } catch (Exception e) {
            logger.error("写入进程ID文件{}失败", PID_PROPERTIES, e);
        }
    }

    private Server newServer(String serverId, String serverName) {
        Server server = new Server();
        server.setId(serverId);
        server.setName(serverName);
        return server;
    }
}
