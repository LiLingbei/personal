package org.lubei.bases.core.jsonrpc.engine;

import org.lubei.bases.core.jsonrpc.constant.Constant;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.util.FieldInfo;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 请求对象反序列化
 */
public class RequestDeserializer extends JavaBeanDeserializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDeserializer.class);

    IMethodCache methodCache;

    public RequestDeserializer(ParserConfig config, Class<?> clazz, IMethodCache methodCache) {
        super(config, clazz);
        this.methodCache = methodCache;
        replaceParamsDeserializer();
    }

    private void replaceParamsDeserializer() {
        // JavaBeanDeserializer 包含两个反序列化数组 sortedFieldDeserializers（启用）和fieldDeserializers
        FieldDeserializer[] effect = this.sortedFieldDeserializers;
        int paramsPos = 0;
        boolean find = false;
        for (; paramsPos < effect.length; paramsPos++) {
            if (effect[paramsPos].fieldInfo.name.equals(Constant.FIELD_PARAMS)) {
                find = true;
                break;
            }
        }
        Preconditions.checkState(find, "自定义RequestDeserializer失败，未找到对应的字段反解析");
        FieldDeserializer old = effect[paramsPos];
        effect[paramsPos] = new ParamsDeserializer(Request.class, old.fieldInfo);
    }

    class ParamsDeserializer extends FieldDeserializer {

        public ParamsDeserializer(Class<?> clazz, FieldInfo fieldInfo) {
            super(clazz, fieldInfo);
        }

        @Override
        public void parseField(DefaultJSONParser parser, Object object, Type objectType,
                               Map<String, Object> fieldValues) {
            JSONLexer lexer = parser.getLexer();
            if (lexer.token() == JSONToken.NULL) {
                return;
            }

            if (lexer.token() != JSONToken.LBRACKET) {
                throw new JSONException(
                        "syntax error, should be array! unexpected token "
                        + JSONToken.name(lexer.token()));
            }
            Request request = (Request) object;
            String method = Preconditions.checkNotNull(request.getMethod());
            if (method.startsWith(Constant.METHOD_RPC)) {
                List<Object> objects = parser.parseArray(Object.class);
                request.setParams(objects);
                return;
            }

            IMethodCache.ClassMethod classMethod = null;
            try {
                classMethod = methodCache.get(method);
            } catch (UncheckedExecutionException | ExecutionException e) {
                throw new RpcException.Builder("方法未找到:" + method)
                        .id(request.getId())
                        .code(Constant.EC_METHOD_NOT_FOUND).build();
            }
            Type[] parameterTypes = classMethod.getParameterTypes();
            if (parameterTypes.length > 0) {
                Object[] objects = parser.parseArray(parameterTypes);
                request.setParams(objects);
                return;
            } else {
                lexer.skipWhitespace();
                lexer.nextToken(JSONToken.RBRACKET);
                return;
            }
        }
    }
}
