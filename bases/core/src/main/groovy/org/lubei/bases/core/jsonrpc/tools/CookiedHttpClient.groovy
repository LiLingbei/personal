package org.lubei.bases.core.jsonrpc.tools

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import wslite.http.HTTPClient
import wslite.http.HTTPClientException
import wslite.http.HTTPRequest
import wslite.http.HTTPResponse

/**
 * 带cookie的http客户端
 */

public class CookiedHttpClient extends HTTPClient {

    static final Logger LOOGER = LoggerFactory.getLogger(CookiedHttpClient.class)

    String cookie
    def cookies = [:]

    HTTPResponse execute(HTTPRequest request) {
        if (!(request?.url && request?.method)) {
            throw new IllegalArgumentException('HTTP Request must contain a url and method')
        }
        HTTPResponse response
        def conn
        try {
            conn = super.createConnection(request)
            super.setupConnection(conn, request)
            response = buildResponseWithCookie(conn, conn.inputStream)
        } catch (Exception ex) {
            if (!conn) {
                throw new HTTPClientException(ex.message, ex, request, response)
            } else {
                response = super.buildResponse(conn, conn.errorStream)
                throw new HTTPClientException(response.statusCode + ' ' + response.statusMessage,
                                              ex, request, response)
            }
        } finally {
            conn?.disconnect()
        }
        return response
    }

    private HTTPResponse buildResponseWithCookie(conn, responseStream) {
        def response = super.buildResponse(conn, responseStream);
        List<String> cookieField = conn.headerFields.'Set-Cookie'
        if (cookieField) {
            LOOGER.debug("cookieField {}", cookieField)
            boolean changed = false
            cookieField.each {
                int end = it.indexOf(';')
                int sp = it.indexOf('=')
                String key = it.substring(0, sp)
                String value = it.substring(sp + 1, end)
                if (value != "deleteMe" && cookies.get(key) != value) {
                    cookies.put(key, value)
                    changed = true
                }
            }
            if (changed) {
                String newCookie = cookies.collect { "${it.key}=${it.value}" }.join('; ')
                LOOGER.debug("cookie {}", newCookie)
                this.defaultHeaders.put('Cookie', newCookie)
            }
        }
        return response
    }
}

