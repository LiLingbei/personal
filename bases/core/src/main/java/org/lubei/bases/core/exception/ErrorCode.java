package org.lubei.bases.core.exception;

public class ErrorCode {

    public static final Integer CLASS_NOT_FOUND = 9999;

    //服务类型异常以99开头
    /**
     *
     */
    public static final Integer SERVICE_NOT_FOUND = 9998;
    /**
     * 远程服务注册冲突。该类型的服务已经注册成功。
      */
    public static final Integer SERVICE_REGISTER_CONFLIT=9996;
    //Batis异常以88开头
    public static final Integer MAPPER_NOT_FOUND = 8897;
    //DB异常以22开头
    public static final int DB_OPENING_ERROR = 2200;
    public static final int DB_SQL_ERROR = 2201;

    //数据
    public static final int NO_DATA_AVAILABLE = 2000;

    // public static final int INVALID_PARAMETER = 7001;


    // public static final int NULL_NOT_ALLOWED = 23502;
}
