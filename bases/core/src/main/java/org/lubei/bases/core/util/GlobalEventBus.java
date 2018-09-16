package org.lubei.bases.core.util;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * 消息总线
 *
 * 异步内存消息总线，所有消息在同一个异步线程内进行分发
 */
public enum GlobalEventBus {
    INSTANCE;

    private final String ID = GlobalEventBus.class.getSimpleName();

    private final EventBus eventBus;

    GlobalEventBus() {
        eventBus = new AsyncEventBus(ID, DaemonExecutors.newSingleThreadExecutor(ID));
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    public void register(Object object) {
        eventBus.register(object);
    }

    public void unregister(Object object) {
        eventBus.unregister(object);
    }

    public EventBus getBus() {
        return eventBus;
    }

}
