package org.lubei.bases.core.jsonrpc.tools;

import org.lubei.bases.core.jsonrpc.engine.Request;
import org.lubei.bases.core.jsonrpc.engine.Response;

import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 打印日志，供调用方记录调试信息
 */
public class ClientLog {

    static final Logger LOGGER = LoggerFactory.getLogger(ClientLog.class);

    static Response rpcLog(Request request, HttpServletRequest servletRequest) {
        if (!LOGGER.isTraceEnabled()) {
            return null;
        }
        Object message;
        if (request != null) {
            message = request.getParams();
        } else {
            message = servletRequest.getQueryString();
        }

        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);
        String ip = ServletUtil.getIp(servletRequest);
        LOGGER.trace("ip:{}, userAgent:{}, {}", ip, userAgent, message);
        return null;
    }

}
