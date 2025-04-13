package repositories;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.entities.Project;
import models.entities.User;
import repositories.interfaces.ProjectRepository;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static utils.mappers.ProjectMapper.mapResultSetToProject;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsRepository implements ProjectRepository {

    private static final String schema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String projectsTable = PropertiesConfiguration.getProperties().getProperty("jdbc.projects-table");
    private static final String projectUsersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");

    private final SqlQueryStrings sqlQueryStrings;
    private static final ExecutorService dbExecutor;

    static {
        dbExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("jdbc-worker-%d").build()
        );
    }

    public ProjectsRepository() {
        sqlQueryStrings = new SqlQueryStrings();
    }

    @Override
    public CompletableFuture<Project> findByIdAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) {
                return null;
            }

            String tableName = String.format("%s.%s", schema, projectsTable);
            String queryString = sqlQueryStrings.findById(tableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                var resultSet  = jdbcConnection.executeQuery(queryString);
                /*var next = resultSet.next();
                Project result = null;
                if(next) {
                    result = mapResultSetToProject(resultSet);
                }*/

                return resultSet.next() ? mapResultSetToProject(resultSet) : null;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }
    @Override
    public CompletableFuture<Project> createAsync(Project item) throws SQLException {
        //7f1111e0-8020-4de6-b15a-601d6903b9eb
        return CompletableFuture.supplyAsync(() -> {
            if (item == null) {
                throw new IllegalArgumentException("User item cannot be null");
            }

            String tableName = String.format("%s.%s", schema, projectsTable);
            String queryString = sqlQueryStrings.createProjectString(tableName, item);

            try (JdbcConnection jdbcConnection = new JdbcConnection();
                 Statement statement = jdbcConnection.statement()) {

                int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

                if (affectedRows == 0) {
                    throw new RuntimeException("Failed to create user, no rows affected");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId((UUID) generatedKeys.getObject(1));
                        return item;
                    }
                    throw new RuntimeException("Failed to retrieve generated keys");
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }








    @Override
    public CompletableFuture<List<Project>> findByAdminIdAsync(UUID adminId) {
        return null;
    }



    @Override
    public CompletableFuture<List<User>> findByNameAsync(String name) {
        return null;
    }

    @Override
    public CompletableFuture<List<User>> findAllAsync() throws SQLException {
        return null;
    }





    @Override
    public CompletableFuture<Project> updateAsync(Project item) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteAsync(UUID id) throws SQLException {
        return null;
    }

    @Override
    public CompletableFuture<Project> AddUserToProjectAsync(UUID userId, UUID projectId) {
        return null;
    }

    @Override
    public CompletableFuture<Project> RemoveUserFromProjectAsync(UUID userId, UUID projectId) {
        return null;
    }
}
