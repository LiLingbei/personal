package org.lubei.bases.core.db;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum DbType {
    PostgreSQL, MySQL, Oracle, H2, MicrosoftSQLServer;

    private static final Map<String, DbType> stringToEnum;

    static {
        ImmutableMap.Builder<String, DbType> builder = new ImmutableMap.Builder<String, DbType>();
        for (DbType dbType : values()) {
            builder.put(dbType.toString(), dbType);
        }
        stringToEnum = builder.build();
    }

    public static DbType fromString(String symbol) {
        if (symbol.contains(" ")) {
            symbol = symbol.replaceAll(" ", "");
        }
        return stringToEnum.get(symbol);
    }
}
