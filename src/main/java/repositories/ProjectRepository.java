package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.dtos.ProjectDto;
import models.dtos.ProjectUsersDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.ProjectUserRepository;
import repositories.interfaces.UserRepository;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.NoProjectsFoundException;
import utils.exceptions.NoUsersFoundException;
import utils.exceptions.ProjectNotFoundException;
import utils.mappers.ProjectMapper;
import utils.mappers.ProjectUserMapper;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryPreparedStrings;
import utils.sqls.SqlQueryStrings;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static utils.mappers.ProjectMapper.mapResultSetToProject;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectRepository implements repositories.interfaces.ProjectRepository {

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
    Logger logger = LoggerFactory.getLogger(ProjectRepository.class);

    private final SqlQueryStrings sqlQueryStrings;
    private final SqlQueryPreparedStrings sqlQueryPreparedStrings;
    private final ProjectUserRepository projectUserRepository;// = new ProjectUsersRepositoryImpl();
    private final UserRepository userRepository;


    public ProjectRepository() {
        sqlQueryStrings = new SqlQueryStrings();
        projectUserRepository = new ProjectUsersRepositoryImpl();
        userRepository = new UsersRepository();
        sqlQueryPreparedStrings = new SqlQueryPreparedStrings();
    }

    @Override
    public CompletableFuture<Project> createAsync(Project project) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException {
        Objects.requireNonNull(project, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return create(project);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private Project create(Project project) throws SQLException, DatabaseOperationException {
        String queryString = sqlQueryPreparedStrings.createProjectString(tableName);

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.prepareStatementReturningGeneratedKey(queryString)) {

            setPreparedStatementToCreateProject(statement, project);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                logger.error(String.format("Repository:  error: %s; project: %s", StaticConstants.DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE, project));
                throw new DatabaseOperationException(StaticConstants.ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE);
            }
            project.setId((getGeneratedKeyFromRequest(statement)));
            return project;
        }
        catch (SQLException e) {
            logger.error(String.format("Repository:  error: %s", e.getMessage()));
            throw new SQLException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }
    private void setPreparedStatementToCreateProject(PreparedStatement statement, Project project) throws SQLException {

        long nowTime = new Date().getTime();
        Timestamp created = new Timestamp(nowTime  );

        statement.setString(1, project.getName());
        statement.setString(2, project.getDescription());
        statement.setTimestamp(3, created);
        statement.setTimestamp(4, created);
        statement.setString(5, Arrays.toString(project.getImage()));
        statement.setObject(6, project.getAdminId(), Types.OTHER);
        statement.setInt(7, project.getProjectStatus().ordinal());
    }

    @Override
    public CompletableFuture<List<Project>> findByAdminIdAsync(UUID adminId) throws NoProjectsFoundException, RuntimeException, NullPointerException {
        Objects.requireNonNull(adminId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return findByAdminId(adminId);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private List<Project> findByAdminId(UUID adminId) throws SQLException, NoProjectsFoundException, RuntimeException {
        String queryString = sqlQueryPreparedStrings.findProjectsByAdminIdString(tableName);
        List<Project> projects = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             PreparedStatement statement = jdbcConnection.prepareStatement(queryString)) {
            statement.setObject(1, adminId, Types.OTHER);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Project project = mapResultSetToProject(resultSet);

                projects.add(project);
            }
            if(projects.size() == 0) {
                return Collections.emptyList();
            }

        } catch (Exception e) {
            logger.error(String.format("Repository: findByAdminIdAsync: \nerror: %s", e.getMessage()));
            throw new RuntimeException("Error finding projects by adminId: " + adminId, e);
        }
        return projects;
    }
/*
    @Override
    public CompletableFuture<List<Project>> findByUserIdAsync(UUID userId) throws NoUsersFoundException, RuntimeException, NullPointerException {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            if (userId == null) {
                return Collections.emptyList();
            }
            CompletableFuture<List<Project>> adminProjectsFuture = null;
            try {
                adminProjectsFuture = findByAdminIdAsync(userId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

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
        });
    }

 */
    @Override
    public CompletableFuture<List<Project>> findByUserIdAsync(UUID userId) throws NoUsersFoundException, RuntimeException, NullPointerException {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        return null;
    }

    private List<Project> findByUserId(UUID userId) throws SQLException, NoProjectsFoundException, RuntimeException {
        String projectUsersTableName = String.format("%s.%s", schema, projectUsersTable);
        String queryString = sqlQueryPreparedStrings.findProjectsByUserIdString(projectUsersTableName);
        List<Project> projects = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             PreparedStatement statement = jdbcConnection.prepareStatement(queryString)) {
            statement.setObject(1, userId, Types.OTHER);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Project project = mapResultSetToProject(resultSet);

                projects.add(project);
            }
            if(projects.size() == 0) {
                return Collections.emptyList();
            }

        } catch (Exception e) {
            logger.error(String.format("Repository: findByAdminIdAsync: \nerror: %s", e.getMessage()));
            throw new RuntimeException("Error finding projects by userId: " + userId, e);
        }
        return projects;
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
        });
        return  memberProjectsFuture;
    }

    @Override
    public CompletableFuture<Project> findByIdAsync(UUID id) throws SQLException {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return findById(id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });
    }
    private Project findById(UUID id) throws SQLException {
        String sql = sqlQueryPreparedStrings.findByIdString(tableName);

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             PreparedStatement statement = jdbcConnection.prepareStatement(sql)) {

            statement.setObject(1, id, Types.OTHER);
            ResultSet resultSet = statement.executeQuery();

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
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAsync(UUID id) throws SQLException {
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
        });
    }
