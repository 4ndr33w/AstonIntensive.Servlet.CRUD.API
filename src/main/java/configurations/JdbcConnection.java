package configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    Logger logger = LoggerFactory.getLogger(JdbcConnection.class);

    static String dbUrl = System.getenv("JDBC_URL") != null
            ? System.getenv("JDBC_URL")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.url");

    static String user = System.getenv("JDBC_USERNAME") != null
            ? System.getenv("JDBC_USERNAME")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.username");

    static String pass = System.getenv("JDBC_PASSWORD") != null
            ? System.getenv("JDBC_PASSWORD")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.password");

    public JdbcConnection() throws SQLException {
        connection = DriverManager.getConnection(dbUrl, user, pass);

    }

    public Connection getConnection() throws SQLException {
           return connection;
    }


    public ResultSet executeQuery(String query) throws Exception {
        closeResultSet();
        this.statement = connection.createStatement();
        this.resultSet = statement.executeQuery(query);

        return resultSet;
    }

    public PreparedStatement prepareStatementReturningGeneratedKey(String sql) throws SQLException {
        closePreparedStatement();
        this.preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        return this.preparedStatement;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        closePreparedStatement();
        this.preparedStatement = connection.prepareStatement(sql);
        return this.preparedStatement;
    }

    @Override
    public void close() {

        closeResultSet();
        closeStatement();
        closeConnection();
    }

    private void closeResultSet() {
        if (resultSet != null) {
            try {
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
                    statement.close();
            } catch (SQLException e) {
                logger.error("Ошибка закрытия Statement");
                throw new RuntimeException(e);
            }
        }
        statement = null;
    }

    private void closePreparedStatement() {
        if (this.preparedStatement != null) {
            try {
                this.preparedStatement.close();
            } catch (SQLException e) {
                logger.error("Ошибка закрытия PreparedStatement");
                throw new RuntimeException(e);
            }
        }
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                 connection.close();
            } catch (SQLException e) {
                logger.error("Ошибка закрытия Connection");
                throw new RuntimeException(e);
            }
        }
    }
}
