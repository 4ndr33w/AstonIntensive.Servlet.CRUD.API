package repositories.synchronous;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.dtos.UserDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.ProjectNotFoundException;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CompletionException;

import static utils.mappers.ProjectMapper.mapResultSetToProject;
import static utils.mappers.ProjectMapper.mapResultSetToProjectOptional;

/**
 * Реализация интерфейса {@code ProjectRepoSynchro} для CRUD операций для работы
 * с репозиторием проектов в синхронном режиме
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsRepository implements repositories.interfaces.synchronous.ProjectRepoSynchro {

    Logger logger = LoggerFactory.getLogger(ProjectsRepository.class);

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
    String projectUsersTableName = String.format("%s.%s", schema, projectUsersTable);

    ProjectUsersRepositorySynchronous projectUsersRepository = new  ProjectUsersRepositorySynchronous();

    private final SqlQueryStrings sqlQueryStrings;

    public ProjectsRepository() {
        this.sqlQueryStrings = new SqlQueryStrings();
    }

    @Override
    public Project create(Project project) {
        Objects.requireNonNull(project);

        String queryString = sqlQueryStrings.createProjectString(tableName, project);
        try (JdbcConnection jdbcConnection = new JdbcConnection();
             Statement statement = jdbcConnection.statement()) {

            int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

            if (affectedRows == 0) {
                logger.error("ProjectRepository: Create: Failed to create a project. Query string: {}", queryString);
                throw new RuntimeException(StaticConstants.ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE);
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    project.setId((UUID) generatedKeys.getObject(1));
                    return project;
                }
                logger.error("ProjectRepository: Create: Failed to create a project. Query string: {}", queryString);
                throw new RuntimeException(StaticConstants.FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE);
            }
        }
        catch (Exception e) {
            logger.error("ProjectRepository: Create: Failed to create a project. Query string: {}", queryString);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Project> findById(UUID id) {
        String sql = sqlQueryStrings.findByIdString(tableName, id.toString());

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             ResultSet resultSet = jdbcConnection.executeQuery(sql)) {

            if (!resultSet.next()) {
                return null;
            }

            return mapResultSetToProjectOptional(resultSet);

        } catch (SQLException e) {
            String message = String.format("%s; id: %s", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, id);
            throw new RuntimeException(message, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        String tableName = String.format("%s.%s", schema, projectsTable);
        String queryString = sqlQueryStrings.deleteByIdString(tableName, id.toString());

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            int affectedRows = jdbcConnection.executeUpdate(queryString);
            return affectedRows > 0;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<List<Project>> findByAdminId(UUID adminId) {
        String queryString = sqlQueryStrings.findProjectsByAdminIdString(tableName, adminId.toString());
        List<Project> projects = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             var resultSet = jdbcConnection.executeQuery(queryString)) {

            while (resultSet.next()) {
                Project project = mapResultSetToProject(resultSet);

                projects.add(project);
            }

        } catch (Exception e) {
            logger.error(String.format("%s; adminId: %s", StaticConstants.NO_PROJECTS_FOUND_BY_ADMIN_ID_EXCEPTION_MESSAGE, adminId));
            String message = String.format("%s; adminId: %s", StaticConstants.NO_PROJECTS_FOUND_BY_ADMIN_ID_EXCEPTION_MESSAGE, adminId);
            throw new ProjectNotFoundException(message , e);
        }
        return projects.isEmpty() ? Optional.empty() : Optional.of(projects);
    }

    @Override
    public Optional<List<Project>> findByUserId(UUID userId) {
        String query = sqlQueryStrings.findAllProjectsByUserId(tableName, projectUsersTableName, userId.toString());
        List<Project> projects = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             var resultSet = jdbcConnection.executeQuery(query)) {

            while (resultSet.next()) {
                Project project = mapResultSetToProject(resultSet);

                projects.add(project);
            }
        } catch (Exception e) {
            String message = String.format("%s; userId: %s", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, userId);
            throw new ProjectNotFoundException(message , e);
        }
        return projects.isEmpty() ? Optional.empty() : Optional.of(projects);
    }

    @Override
    public Optional<List<Project>> findByUserIds(List<UUID> userIds) {
        Objects.requireNonNull(userIds, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        List<String> ids = userIds.stream().map(UUID::toString).toList();

        var string = sqlQueryStrings.findAllByIdsString(tableName, ids);
        List<Project> projects = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            var resultSet = jdbcConnection.executeQuery(string);
            while (resultSet.next()) {
                Project project = mapResultSetToProject(resultSet);
                projects.add(project);
            }
        }
        catch (Exception e) {
            throw new ProjectNotFoundException( StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, e);
        }
        return projects.isEmpty() ? Optional.empty() : Optional.of(projects);
    }

    @Override
    public Optional<List<Project>> findByProjectIds(List<UUID> projectIds) {
        Objects.requireNonNull(projectIds, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        List<String> ids = projectIds.stream().map(UUID::toString).toList();

        var string = sqlQueryStrings.findAllByIdsString(tableName, ids);
        List<Project> projects = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            var resultSet = jdbcConnection.executeQuery(string);
            while (resultSet.next()) {
                Project project = mapResultSetToProject(resultSet);
                projects.add(project);
            }
        }
        catch (Exception e) {
            throw new ProjectNotFoundException( StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, e);
        }
        return projects.isEmpty() ? Optional.empty() : Optional.of(projects);
    }

    @Override
    public Optional<List<Project>> findByAdminIds(List<UUID> adminIds) {
        Objects.requireNonNull(adminIds, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        List<String> ids = adminIds.stream().map(UUID::toString).toList();

        var string = sqlQueryStrings.findAllProjectsByAdminIds(tableName, ids);
        List<Project> projects = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            var resultSet = jdbcConnection.executeQuery(string);
            while (resultSet.next()) {
                Project project = mapResultSetToProject(resultSet);
                projects.add(project);
            }
        }
        catch (Exception e) {
            throw new ProjectNotFoundException( StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, e);
        }
        return projects.isEmpty() ? Optional.empty() : Optional.of(projects);
    }

    @Override
    public Project  addUserToProject(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        var projectOptional = findById(projectId);
        if(projectOptional.isPresent()) {
            var project = projectOptional.get();

            List<UserDto> updatedUsers = new ArrayList<>(project.getProjectUsers());
            UserDto userDto = new UserDto();
            userDto.setId(userId);

            updatedUsers.add(userDto);

            var result = projectUsersRepository.addUserToProject(userId, projectId);
            if(result ) {
                project.setProjectUsers(updatedUsers);
            }
            return project;
        }
        logger.error("ProjectRepository: addUserToProject: Optional.isEmpty()\n Failed to add user: {} to project: {}", userId, projectId);
        return null;
    }

    @Override
    public Project RemoveUserFromProject(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        var projectOptional = findById(projectId);
        if(projectOptional.isPresent()) {
            var project = projectOptional.get();

            List<UserDto> updatedUsers = new ArrayList<>(project.getProjectUsers());
            UserDto userDto = updatedUsers.stream().filter(u -> u.getId().equals(userId)).findFirst().get();

            var result = projectUsersRepository.deleteUserFromProject(userId, projectId);
            if(result) {
                updatedUsers.remove(userDto);
                project.setProjectUsers(updatedUsers);
            }
            return project;
        }
        logger.error("ProjectRepository: RemoveUserFromProject: Optional.isEmpty()\n Failed to remove user: {} from project: {}", userId, projectId);
        return null;
    }

    @Override
    public Optional<List<Project>> findAll() {
        return Optional.of(List.of());
    }


    @Override
    public Project update(Project project) {
        Objects.requireNonNull(project, "Project project cannot be null");

        String updateQuery = sqlQueryStrings.updateProjectByIdString(
                tableName, project.getId().toString(), project);

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            jdbcConnection.setAutoCommit(false);

            int affectedRows = jdbcConnection.executeUpdate(updateQuery);

            if (affectedRows == 0) {
                logger.error(String.format("Repository: update: error: Project with id %s not found", project.getId()));
                throw new DatabaseOperationException(StaticConstants.DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE);
            }
            jdbcConnection.commit();
            return project;
        }
        catch (Exception e) {
            logger.error(String.format("Repository: update: error: %s", e.getMessage()));
            throw new CompletionException("Unexpected error", e);
        }
    }


}
