package configurations;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.synchronous.ProjectUsersRepositorySynchronous;

import javax.sql.DataSource;

/**
 * Класс предоставляет DataSource,
 * пул потоков и конфигурацию для подключения к БД
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class DataSourceProvider {

    static Logger logger = LoggerFactory.getLogger(DataSourceProvider.class);
    /*static String dbUrl = PropertiesConfiguration.getProperties().getProperty("jdbc.url");
    static String user = PropertiesConfiguration.getProperties().getProperty("jdbc.username");
    static String pass = PropertiesConfiguration.getProperties().getProperty("jdbc.password");*/

    /*static String dbUrl = PropertiesConfiguration.getProperties().getProperty("jdbc.url");
    static String user = PropertiesConfiguration.getProperties().getProperty("jdbc.username");
    static String pass = PropertiesConfiguration.getProperties().getProperty("jdbc.password");
    */

    static String dbUrl = System.getenv("JDBC_URL") != null
            ? System.getenv("JDBC_URL")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.url");

    static String user = System.getenv("JDBC_USERNAME") != null
            ? System.getenv("JDBC_USERNAME")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.username");

    static String pass = System.getenv("JDBC_PASSWORD") != null
            ? System.getenv("JDBC_PASSWORD")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.password");


    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        logger.info("Предоставлен источник DataSource");
        logger.info("Database URL:" + dbUrl);
        logger.info("Database username:" + user);
        return dataSource;
    }
}
