package repositories;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.dtos.ProjectDto;
import models.entities.Project;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import utils.mappers.ProjectMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static utils.mappers.ProjectMapper.toDto;
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
    private final UserRepository userRepository;

    static {
        dbExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("jdbc-worker-%d").build()
        );
    }

    public ProjectsRepository() throws SQLException {
        sqlQueryStrings = new SqlQueryStrings();
        userRepository = new UsersRepository();
    }

    @Override
    public CompletableFuture<Project> findByIdAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) {
                return null;
            }
            String tableName = String.format("%s.%s", schema, projectsTable);
            String queryString = sqlQueryStrings.findByIdString(tableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                var resultSet  = jdbcConnection.executeQuery(queryString);

                return resultSet.next() ? mapResultSetToProject(resultSet) : null;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<Project> createAsync(Project item) throws SQLException {

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
        return CompletableFuture.supplyAsync(() -> {
            if (adminId == null) {
                return null;
            }
            String tableName = String.format("%s.%s", schema, projectsTable);
            String queryString = sqlQueryStrings.findProjectsByAdminIdString(tableName, adminId.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                var resultSet  = jdbcConnection.executeQuery(queryString);

                List<Project> projects = new ArrayList<>();
                while (resultSet.next()) {
                    projects.add(mapResultSetToProject(resultSet));
                }
                return projects;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) {
        if (userId == null || projectId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Parameters cannot be null"));
        }

        return findByIdAsync(projectId)
                .thenApply(ProjectMapper::toDto)
                .thenCompose(projectDto -> {
                    List<UUID> updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                    updatedUsers.add(userId);

                    return executeUpdate(userId, projectId, updatedUsers)
                            .thenApply(v -> {
                                projectDto.setProjectUsersIds(updatedUsers);
                                return projectDto;
                            });
                });
    }

    private CompletableFuture<Void> executeUpdate(UUID userId, UUID projectId, List<UUID> updatedUsers) {
        return CompletableFuture.runAsync(() -> {
            String tableName = String.format("%s.%s", schema, projectUsersTable);
            String query = sqlQueryStrings.addUserIntoProjectString(
                    tableName, projectId.toString(), userId.toString());

            try (JdbcConnection conn = new JdbcConnection();
                 Statement stmt = conn.statement()) {

                conn.setAutoCommit(false);
                try {
                    int affected = stmt.executeUpdate(query);
                    if (affected == 0) {
                        throw new SQLDataException("No rows affected");
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                throw new CompletionException("Database error", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }









    





    @Override
    public CompletableFuture<List<Project>> findAllAsync() throws SQLException {
        return null;
    }




    @Override
    public CompletableFuture<List<Project>> findByUserIdAsync(UUID adminId) {
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
    public CompletableFuture<ProjectDto> RemoveUserFromProjectAsync(UUID userId, ProjectDto projectDto) {
        return null;
    }
}
