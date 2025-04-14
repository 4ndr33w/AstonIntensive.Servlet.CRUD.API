package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectUsersDto;
import models.entities.User;
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
import static utils.mappers.UserMapper.mapResultSetToUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectUsersRepository {

    private final SqlQueryStrings sqlQueryStrings;
    private static final ExecutorService dbExecutor;
    private static final String schema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String projectUsersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");
    String tableName = String.format("%s.%s", schema, projectUsersTable);

    static {
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    public ProjectUsersRepository() {
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

    public CompletableFuture<ProjectUsersDto> addUserToProject(UUID userId, UUID projectId) {
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

                    // Возвращаем DTO с информацией о добавленной связи
                    return new ProjectUsersDto(projectId, userId);

                } catch (SQLException e) {
                    connection.rollback();
                    throw new CompletionException("Failed to add user to project", e);
                }
            } catch (Exception e) {
                throw new CompletionException("Database connection error", e);
            }
        }, dbExecutor);

    }

    public CompletableFuture<List<ProjectUsersDto>> findByProjectIds(List<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.supplyAsync(() -> {
            String tableName = String.format("%s.%s", schema, projectUsersTable);
            String query = sqlQueryStrings.findByProjectIdsString(tableName, projectIds);

            try (JdbcConnection connection = new JdbcConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                // Устанавливаем параметры для IN-условия
                for (int i = 0; i < projectIds.size(); i++) {
                    statement.setObject(i + 1, projectIds.get(i));
                }

                try (ResultSet rs = statement.executeQuery()) {
                    List<ProjectUsersDto> results = new ArrayList<>();

                    while (rs.next()) {
                        results.add(mapResultSetToProjectUser(rs));
                    }

                    return results;
                }
            } catch (Exception e) {
                throw new CompletionException("Failed to load project-user relations", e);
            }
        }, dbExecutor);
    }


}

