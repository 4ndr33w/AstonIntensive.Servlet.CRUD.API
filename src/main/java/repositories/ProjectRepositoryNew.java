package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfNonStatic;
import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.ProjectUserRepository;
import repositories.interfaces.UserRepository;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
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
public class ProjectRepositoryNew implements ProjectRepository {

    String schema = System.getenv("JDBC_DEFAULT_SCHEMA") != null
            ? System.getenv("JDBC_DEFAULT_SCHEMA")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");

    String projectsTable = System.getenv("JDBC_PROJECTS_TABLE") != null
            ? System.getenv("JDBC_PROJECTS_TABLE")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.projects-table");

    String projectUsersTable = System.getenv("JDBC_PROJECT_USERS_TABLE") != null
            ? System.getenv("JDBC_PROJECT_USERS_TABLE")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");

    String tableName = String.format("%s.%s", schema, projectsTable);

    Logger logger = LoggerFactory.getLogger(ProjectRepositoryNew.class);

    private final SqlQueryStrings sqlQueryStrings;
    private final ExecutorService dbExecutor;
    private final ProjectUserRepository projectUserRepository;// = new ProjectUsersRepositoryImpl();
    private final UserRepository userRepository;


    public ProjectRepositoryNew() {
        sqlQueryStrings = new SqlQueryStrings();
        projectUserRepository = new ProjectUsersRepositoryImpl();
        userRepository = new UsersRepository();

        ThreadPoolConfNonStatic threadPoolConfNonStatic = new ThreadPoolConfNonStatic();
        dbExecutor = threadPoolConfNonStatic.getDbExecutor();
    }

