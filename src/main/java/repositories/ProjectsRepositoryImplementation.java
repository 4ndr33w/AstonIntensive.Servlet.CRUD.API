package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.Project;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import utils.StaticConstants;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.*;
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
public class ProjectsRepositoryImplementation implements ProjectRepository {

    private static final String schema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String projectsTable = PropertiesConfiguration.getProperties().getProperty("jdbc.projects-table");
    private static final String projectUsersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");
    String tableName = String.format("%s.%s", schema, projectsTable);

    private final SqlQueryStrings sqlQueryStrings;
    private static final ExecutorService dbExecutor;
    private final UserRepository userRepository;
    private final ProjectUsersRepository projectUsersRepository;

    static {
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    public ProjectsRepositoryImplementation() {
        sqlQueryStrings = new SqlQueryStrings();
        userRepository = new UsersRepositoryImplementation();
        projectUsersRepository = new ProjectUsersRepository();
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
                List<Project> projects = new ArrayList<>();

                while (resultSet.next()) {
                    Project project = mapResultSetToProject(resultSet);
                    projects.add(project);
                }

                return projects;

            } catch (Exception e) {
                throw new CompletionException(StaticConstants.NO_PROJECTS_FOUND_BY_ADMIN_ID_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<Project> findByIdAsync(UUID id){
        try {
            return CompletableFuture.supplyAsync(() -> {
                if (id == null) {
                    throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
                }

                String queryString = sqlQueryStrings.findByIdString(tableName, id.toString());

                try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                    var resultSet = jdbcConnection.executeQuery(queryString);
                    return resultSet.next() ? mapResultSetToProject(resultSet) : null;
                } catch (SQLException e) {
                    throw new CompletionException("Failed to find project by id", e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, dbExecutor);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

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
    public CompletableFuture<Project> createAsync(Project project) throws SQLException {

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
                    throw new RuntimeException("Failed to create user, no rows affected");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        project.setId((UUID) generatedKeys.getObject(1));
                        return project;
                    }
                    throw new RuntimeException("Failed to retrieve generated keys");
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
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
                        throw new SQLDataException("No rows affected");
                    }
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
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
    public CompletableFuture<List<Project>> findByUserIdAsync(UUID userId) {
        if (userId == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        CompletableFuture<List<Project>> adminProjectsFuture = findByAdminIdAsync(userId);
        CompletableFuture<List<Project>> memberProjectsFuture = findProjectsByUserIdIfUserNotProjectAdmin(userId);

        return adminProjectsFuture
                .thenCombine(memberProjectsFuture, (adminProjects, memberProjects) -> {
                    Set<Project> combined = new LinkedHashSet<>();
                    combined.addAll(adminProjects);
                    combined.addAll(memberProjects);
                    return new ArrayList<>(combined);
                });
    }

    public CompletableFuture<List<Project>> findProjectsByUserIdIfUserNotProjectAdmin(UUID userId) {
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
    public CompletableFuture<Boolean> deleteAsync(UUID id) throws SQLException {
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
    /*public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException {
        if (userId == null || projectId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Parameters cannot be null"));
        }

        return findByIdAsync(projectId)
                .thenApply(ProjectMapper::toDto)
                .thenCompose(projectDto -> {
                    List<UUID> updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                    updatedUsers.add(userId);

                    return insertUsersIntoProjectUsersTable(userId, projectId)
                            .thenApply(v -> {
                                projectDto.setProjectUsersIds(updatedUsers);
                                return projectDto;
                            });
                });
    }*/
    public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) {
        if (userId == null || projectId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Parameters cannot be null"));
        }

        return findByIdAsync(projectId)
                .thenCompose(project -> {
                    // Создаем DTO сразу с обновленным списком пользователей
                    ProjectDto projectDto = ProjectMapper.toDto(project);
                    List<UUID> updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                    updatedUsers.add(userId);
                    projectDto.setProjectUsersIds(updatedUsers);

                    return projectUsersRepository.addUserToProject(userId, projectId)
                            .thenApply(projectUsersDto -> projectDto);
                });
    }

    @Override
    public CompletableFuture<ProjectDto> RemoveUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException {
        if (userId == null || projectId == null) {
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
                        throw new SQLDataException("No rows affected");
                    }
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                throw new CompletionException("Database error", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }






    public CompletableFuture<List<Project>> findAllByIdsAsync(List<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.supplyAsync(() -> {
            String tableName = String.format("%s.%s", schema, this.tableName);
            String query = sqlQueryStrings.findAllByIdsString(tableName, projectIds.size());

            try (JdbcConnection connection = new JdbcConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                // Устанавливаем параметры для IN-условия
                for (int i = 0; i < projectIds.size(); i++) {
                    statement.setObject(i + 1, projectIds.get(i));
                }

                List<Project> projects = new ArrayList<>();
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        projects.add(mapResultSetToProject(resultSet));
                    }
                }
                return projects;

            } catch (SQLException e) {
                throw new CompletionException("Failed to load projects by ids", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }



    // ToDo: реализовать методы
    @Override
    public CompletableFuture<List<Project>> findAllAsync() throws SQLException {
        return null;
    }

    @Override
    public CompletableFuture<Project> updateAsync(Project item) {
        return null;
    }
}
