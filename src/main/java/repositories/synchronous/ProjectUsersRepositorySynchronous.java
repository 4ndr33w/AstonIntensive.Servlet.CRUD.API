package repositories.synchronous;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    Logger logger = LoggerFactory.getLogger(ProjectUsersRepositorySynchronous.class);


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
                    logger.error("No rows affected");
                    throw new SQLDataException("No rows affected");
                }
                logger.info("Number of affected rows: {}", affected);
                connection.commit();
                return true;
            } catch (SQLException e) {
                logger.error("Error adding user to project");
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error adding user to project", e.getMessage());
            throw new CompletionException("Database error", e);
        } catch (Exception e) {
            logger.error("Error adding user to project", e.getMessage());
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
                    logger.error("No rows affected");
                    throw new SQLDataException("No rows affected");
                }
                logger.info("Number of affected rows: {}", affected);
                connection.commit();
                return true;
            } catch (SQLException e) {
                logger.error("Error deleting user from project");
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error deleting user from project", e.getMessage());
            throw new CompletionException("Database error", e);
        } catch (Exception e) {
            logger.error("Error deleting user from project", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