/*
    @Override
    public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return null;
    }*/

    @Override
    public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException {

        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return findByIdAsync(projectId)
                .thenApply(ProjectMapper::toDto)
                .thenCompose(projectDto -> {
                    List<UUID> updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                    updatedUsers.add(userId);

                    try {
                        return projectUserRepository.addUserToProjectAsync(userId, projectId)
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
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<ProjectDto> RemoveUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException {
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

                    try {
                        return projectUserRepository.deleteUserFromProjectAsync(userId, projectId)
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
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<Project> updateAsync(Project project) throws SQLException, RuntimeException, ProjectNotFoundException, NullPointerException  {
        return CompletableFuture.supplyAsync(() -> {
            Objects.requireNonNull(project, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

            try {
                return update(project);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private Project update(Project project) throws SQLException, ProjectNotFoundException {
        String updateQuery = sqlQueryPreparedStrings.updateProjectByIdString(tableName);

        try (JdbcConnection jdbcConnection = new JdbcConnection();
        PreparedStatement statement = jdbcConnection.prepareStatement(updateQuery)) {
            setPreparedStatementToUpdateProject(statement, project);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                logger.error(String.format("Repository: update: error: Project with id %s not found", project.getId()));
                throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
            }
            return project;
        }
        catch (Exception e) {
            logger.error(String.format("Repository: update: error: %s", e.getMessage()));
            throw new SQLException(StaticConstants.DATA_NOT_FOUND_EXCEPTION_MESSAGE, e);
        }
    }
    private void setPreparedStatementToUpdateProject(PreparedStatement statement, Project project) throws SQLException {

        long updatedTime = project.getCreatedAt().getTime();
        Timestamp updated = new Timestamp(updatedTime);

        statement.setString(1, project.getName());
        statement.setString(2, project.getDescription());
        statement.setTimestamp(3, updated);
        statement.setBytes(4, project.getImage());
        statement.setInt(5, project.getProjectStatus().ordinal());
        statement.setObject(6, project.getId(), Types.OTHER);
    }


    @Override
    public CompletableFuture<List<Project>> findAllAsync() {
        return null;
    }
    @Override
    public CompletableFuture<List<Project>> findByProjectIdsAsync(List<UUID> projectIds) throws SQLException, RuntimeException  {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return findAllByIds(projectIds);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private List<Project> findAllByIds(List<UUID> userIds) throws SQLException, RuntimeException {
        String queryString = sqlQueryPreparedStrings.findAllByIdsString(tableName, userIds.size());

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.getConnection().prepareStatement(queryString)) {

            for (int i = 0; i < userIds.size(); i++) {
                statement.setObject(i + 1, userIds.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            List<Project> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(ProjectMapper.mapResultSetToProject(resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<List<Project>> findByAdminIdsAsync(List<UUID> adminIds) throws SQLException, RuntimeException  {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return findByAdminIds(adminIds);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private List<Project> findByAdminIds(List<UUID> adminIds) throws SQLException, RuntimeException {
        String queryString = sqlQueryPreparedStrings.findProjectsByAdminsIdsString(tableName, adminIds.size());

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.getConnection().prepareStatement(queryString)) {

            for (int i = 0; i < adminIds.size(); i++) {
                statement.setObject(i + 1, adminIds.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            List<Project> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(ProjectMapper.mapResultSetToProject(resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
