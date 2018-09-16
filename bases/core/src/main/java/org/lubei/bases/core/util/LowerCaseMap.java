package org.lubei.bases.core.util;

import java.util.HashMap;

public class LowerCaseMap<V> extends HashMap<String, V> {
    private static final long serialVersionUID = 1L;

    @Override
    public V get(final Object key) {
        return super.get(toLower(key));
    }

    @Override
    public V put(final String key, final V value) {
        return super.put(toLower(key), value);
    }

    @Override
    public boolean containsKey(final Object key) {
        return super.containsKey(toLower(key));
    }

    @Override
    public V remove(final Object key) {
        return super.remove(toLower(key));
    }

    private static String toLower(final Object key) {
        return key == null ? null : key.toString().toLowerCase();
    }
}
