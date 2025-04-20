package configurations;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ThreadPoolConfNonStatic {
    Logger logger = LoggerFactory.getLogger( ThreadPoolConfiguration.class);
    private final ExecutorService dbExecutor;
    String dbUrl = System.getenv("JDBC_URL") != null
            ? System.getenv("JDBC_URL")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.url");

    String user = System.getenv("JDBC_USERNAME") != null
            ? System.getenv("JDBC_USERNAME")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.username");


    public ThreadPoolConfNonStatic() {
        logger.error(dbUrl);
        logger.error(user);
        logger.info(dbUrl);
        logger.info(user);
        dbExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("jdbc-worker-%d").build()
        );
    }


    public  ExecutorService getDbExecutor() {
        if (dbExecutor != null) {
            return dbExecutor;
        } else {
            logger.error("Ошибка инициализации Thread Pool");
            throw new IllegalStateException("dbExecutor is not initialized");
        }
    }
}
