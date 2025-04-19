package repositories.synchronous;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.dtos.ProjectUsersDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.synchronous.ProjectUserRepositorySynchro;
import utils.StaticConstants;
import utils.exceptions.ProjectNotFoundException;
import utils.mappers.ProjectUserMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static utils.mappers.ProjectMapper.mapResultSetToProject;
import static utils.mappers.ProjectUserMapper.mapResultSetToProjectUser;
import static utils.mappers.UserMapper.mapResultSetToUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectUsersRepositorySynchronous implements ProjectUserRepositorySynchro {

    private final SqlQueryStrings sqlQueryStrings;

    String schema = System.getenv("JDBC_DEFAULT_SCHEMA") != null
            ? System.getenv("JDBC_DEFAULT_SCHEMA")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");

    String projectUsersTable = System.getenv("JDBC_PROJECT_USERS_TABLE") != null
            ? System.getenv("JDBC_PROJECT_USERS_TABLE")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");

    String tableName = String.format("%s.%s", schema, projectUsersTable);

    Logger logger = LoggerFactory.getLogger(ProjectUsersRepositorySynchronous.class);


    public ProjectUsersRepositorySynchronous() {
        this.sqlQueryStrings = new SqlQueryStrings();
    }

    @Override
    public boolean addUserToProject(UUID userId, UUID projectId) {

        String query = sqlQueryStrings.addUserIntoProjectString(
                tableName, projectId.toString(), userId.toString());

        try (JdbcConnection connection = new JdbcConnection();
             Statement statement = connection.statement()) {

            connection.setAutoCommit(false);
            try {
                int affected = statement.executeUpdate(query);
                if (affected == 0) {
                    logger.error("No rows affected");
                    throw new SQLDataException("No rows affected");
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                logger.error("Error adding user to project");
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error adding user to project", e.getMessage());
            throw new CompletionException("Database error", e);
        } catch (Exception e) {
            logger.error("Error adding user to project", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean deleteUserFromProject(UUID userId, UUID projectId) {

        String query = sqlQueryStrings.removeUserFromProjectString(
                tableName, projectId.toString(), userId.toString());

        try (JdbcConnection connection = new JdbcConnection();
             Statement statement = connection.statement()) {

            connection.setAutoCommit(false);
            try {
                int affected = statement.executeUpdate(query);
                if (affected == 0) {
                    logger.error("No rows affected");
                    throw new SQLDataException("No rows affected");
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                logger.error("Error deleting user from project");
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error deleting user from project", e.getMessage());
            throw new CompletionException("Database error", e);
        } catch (Exception e) {
            logger.error("Error deleting user from project", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public Optional<List<ProjectUsersDto>> findByUserId(UUID userId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        String queryString = sqlQueryStrings.findProjectUserByUserIdString(tableName, userId.toString());
            try (JdbcConnection conn = new JdbcConnection();
                 ResultSet rs = conn.executeQuery(queryString)) {

                List<ProjectUsersDto> projectUsers = new ArrayList<>();
                while (rs.next()) {
                    projectUsers.add(mapResultSetToProjectUser(rs));
                }
                return projectUsers.isEmpty() ? Optional.empty() : Optional.of(projectUsers);
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
    }

    public Optional<List<ProjectUsersDto>> findByProjectId(UUID projectId) {
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        String queryString = sqlQueryStrings.findProjectUserByProjectIdString(tableName, projectId.toString());

            try (JdbcConnection conn = new JdbcConnection();
                 ResultSet rs = conn.executeQuery(queryString)) {

                List<ProjectUsersDto> projectUsers = new ArrayList<>();
                while (rs.next()) {
                    projectUsers.add(mapResultSetToProjectUser(rs));
                }
                return projectUsers.isEmpty() ? Optional.empty() : Optional.of(projectUsers);
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
    }

    public Optional<List<ProjectUsersDto>> findByProjectIds(List<UUID> projectIds) {
        Objects.requireNonNull(projectIds, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        List<String > ids = projectIds.stream().map(UUID::toString).toList();

        String queryString = sqlQueryStrings.findAllByIdsString(tableName, ids);

        List<ProjectUsersDto> projectUsers = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            var resultSet = jdbcConnection.executeQuery(queryString);
            while (resultSet.next()) {
                ProjectUsersDto projectUsersDto = ProjectUserMapper.mapResultSetToProjectUser(resultSet);
                projectUsers.add(projectUsersDto);
            }
        }
        catch (Exception e) {
            throw new ProjectNotFoundException( StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, e);
        }
        return projectUsers.isEmpty() ? Optional.empty() : Optional.of(projectUsers);
    }

    public Optional<List<ProjectUsersDto>> findByUserIds(List<UUID> userIds) {
        Objects.requireNonNull(userIds, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        List<String > ids = userIds.stream().map(UUID::toString).toList();

        String queryString = sqlQueryStrings.findProjectUsersByUserIds(tableName, ids);

        List<ProjectUsersDto> projectUsers = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            var resultSet = jdbcConnection.executeQuery(queryString);
            while (resultSet.next()) {
                ProjectUsersDto projectUsersDto = ProjectUserMapper.mapResultSetToProjectUser(resultSet);
                projectUsers.add(projectUsersDto);
            }
        }
        catch (Exception e) {
            throw new ProjectNotFoundException( StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, e);
        }
        return projectUsers.isEmpty() ? Optional.empty() : Optional.of(projectUsers);
    }
}
