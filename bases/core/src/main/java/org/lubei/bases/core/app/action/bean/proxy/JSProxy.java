package org.lubei.bases.core.app.action.bean.proxy;

import com.alibaba.fastjson.JSON;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.json.JsonParser;

/**
 * @author sany Created by sany on 17-4-26.
 */
public class JSProxy extends BaseProxy {

    ScriptableObject scope;
    Context cx;
    JsonParser parser;

    public JSProxy(ScriptableObject scope, Context cx) {
        this.cx = cx;
        this.scope = scope;
        parser = new JsonParser(cx, scope);
    }

    public Object call(String method, Object... args) {
        Context ctx = Context.enter();
        try {
            int length = args.length;
            for (int i = 0; i < length; i++) {
                if (!ScriptRuntime.isPrimitive(args[i])) {
                    args[i] = parser.parseValue(JSON.toJSONString(args[i]));
                }
            }
//        Object[] o = (Object[]) Context.javaToJS(args, scope);
            return ((NativeJavaObject) this.scope.callMethod(scope, method, args)).unwrap();
        } catch (JsonParser.ParseException e) {
            e.printStackTrace();
            throw new IllegalStateException("输入参数不合法", e);
        } finally {
            Context.exit();
        }
    }


    @Override
    public Class[] getArgumentTypes(String method) {
        Scriptable instance = (Scriptable) scope.get(method);
        // 参数个数
        Object length = instance.get("length", instance);
        int len = Integer.parseInt(length.toString());
        Class[] classes = new Class[len];
        for (int i = 0; i < len; i++) {
            // NativeObject 在带有参数的js方法中不可执行,JSONObject只适用于Json格式
            classes[i] = Object.class;
        }
        return classes;
    }
}
