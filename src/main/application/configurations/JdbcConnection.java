package configurations;

import javax.sql.DataSource;
import java.sql.*;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class JdbcConnection implements AutoCloseable{

    private final Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public JdbcConnection() throws SQLException {
        DataSource dataSource = DataSourceProvider.getDataSource();
        this.connection = dataSource.getConnection();
    }
/*
    public Connection getConnection() {
        return connection;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        closeResultSet();
        this.statement = connection.createStatement();
        this.resultSet = statement.executeQuery(query);
        return resultSet;
    }

    public boolean execute(String query) throws SQLException {
        closeStatement();
        this.statement = connection.createStatement();
        return statement.execute(query);
    }
*/
    public int executeUpdate(String query) throws SQLException {
        closeStatement();
        this.statement = connection.createStatement();
        return statement.executeUpdate(query);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        closeStatement();
        return connection.prepareStatement(sql);
    }

    public Statement statement() throws SQLException {
        closeStatement();
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
                resultSet.close();
            } catch (SQLException e) {
                // ToDo:  Добавить логгирование ошибки закрытия
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
                // ToDo:  Добавить логгирование ошибки закрытия
                throw new RuntimeException(e);
            }
        }
        statement = null;
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ToDo:  Добавить логгирование ошибки закрытия
                throw new RuntimeException(e);
            }
        }
    }
}
