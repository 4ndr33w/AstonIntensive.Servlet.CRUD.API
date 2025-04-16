package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import utils.StaticConstants;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static utils.mappers.ProjectMapper.mapResultSetToProject;

/**
 * @author 4ndr33w
 * @version 1.0
 */
/*public class ProjectsRepositoryImplementation implements ProjectRepository {

    private static final String schema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String projectsTable = PropertiesConfiguration.getProperties().getProperty("jdbc.projects-table");
    private static final String projectUsersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");
    String tableName = String.format("%s.%s", schema, projectsTable);

    Logger logger = LoggerFactory.getLogger(ProjectsRepositoryImplementation.class);

    private final SqlQueryStrings sqlQueryStrings;
    private static final ExecutorService dbExecutor;
    private final UserRepository userRepository;

    static {
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    public ProjectsRepositoryImplementation() {
        sqlQueryStrings = new SqlQueryStrings();
        userRepository = new UsersRepositoryImplementation();
    }

    @Override
    public CompletableFuture<List<Project>> findByAdminIdAsync(UUID adminId) {
        return CompletableFuture.supplyAsync(() -> {
            if (adminId == null) {
                throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
            }

            String queryString = sqlQueryStrings.findProjectsByAdminIdString(tableName, adminId.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                var resultSet = jdbcConnection.executeQuery(queryString);

                List<CompletableFuture<Project>> projectFutures = new ArrayList<>();

                while (resultSet.next()) {
                    Project project = mapResultSetToProject(resultSet);
                    projectFutures.add(
                            loadProjectUsers(project.getId())
                                    .thenApply(users -> {
                                        project.setProjectUsers(users);
                                        return project;
                                    })
                    );
                }

                return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> projectFutures.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList()))
                        .join();

            } catch (Exception e) {
                throw new CompletionException(StaticConstants.NO_PROJECTS_FOUND_BY_ADMIN_ID_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
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
                var resultSet = jdbcConnection.executeQuery(queryString);

                if (!resultSet.next()) {
                    return null;
                }

                Project project = mapResultSetToProject(resultSet);

                List<UserDto> users = loadProjectUsers(id).join();
                project.setProjectUsers(users);

                return project;
            } catch (Exception e) {
                throw new CompletionException("Failed to find project by id: " + id, e);
            }
        }, dbExecutor);
    }

    private CompletableFuture<List<UserDto>> loadProjectUsers(UUID projectId) {
        return CompletableFuture.supplyAsync(() -> {
            String projectUsersTableName = String.format("%s.%s", schema, projectUsersTable);
            String queryString = sqlQueryStrings
                    .selectUserIdsFromProjectUsersTableByProjectId(
                            projectUsersTableName,
                            projectId.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection();
                 var resultSet = jdbcConnection.executeQuery(queryString)) {

                List<CompletableFuture<UserDto>> userFutures = new ArrayList<>();

                while (resultSet.next()) {
                    UUID userId = (UUID) resultSet.getObject("user_id");
                    userFutures.add(
                            userRepository.findByIdAsync(userId)
                                    .thenApply(UserMapper::toDto)
                    );
                }

                return CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> userFutures.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList()))
                        .join();

            } catch (Exception e) {
                throw new CompletionException("Failed to load project users", e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<Project> createAsync(Project project) {

        return CompletableFuture.supplyAsync(() -> {
            if (project == null) {
                throw new IllegalArgumentException("User project cannot be null");
            }
            String tableName = String.format("%s.%s", schema, projectsTable);
            String queryString = sqlQueryStrings.createProjectString(tableName, project);

            try (JdbcConnection jdbcConnection = new JdbcConnection();
                 Statement statement = jdbcConnection.statement()) {

                int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

                if (affectedRows == 0) {
                    logger.error("RepositoryImplementation: Failed to create user");
                    throw new RuntimeException("Failed to create user, no rows affected");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        project.setId((UUID) generatedKeys.getObject(1));
                        logger.info("RepositoryImplementation: Successfully created user");
                        return project;
                    }
                    logger.error("RepositoryImplementation: Failed to retrieve generated keys");
                    throw new RuntimeException("Failed to retrieve generated keys");
                }
            }
            catch (Exception e) {
                logger.error("RepositoryImplementation: Failed to create user", e.getMessage());
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) {
        if (userId == null || projectId == null) {
            logger.error("RepositoryImplementation: Parameters cannot be null");
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Parameters cannot be null"));
        }

        return findByIdAsync(projectId)
                .thenApply(ProjectMapper::toDto)
                .thenCompose(projectDto -> {
                    List<UUID> updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                    updatedUsers.add(userId);

                    logger.info("RepositoryImplementation: Adding user into project: {}", userId);
                    return insertUsersIntoProjectUsersTable(userId, projectId)
                            .thenApply(v -> {
                                projectDto.setProjectUsersIds(updatedUsers);
                                logger.info("RepositoryImplementation: Successfully added user into project: {}", userId);
                                return projectDto;
                            });
                });
    }

    private CompletableFuture<Void> insertUsersIntoProjectUsersTable(UUID userId, UUID projectId) {
        return CompletableFuture.runAsync(() -> {
            String tableName = String.format("%s.%s", schema, projectUsersTable);
            String query = sqlQueryStrings.addUserIntoProjectString(
                    tableName, projectId.toString(), userId.toString());

            try (JdbcConnection connection = new JdbcConnection();
                 Statement statement = connection.statement()) {

                connection.setAutoCommit(false);
                try {
                    int affected = statement.executeUpdate(query);
                    if (affected == 0) {
                        logger.error("RepositoryImplementation: Failed to add user into project: {}", userId);
                        throw new SQLDataException("No rows affected");
                    }
                    logger.info("RepositoryImplementation: Successfully added user into project: {}", userId);
                    connection.commit();
                } catch (SQLException e) {
                    logger.error("RepositoryImplementation: Failed to add user into project: {}", userId, e);
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logger.error("RepositoryImplementation: Failed to add user into project: {}", userId, e);
                throw new CompletionException("Database error", e);
            } catch (Exception e) {
                logger.error("RepositoryImplementation: Failed to add user into project: {}", userId, e);
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<List<Project>> findByUserIdAsync(UUID userId) {

        return CompletableFuture.supplyAsync(() -> {
            if (userId == null) {
                return Collections.emptyList();
            }
            CompletableFuture<List<Project>> adminProjectsFuture = findByAdminIdAsync(userId);

            CompletableFuture<List<Project>> memberProjectsFuture = findProjectsByUserIdIfUserNotProjectAdmin(userId);

            return adminProjectsFuture
                    .thenCombine(memberProjectsFuture, (adminProjects, memberProjects) -> {
                        Set<Project> combined = new LinkedHashSet<>();
                        combined.addAll(adminProjects);
                        combined.addAll(memberProjects);
                        return new ArrayList<>(combined);
                    })
                    .thenCompose(projects -> {
                        List<CompletableFuture<Project>> enrichedProjects = projects.stream()
                                .map(project -> loadProjectUsers(project.getId())
                                        .thenApply(users -> {
                                            project.setProjectUsers(users);
                                            return project;
                                        }))
                                .toList();

                        return CompletableFuture.allOf(enrichedProjects.toArray(new CompletableFuture[0]))
                                .thenApply(v -> enrichedProjects.stream()
                                        .map(CompletableFuture::join)
                                        .collect(Collectors.toList()));
                    })
                    .join();

        }, dbExecutor);
    }

    private CompletableFuture<List<Project>> findProjectsByUserIdIfUserNotProjectAdmin(UUID userId) {
        CompletableFuture<List<Project>> memberProjectsFuture = CompletableFuture.supplyAsync(() -> {

            String projectsTableName = String.format("%s.%s", schema, projectsTable);
            String projectUsersTableName = String.format("%s.%s", schema, projectUsersTable);
            String query = sqlQueryStrings.findAllProjectsByUserId(projectsTableName, projectUsersTableName, userId.toString());

            try (JdbcConnection conn = new JdbcConnection();
                 ResultSet rs = conn.executeQuery(query)) {

                List<Project> projects = new ArrayList<>();
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
                return projects;
            } catch (SQLException e) {
                throw new CompletionException("Failed to find member projects", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
        return  memberProjectsFuture;
    }

    @Override
    public CompletableFuture<Boolean> deleteAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) {
                return false;
            }

            String tableName = String.format("%s.%s", schema, projectsTable);
            String queryString = sqlQueryStrings.deleteByIdString(tableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                int affectedRows = jdbcConnection.executeUpdate(queryString);
                return affectedRows > 0;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<ProjectDto> RemoveUserFromProjectAsync(UUID userId, UUID projectId) {
        if (userId == null || projectId == null) {
            logger.error("RepositoryImplementation: Parameters cannot be null");
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Parameters cannot be null"));
        }

        return findByIdAsync(projectId)
                .thenApply(ProjectMapper::toDto)
                .thenCompose(projectDto -> {
                    List<UUID> updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                    updatedUsers.remove(userId);

                    return deleteUsersFromProjectUsersTable(userId, projectId)
                            .thenApply(v -> {
                                logger.info("RepositoryImplementation: Removed user from project: {}", userId);
                                projectDto.setProjectUsersIds(updatedUsers);
                                return projectDto;
                            });
                });
    }

    private CompletableFuture<Void> deleteUsersFromProjectUsersTable(UUID userId, UUID projectId) {
        return CompletableFuture.runAsync(() -> {
            String tableName = String.format("%s.%s", schema, projectUsersTable);
            String query = sqlQueryStrings.removeUserFromProjectString(
                    tableName, projectId.toString(), userId.toString());

            try (JdbcConnection connection = new JdbcConnection();
                 Statement statement = connection.statement()) {

                connection.setAutoCommit(false);
                try {
                    int affected = statement.executeUpdate(query);
                    if (affected == 0) {
                        logger.error("RepositoryImplementation: Failed to remove user from project: {}\nNo rows affected", userId);
                        throw new SQLDataException("No rows affected");
                    }
                    logger.info("RepositoryImplementation: Successfully removed user from project: {}", userId);
                    connection.commit();
                } catch (SQLException e) {
                    logger.error("RepositoryImplementation: Failed to remove user from project: {}", userId, e);
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logger.error("RepositoryImplementation: Failed to remove user from project: {}", userId, e);
                throw new CompletionException("Database error", e);
            } catch (Exception e) {
                logger.error("RepositoryImplementation: Failed to remove user from project: {}", userId, e.getMessage());
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }










    // ToDo: реализовать методы
    @Override
    public CompletableFuture<List<Project>> findAllAsync() {
        return null;
    }

    @Override
    public CompletableFuture<Project> updateAsync(Project item) {
        return null;
    }
}
*/