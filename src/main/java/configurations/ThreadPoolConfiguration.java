package configurations;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ProjectsService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс предоставляет Thread Pool
 * для процесса работы приложения в параллельных потоках
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class ThreadPoolConfiguration {

    static Logger logger = LoggerFactory.getLogger( ThreadPoolConfiguration.class);
    private static final ExecutorService dbExecutor;

    static {
        /*dbExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("jdbc-worker-%d").build()
        );*/
        dbExecutor = Executors.newFixedThreadPool(20,
                new ThreadFactoryBuilder().setNameFormat("jdbc-worker-%d").build()
        );
    }
    public static ExecutorService getDbExecutor() {
        if (dbExecutor != null) {
            return dbExecutor;
        } else {
            logger.error("Ошибка инициализации Thread Pool");
            throw new IllegalStateException("dbExecutor is not initialized");
        }
    }
}
