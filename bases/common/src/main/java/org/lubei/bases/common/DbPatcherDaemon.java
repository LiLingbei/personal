package org.lubei.bases.common;

import org.lubei.bases.common.db.patcher.Runner;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractService;
import org.lubei.bases.db.DbPatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 数据库补丁执行服务
 *
 * <p>由于必须等待补丁执行结束，所以该类继承自AbstractService</p>
 *
 * @author liwenheng@ruijie.com.cn
 */
public class DbPatcherDaemon extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbPatcherDaemon.class);

    @Override
    protected void doStart() {
        LOGGER.info("开始检查并应用数据库补丁");
        try {
            DbPatcher patcher = new DbPatcher(App.db.getDataSource());
            patcher.migrate();
            Runner runner=new Runner();
            runner.merge();
            LOGGER.info("检查并应用数据库补丁结束");
            this.notifyStarted();
        } catch (IOException e) {
            LOGGER.error("migrate db fail:", e);
            Throwables.propagate(e);
        }
    }

    @Override
    protected void doStop() {

    }
}
