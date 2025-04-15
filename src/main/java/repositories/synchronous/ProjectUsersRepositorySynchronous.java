package repositories.synchronous;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import utils.sqls.SqlQueryStrings;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectUsersRepositorySynchronous {

    private final SqlQueryStrings sqlQueryStrings;
    private static final String schema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String projectUsersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");
    String tableName = String.format("%s.%s", schema, projectUsersTable);


    public ProjectUsersRepositorySynchronous() {
        this.sqlQueryStrings = new SqlQueryStrings();
    }
    public boolean addUserToProject(UUID userId, UUID projectId) {
        String tableName = String.format("%s.%s", schema, projectUsersTable);
        String query = sqlQueryStrings.addUserIntoProjectString(
                tableName, projectId.toString(), userId.toString());

        try (JdbcConnection connection = new JdbcConnection();
             Statement statement = connection.statement()) {

            connection.setAutoCommit(false);
            try {
                int affected = statement.executeUpdate(query);
                if (affected == 0) {
                    throw new SQLDataException("No rows affected");
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new CompletionException("Database error", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteUserFromProject(UUID userId, UUID projectId) {
        String tableName = String.format("%s.%s", schema, projectUsersTable);
        String query = sqlQueryStrings.removeUserFromProjectString(
                tableName, projectId.toString(), userId.toString());

        try (JdbcConnection connection = new JdbcConnection();
             Statement statement = connection.statement()) {

            connection.setAutoCommit(false);
            try {
                int affected = statement.executeUpdate(query);
                if (affected == 0) {
                    throw new SQLDataException("No rows affected");
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new CompletionException("Database error", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
