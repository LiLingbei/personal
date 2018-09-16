package org.lubei.bases.core.jsonrpc.engine;

import org.lubei.bases.core.annotation.AuditType;
import org.lubei.bases.core.annotation.BoolType;

import com.google.common.base.Enums;
import com.google.common.base.MoreObjects;

/**
 * 审计配置
 */
public class AuditConf {

    static final String FIELD_MODULE = "module";
    static final String FIELD_CLAZZ = "clazz";
    static final String FIELD_METHODS = "methods";
    static final String FIELD_METHOD = "method";
    static final String FIELD_TYPE = "type";
    static final String FIELD_NAME = "name";
    static final String FIELD_NO_LOGIN = "noLogin";
    static final String FIELD_REQUEST = "request";
    static final String FIELD_RESPONSE = "response";

    /**
     * 模块
     *
     * @return 模块名称
     */
    String module;

    /**
     * 类型
     *
     * @return 类型
     */
    AuditType type;

    /**
     * 操作名称
     *
     * @return 操作名称
     */
    String name;

    /**
     * 请求信息表达式（JXLT）
     *
     * @return 请求信息表达式
     */
    String request;

    /**
     * 响应信息表达式（JXLT）
     *
     * @return 响应信息表达式
     */
    String response;

    /**
     * 不需要登录即可调用
     */
    BoolType noLogin = BoolType.NONE;

    public AuditConf() {
    }

    public AuditConf(String module, String type, String name, String request,
                     String response) {
        AuditType auditType =
                (type == null) ? AuditType.NONE
                               : Enums.getIfPresent(AuditType.class, type).or(AuditType.NONE);
        this.module = module;
        this.type = auditType;
        this.name = name;
        this.request = request;
        this.response = response;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("module", module)
                .add("type", type)
                .add("name", name)
                .add("request", request)
                .add("response", response)
                .add("noLogin", noLogin)
                .toString();
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public AuditType getType() {
        return type;
    }

    public void setType(AuditType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public BoolType getNoLogin() {
        return noLogin;
    }

    public void setNoLogin(BoolType noLogin) {
        this.noLogin = noLogin;
    }

}