    @Override
    public CompletableFuture<Project> createAsync(Project project) {
        Objects.requireNonNull(project, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            String tableName = String.format("%s.%s", schema, projectsTable);
            String queryString = sqlQueryStrings.createProjectString(tableName, project);

            try (JdbcConnection jdbcConnection = new JdbcConnection();
                 Statement statement = jdbcConnection.statement()) {

                int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

                if (affectedRows == 0) {
                    logger.error(String.format("Repository:  error: %s; project: %s", StaticConstants.DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE, project));
                    throw new DatabaseOperationException(StaticConstants.ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE);
                }
                project.setId((getGeneratedKeyFromRequest(statement)));
                jdbcConnection.close();
                return project;
            }
            catch (Exception e) {
                logger.error(String.format("Repository:  error: %s", e.getMessage()));
                throw new RuntimeException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<List<Project>> findByAdminIdAsync(UUID adminId) {
        Objects.requireNonNull(adminId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        String queryString = sqlQueryStrings.findProjectsByAdminIdString(tableName, adminId.toString());
        return CompletableFuture.supplyAsync(() -> {
            List<Project> projects = new ArrayList<>();

            try (JdbcConnection jdbcConnection = new JdbcConnection();
                 var resultSet = jdbcConnection.executeQuery(queryString)) {

                while (resultSet.next()) {
                    Project project = mapResultSetToProject(resultSet);

                    projects.add(project);
                }


            } catch (Exception e) {
                logger.error(String.format("Repository: findByAdminIdAsync: \nerror: %s", e.getMessage()));
                throw new RuntimeException("Error finding projects by adminId: " + adminId, e);
            }

            logger.info("Repository: findByAdminIdAsync: \n retrieved list of projects...");
            return projects;
        }, dbExecutor);
    }


    @Override
    public CompletableFuture<List<Project>> findByUserIdAsync(UUID userId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

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
                        List<CompletableFuture<Project>> enrichedProjects = loadUserIdsToFutureProject(projects);

                        return CompletableFuture.allOf(enrichedProjects.toArray(new CompletableFuture[0]))
                                .thenApply(v -> enrichedProjects.stream()
                                        .map(CompletableFuture::join)
                                        .collect(Collectors.toList()));
                    })
                    .join();

        }, dbExecutor);
    }
    private List<CompletableFuture<Project>> loadUserIdsToFutureProject(ArrayList<Project> projects) {

        return projects.stream()
                .map(project -> loadProjectUsers(project.getId())
                        .thenApply(users -> {
                            project.setProjectUsers(users);
                            return project;
                        }))
                .toList();
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
    public CompletableFuture<Project> findByIdAsync(UUID id) {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            String sql = sqlQueryStrings.findByIdString(tableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection();
                 ResultSet resultSet = jdbcConnection.executeQuery(sql)) {

                if (!resultSet.next()) {
                    return null;
                }

                var project = mapResultSetToProject(resultSet);
                List<UserDto> users = loadProjectUsers(id).join();
                project.setProjectUsers(users);
                return project;

            } catch (SQLException e) {
                String message = String.format("%s; id: %s", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, id);
                throw new RuntimeException(message, e);
            } catch (Exception e) {
                throw new RuntimeException(e);
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
    public CompletableFuture<Boolean> deleteAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

            String tableName = String.format("%s.%s", schema, projectsTable);
            String queryString = sqlQueryStrings.deleteByIdString(tableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                int affectedRows = jdbcConnection.executeUpdate(queryString);
                return affectedRows > 0;
            }
            catch (Exception e) {
                logger.error(String.format("Repository: delete: error: %s", e.getMessage()));
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) {

        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return findByIdAsync(projectId)
                .thenApply(ProjectMapper::toDto)
                .thenCompose(projectDto -> {
                    List<UUID> updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                    updatedUsers.add(userId);

                    return projectUserRepository.addUserToProject(userId, projectId)
                            .thenApply(success -> {
                                if (!success) {
                                    logger.error(String.format("Repository: addUserToProject: error: CompletitionException -> SQLException"));
                                    throw new CompletionException(
                                            //
                                            new SQLException("Failed to add user to project"));
                                }
                                logger.info("Repository: addUserToProject: added user to project");
                                projectDto.setProjectUsersIds(updatedUsers);
                                return projectDto;
                            });
                });
    }

    @Override
    public CompletableFuture<ProjectDto> RemoveUserFromProjectAsync(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return findByIdAsync(projectId)
                .thenApply(ProjectMapper::toDto)
                .thenCompose(projectDto -> {
                    List<UUID> updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                    if (!updatedUsers.contains(userId)) {
                        return CompletableFuture.completedFuture(projectDto);
                    }
                    updatedUsers.remove(userId);

                    return projectUserRepository.deleteUserFromProject(userId, projectId)
                            .thenApply(success -> {
                                if (!success) {
                                    logger.error(String.format("Repository: RemoveUserFromProjectAsync: error: CompletitionException -> SQLException"));
                                    throw new CompletionException(
                                            new SQLException("Failed to remove user from project"));
                                }
                                logger.info("Repository: RemoveUserFromProjectAsync: removed user from project");
                                projectDto.setProjectUsersIds(updatedUsers);
                                return projectDto;
                            });
                });
    }

    @Override
    public CompletableFuture<Project> updateAsync(Project project) {
        return CompletableFuture.supplyAsync(() -> {
            Objects.requireNonNull(project, "Project project cannot be null");

            String updateQuery = sqlQueryStrings.updateProjectByIdString(
                    tableName, project.getId().toString(), project);

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                jdbcConnection.setAutoCommit(false);

                int affectedRows = jdbcConnection.executeUpdate(updateQuery);

                if (affectedRows == 0) {
                    logger.error(String.format("Repository: update: error: Project with id %s not found", project.getId()));
                    throw new NoSuchElementException("Project with id " + project.getId() + " not found");
                }
                jdbcConnection.commit();
                return project;
            }
            catch (Exception e) {
                logger.error(String.format("Repository: update: error: %s", e.getMessage()));
                throw new CompletionException("Unexpected error", e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<List<Project>> findAllAsync() {
        return null;
    }
}
