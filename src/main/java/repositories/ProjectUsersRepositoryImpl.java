package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectUsersDto;
import repositories.interfaces.ProjectUserRepository;
import utils.StaticConstants;
import utils.sqls.SqlQueryStrings;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static utils.mappers.ProjectUserMapper.mapResultSetToProjectUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectUsersRepositoryImpl implements ProjectUserRepository {

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

    public CompletableFuture<List<ProjectUsersDto>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            try (JdbcConnection conn = new JdbcConnection();
                 PreparedStatement stmt = conn.prepareStatement(tableName);
                 ResultSet rs = stmt.executeQuery()) {

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

    @Override
    public CompletableFuture<List<ProjectUsersDto>> findByUserId(UUID userId) {
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

    @Override
    public CompletableFuture<List<ProjectUsersDto>> findByProjectId(UUID projectId) {
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

    @Override
    public CompletableFuture<Boolean> deleteUserFromProject(UUID userId, UUID projectId) {
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
        }, dbExecutor).handle((result, ex) -> {
            if (ex != null) {
                return false;
            }
            return result;
        });
    }

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

                // Устанавливаем параметры для IN-условия
                for (int i = 0; i < projectIds.size(); i++) {
                    statement.setObject(i + 1, projectIds.get(i));
                }

                ResultSet resultSet = statement.executeQuery();
                List<ProjectUsersDto> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(new ProjectUsersDto(
                            (UUID) resultSet.getObject("project_id"),
                            (UUID) resultSet.getObject("user_id")
                    ));
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

