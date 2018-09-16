package org.lubei.bases.core.app.action.engine;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sany Created by sany on 17-4-14.
 */
public class JSEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(JSEngine.class);

    public static Object getMethod(String content) {
        try {
            Context cx = Context.enter();
            ScriptableObject scope = cx.initStandardObjects();
            Script script = cx.compileString("", "", 1, null);
            return script.exec(cx, scope);

//            Object wrappedOut = Context.javaToJS(System.out, scope);
//            ScriptableObject.putProperty(scope, "out", wrappedOut);
        } finally {
            Context.exit();
        }
    }
}
