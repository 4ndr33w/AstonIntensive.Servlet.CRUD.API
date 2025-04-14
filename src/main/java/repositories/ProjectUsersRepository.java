package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectUsersDto;
import models.entities.User;
import utils.StaticConstants;
import utils.sqls.SqlQueryStrings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

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
}

