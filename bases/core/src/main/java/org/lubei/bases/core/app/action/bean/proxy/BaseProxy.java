package org.lubei.bases.core.app.action.bean.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sany Created by sany on 17-4-26.
 */
public abstract class BaseProxy {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseProxy.class);

    public abstract Object call(String method, Object... args);

    public abstract Class[] getArgumentTypes(String method);
}
