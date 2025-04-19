package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectUsersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.ProjectUserRepository;
import utils.StaticConstants;
import utils.mappers.ProjectUserMapper;
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
    private final SqlQueryStrings sqlQueryStrings;
    private static final ExecutorService dbExecutor;
    private static final String schema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String projectUsersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");
    String tableName = String.format("%s.%s", schema, projectUsersTable);

    static {
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    public ProjectUsersRepositoryImpl() {
        this.sqlQueryStrings = new SqlQueryStrings();
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
    public CompletableFuture<List<ProjectUsersDto>> findByUserId(UUID userId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        String queryString = sqlQueryStrings.findProjectUserByUserIdString(tableName, userId.toString());
        return CompletableFuture.supplyAsync(() -> {
            try (JdbcConnection conn = new JdbcConnection();
                 ResultSet rs = conn.executeQuery(queryString)) {

                List<ProjectUsersDto> projectUsers = new ArrayList<>();
                while (rs.next()) {
                    projectUsers.add(mapResultSetToProjectUser(rs));
                }
                return projectUsers;
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
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
    public CompletableFuture<List<ProjectUsersDto>> findByProjectId(UUID projectId) {
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        String queryString = sqlQueryStrings.findProjectUserByProjectIdString(tableName, projectId.toString());
        return CompletableFuture.supplyAsync(() -> {
            try (JdbcConnection conn = new JdbcConnection();
                 ResultSet rs = conn.executeQuery(queryString)) {

                List<ProjectUsersDto> projectUsers = new ArrayList<>();
                while (rs.next()) {
                    projectUsers.add(mapResultSetToProjectUser(rs));
                }
                return projectUsers;
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
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
    public CompletableFuture<Boolean> deleteUserFromProject(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {
            String tableName = String.format("%s.%s", schema, projectUsersTable);
            String query = sqlQueryStrings.removeUserFromProjectString(
                    tableName, projectId.toString(), userId.toString());

            try (JdbcConnection connection = new JdbcConnection();
                 Statement statement = connection.statement()) {

                connection.setAutoCommit(false);
                try {
                    int affected = statement.executeUpdate(query);
                    if (affected == 0) {
                        logger.warn("No rows affected");
                        throw new SQLDataException("No rows affected");
                    }
                    connection.commit();
                    logger.info("ProjectUserRepositoryImpl: Deleted {} users from {}", userId.toString(), projectId.toString());
                    return true;
                } catch (SQLException e) {
                    logger.error("ProjectUserRepositoryImpl: Failed to delete user from project", e);
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logger.error("ProjectUserRepositoryImpl: Failed to delete user from project", e);
                throw new CompletionException("Database error", e);
            } catch (Exception e) {
                logger.error("ProjectUserRepositoryImpl: Failed to delete user from project", e);
                throw new RuntimeException(e);
            }
        }, dbExecutor).handle((result, ex) -> {
            if (ex != null) {
                return false;
            }
            return result;
        });
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
    public CompletableFuture<Boolean> addUserToProject(UUID userId, UUID projectId) {
        return CompletableFuture.supplyAsync(() -> {
            String tableName = String.format("%s.%s", schema, projectUsersTable);
            String query = sqlQueryStrings.addUserIntoProjectString(
                    tableName, projectId.toString(), userId.toString());

            try (JdbcConnection connection = new JdbcConnection();
                 Statement statement = connection.statement()) {

                connection.setAutoCommit(false);
                try {
                    int affected = statement.executeUpdate(query);
                    if (affected == 0) {
                        logger.warn("No rows affected");
                        throw new SQLDataException("No rows affected");
                    }
                    connection.commit();
                    logger.info("ProjectUserRepositoryImpl: Added user {} to project {}", userId.toString(), projectId.toString());
                    return true;
                } catch (SQLException e) {
                    logger.error("ProjectUserRepositoryImpl: Failed to add user to project", e);
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logger.error("ProjectUserRepositoryImpl: Failed to add user to project", e);
                throw new CompletionException("Database error", e);
            } catch (Exception e) {
                logger.error("ProjectUserRepositoryImpl: Failed to add user to project", e);
                throw new RuntimeException(e);
            }
        }, dbExecutor).handle((result, ex) -> {
            if (ex != null) {
                return false;
            }
            return result;
        });
    }

    @Override
    public CompletableFuture<List<ProjectUsersDto>> findByProjectIds(List<UUID> projectIds) {
        return CompletableFuture.supplyAsync(() -> {
            if (projectIds == null || projectIds.isEmpty()) {
                return Collections.emptyList();
            }

            String sql = String.format("SELECT project_id, user_id FROM %s WHERE project_id IN (", tableName) +
                    projectIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

            try (JdbcConnection connection = new JdbcConnection();
                 PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {

                for (int i = 0; i < projectIds.size(); i++) {
                    statement.setObject(i + 1, projectIds.get(i));
                }

                ResultSet resultSet = statement.executeQuery();
                List<ProjectUsersDto> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(ProjectUserMapper.mapResultSetToProjectUser(resultSet));
                }

                return result;
            } catch (SQLException e) {
                throw new CompletionException("Failed to find project users by project ids", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }
}

