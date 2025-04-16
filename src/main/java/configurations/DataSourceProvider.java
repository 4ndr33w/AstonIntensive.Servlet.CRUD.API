package configurations;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Класс предоставляет DataSource,
 * пул потоков и конфигурацию для подключения к БД
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class DataSourceProvider {

    static String dbUrl = PropertiesConfiguration.getProperties().getProperty("jdbc.url");
    static String user = PropertiesConfiguration.getProperties().getProperty("jdbc.username");
    static String pass = PropertiesConfiguration.getProperties().getProperty("jdbc.password");

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
        return dataSource;
    }
}
