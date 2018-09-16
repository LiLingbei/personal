package org.lubei.bases.core.jsonrpc.tools;


import org.lubei.bases.core.jsonrpc.constant.Constant;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet工具类
 */
public class ServletUtil {

    private static final Logger logger = LoggerFactory.getLogger(GroovyRpcServlet.class);

    private ServletUtil() {
        throw new IllegalAccessError("不能实例化");
    }

    /**
     * 获取HTTP请求的实际地址，包括在跨代理调用时
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 读取HTTP请求的报文（如果为gzip则解压）
     *
     * @param request HTTP请求
     * @return HTTP报文（如果为gzip时，为解压后的报文体）
     */
    public static ByteBuf getBytes(HttpServletRequest request) {
        String cen = request.getHeader(Constant.CONTENT_ENCODING);
        boolean gzip = (cen != null) && (cen.contains(Constant.GZIP));
        try {
            InputStream inputStream = request.getInputStream();
            return getBytes(gzip ? new GZIPInputStream(inputStream) : inputStream);
        } catch (IOException e) {
            throw new IllegalAccessError("read json fail:" + e.getMessage());
        }
    }

    private static ByteBuf getBytes(InputStream is) throws IOException {
        ByteBuf byteBuf = ByteBufUtil.threadLocalDirectBuffer();
        byte[] byteBuffer = new byte[512];
        int nbByteRead;
        try {
            while ((nbByteRead = is.read(byteBuffer)) != -1) {
                // appends buffer
                byteBuf.writeBytes(byteBuffer, 0, nbByteRead);
            }
        } finally {
            closeWithWarning(is);
        }
        return byteBuf;
    }

    private static void closeWithWarning(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // logger.warn("Caught exception during close(): ", e);
            }
        }
    }

    /**
     * 写数据到HTTP Response，如果支持压缩并且数据量大于256字节时将压缩传输
     *
     * @param request  HTTP请求
     * @param data     发送数据
     * @param response HTTP响应
     */
    public static void writeData(HttpServletRequest request, byte[] data,
                                 HttpServletResponse response) throws IOException {
        if (data == null) {
            return;
        }
        OutputStream stream = response.getOutputStream();
        response.setContentType(Constant.DEFAULT_CONTENT_TYPE);
        String acceptEncoding = request.getHeader(Constant.ACCEPT_ENCODING);
        boolean acceptGzip = (acceptEncoding != null && acceptEncoding.contains(Constant.GZIP));
        if (acceptGzip && (data.length > Constant.MIN_GZIP_LEN)) {
            response.addHeader(Constant.CONTENT_ENCODING, Constant.GZIP);
            GZIPOutputStream st = new GZIPOutputStream(stream);
            st.write(data);
            st.finish();
            st.close();
        } else {
            stream.write(data);
        }
    }

    /**
     * 以JSON格式写对象到HTTP响应中
     *
     * @param data     发送的对象
     * @param response HTTP响应
     */
    public static void writeJson(Object data, HttpServletResponse response) {
        try {
            OutputStream stream = response.getOutputStream();
            response.setContentType(Constant.DEFAULT_CONTENT_TYPE);
            stream.write(JSON.toJSONBytes(data));
        } catch (IOException e) {
            logger.warn("writeJson fail:", e);
        }
    }
}
