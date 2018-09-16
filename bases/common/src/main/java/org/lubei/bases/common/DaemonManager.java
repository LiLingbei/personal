package org.lubei.bases.common;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 服务管理器
 */
public class DaemonManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DaemonManager.class);

    ServiceManager manager;

    public DaemonManager(Iterable<? extends Service> services) {
        manager = new ServiceManager(services);
        manager.addListener(new ServiceManager.Listener() {
                                public void stopped() {
                                }

                                public void healthy() {
                                    // Services have been initialized and are healthy, start accepting requests...
                                    LOGGER.info("daemons healthy");
                                    LOGGER.info("daemons {}", manager);
                                }

                                public void failure(Service service) {
                                    // Something failed, at this point we could log it, notify a load balancer, or take
                                    // some other action.  For now we will just exit.
                                    LOGGER.info("daemon fail, exit {}, cause:", service,
                                                service.failureCause());
                                    System.exit(1);
                                }
                            },
                            MoreExecutors.directExecutor());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOGGER.info("shutdown hooked");
                // Give the services 5 seconds to stop to ensure that we are responsive to shutdown
                // requests.
                try {
                    manager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
                } catch (TimeoutException timeout) {
                    // stopping timed out
                }
            }
        });
    }

    @Override
    public String toString() {
        return manager.toString();
    }

    public void start() {
        manager.startAsync().awaitHealthy();
    }

}
