package configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Класс для работы с JDBC
 * <p>Предоставляет интерфейс для работы с БД
 * ссконфигурированы настройки БД
 * и предоставлен пул потоков</p>
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class JdbcConnection implements AutoCloseable{

    private final Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    Logger logger = LoggerFactory.getLogger(DataSourceProvider.class);

    public JdbcConnection() throws SQLException {
        DataSource dataSource = DataSourceProvider.getDataSource();
        this.connection = dataSource.getConnection();
    }

    public Connection getConnection() {
        logger.info("Получен объект Connection класса JdbcConnection");
        return connection;
    }

    public void setAutoCommit(boolean flag) throws SQLException {
        if (connection != null) {
            logger.info("AutoCommit: Выставлен флаг {}", flag);
            connection.setAutoCommit(flag);
        }
    }

    public void commit() throws SQLException {
        if (connection != null) {
            logger.info("Выполнен коммит");
            connection.commit();
        }
    }

    public void rollback() throws SQLException {
        if (connection != null) {
            logger.info("Выполнен откат коммита");
            connection.rollback();
        }
    }

    public ResultSet executeQuery(String query) throws SQLException {
        closeResultSet();
        this.statement = connection.createStatement();
        this.resultSet = statement.executeQuery(query);
        logger.info("Получен ResultSet; Выполнен запрос: {}", query);
        return resultSet;
    }

    public boolean execute(String query) throws SQLException {
        closeStatement();
        this.statement = connection.createStatement();
        logger.info("Выполнен запрос: {}", query);
        return statement.execute(query);
    }

    public int executeUpdate(String query) throws SQLException {
        closeStatement();
        this.statement = connection.createStatement();
        logger.info("Выполнен запрос: {}", query);
        return statement.executeUpdate(query);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        closeStatement();
        logger.info("Выполнен запрос: {}", sql);
        return connection.prepareStatement(sql);
    }

    public Statement statement() throws SQLException {
        closeStatement();
        logger.info("Получен объект Statement");
        return connection.createStatement();
    }

    @Override
    public void close() throws Exception {


        closeResultSet();
        closeStatement();
        closeConnection();
    }

    private void closeResultSet() {
        if (resultSet != null) {
            try {
                logger.info("Закрыт ResultSet");
                resultSet.close();
            } catch (SQLException e) {
                logger.error("Ошибка закрытия ResultSet");
                throw new RuntimeException(e);
            }
        }
        resultSet = null;
    }

    private void closeStatement() {
        if (statement != null) {
            try {
                logger.info("Закрыт Statement");
                statement.close();
            } catch (SQLException e) {
                logger.error("Ошибка закрытия Statement");
                throw new RuntimeException(e);
            }
        }
        statement = null;
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                logger.info("Закрыт Connection");
                connection.close();
            } catch (SQLException e) {
                logger.error("Ошибка закрытия Connection");
                throw new RuntimeException(e);
            }
        }
    }
}
