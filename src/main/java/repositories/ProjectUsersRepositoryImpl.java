package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfNonStatic;
import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectUsersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.ProjectUserRepository;
import utils.StaticConstants;
import utils.exceptions.ProjectUserNotFoundException;
import utils.mappers.ProjectUserMapper;
import utils.sqls.SqlQueryPreparedStrings;
import utils.sqls.SqlQueryStrings;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static utils.mappers.ProjectUserMapper.mapResultSetToProjectUser;

/**
 * Вспомогательный репозиторий для работы с
 * связями между проектами и пользователями.
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectUsersRepositoryImpl implements ProjectUserRepository {

    Logger logger = LoggerFactory.getLogger(ProjectUsersRepositoryImpl.class);
    private final SqlQueryPreparedStrings sqlQueryPreparedStrings;

    static String schema = System.getenv("JDBC_DEFAULT_SCHEMA") != null
            ? System.getenv("JDBC_DEFAULT_SCHEMA")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");

    static String projectUsersTable = System.getenv("JDBC_PROJECT_USERS_TABLE") != null
            ? System.getenv("JDBC_PROJECT_USERS_TABLE")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");

    String tableName = String.format("%s.%s", schema, projectUsersTable);

    public ProjectUsersRepositoryImpl() {
        sqlQueryPreparedStrings = new SqlQueryPreparedStrings();
    }

    /**
     * Ищет связи между проектами и
     * пользователями по id пользователя
     *
     * @param userId
     * @return {@code CompletableFuture<List<ProjectUsersDto>>}
     * @throws CompletionException
     * @throws NullPointerException
     */
    @Override
    public CompletableFuture<List<ProjectUsersDto>> findByUserIdAsync(UUID userId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            return findByUserId(userId);
        });
    }
    private List<ProjectUsersDto> findByUserId(UUID userId) {
        String queryString = sqlQueryPreparedStrings.findProjectUsersByUserId(tableName);

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.prepareStatement(queryString)) {
            statement.setObject(1, userId.toString(), Types.OTHER);
            ResultSet resultSet = statement.executeQuery();

            List<ProjectUsersDto> projectUsers = new ArrayList<>();
            while (resultSet.next()) {
                projectUsers.add(mapResultSetToProjectUser(resultSet));
            }
            return projectUsers;
        }
        catch (Exception e) {
            throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Ищет связи между проектами и
     * пользователями по id проекта
     *
     * @param projectId
     * @return {@code CompletableFuture<List<ProjectUsersDto>>}
     * @throws CompletionException
     * @throws NullPointerException
     */
    @Override
    public CompletableFuture<List<ProjectUsersDto>> findByProjectIdAsync(UUID projectId) {
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            return findByProjectId(projectId);
        });
    }
    private List<ProjectUsersDto> findByProjectId(UUID projectId) {
        String queryString = sqlQueryPreparedStrings.findProjectUsersByProjectId(tableName);

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.prepareStatement(queryString)) {
            statement.setObject(1, projectId.toString(), Types.OTHER);
            ResultSet resultSet = statement.executeQuery();

            List<ProjectUsersDto> projectUsers = new ArrayList<>();
            while (resultSet.next()) {
                projectUsers.add(mapResultSetToProjectUser(resultSet));
            }
            return projectUsers;
        }
        catch (Exception e) {
            throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Метод удаляет связи между проектами и пользователями
     * по id пользователя и id проекта
     *
     * @param userId
     * @param projectId
     * @return {@code CompletableFuture<Boolean>}
     * @throws CompletionException
     * @throws NullPointerException
     * @throws RuntimeException
     * @throws SQLDataException
     */
    @Override
    public CompletableFuture<Boolean> deleteUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException, RuntimeException, NullPointerException, ProjectUserNotFoundException  {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return deleteUserFromProject(userId, projectId);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private boolean deleteUserFromProject(UUID userId, UUID projectId) throws SQLException, RuntimeException {
        String query = sqlQueryPreparedStrings.removeProjectUser(tableName);

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setObject(1, projectId.toString(), Types.OTHER);
            statement.setObject(2, userId.toString(), Types.OTHER);

            int affected = statement.executeUpdate();
            if(affected == 0) throw new ProjectUserNotFoundException(StaticConstants.PROJECT_USER_NOT_FOUND_EXCEPTION_MESSAGE);
            return affected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *Метод добавляет связи
     * между проектами и пользователями
     * по id пользователя и id проекта
     * @param userId
     * @param projectId
     * @return {@code CompletableFuture<Boolean>}
     * @throws CompletionException
     * @throws NullPointerException
     * @throws RuntimeException
     * @throws SQLDataException
     */
    @Override
    public CompletableFuture<Boolean> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException, RuntimeException, NullPointerException  {
        return CompletableFuture.supplyAsync(() -> {

            try {
                return addUserToProject(userId, projectId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private boolean addUserToProject(UUID userId, UUID projectId) throws SQLException, RuntimeException {
        String query = sqlQueryPreparedStrings.addProjectUser(tableName);

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setObject(1, projectId.toString(), Types.OTHER);
            statement.setObject(2, userId.toString(), Types.OTHER);

            int affected = statement.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<List<ProjectUsersDto>> findByProjectIdsAsync(List<UUID> projectIds) throws SQLException, RuntimeException, NullPointerException  {
        return CompletableFuture.supplyAsync(() -> {
            if (projectIds == null || projectIds.isEmpty()) {
                return Collections.emptyList();
            }
            try {
                return findByProjectIds(projectIds);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private List<ProjectUsersDto> findByProjectIds(List<UUID> projectIds) throws SQLException, RuntimeException, NullPointerException  {
        String queryString = sqlQueryPreparedStrings.findProjectUsersByProjectIds(tableName, projectIds.size());

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.getConnection().prepareStatement(queryString)) {

            for (int i = 0; i < projectIds.size(); i++) {
                statement.setObject(i + 1, projectIds.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            List<ProjectUsersDto> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(ProjectUserMapper.mapResultSetToProjectUser(resultSet));
            }
            return result;
        }
    }

    @Override
    public CompletableFuture<List<ProjectUsersDto>> findByUserIdsAsync(List<UUID> userIds) throws SQLException, RuntimeException, NullPointerException, ProjectUserNotFoundException  {
        return CompletableFuture.supplyAsync(() -> {
            if (userIds == null || userIds.isEmpty()) {
                return Collections.emptyList();
            }
            try {
                return findByUserIds(userIds);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private List<ProjectUsersDto> findByUserIds(List<UUID> userIds) throws SQLException, RuntimeException, NullPointerException, ProjectUserNotFoundException  {
        String queryString = sqlQueryPreparedStrings.findProjectUsersByUserIds(tableName, userIds.size());

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.getConnection().prepareStatement(queryString)) {

            for (int i = 0; i < userIds.size(); i++) {
                statement.setObject(i + 1, userIds.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            List<ProjectUsersDto> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(ProjectUserMapper.mapResultSetToProjectUser(resultSet));
            }
            return result;
        }
    }
}

