package configurations;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ThreadPoolConfiguration {

    private static final ExecutorService dbExecutor;

    static {
        dbExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("jdbc-worker-%d").build()
        );
    }
    public static ExecutorService getDbExecutor() {
        if (dbExecutor != null) {
            return dbExecutor;
        } else {
            throw new IllegalStateException("dbExecutor is not initialized");
        }
    }
}
