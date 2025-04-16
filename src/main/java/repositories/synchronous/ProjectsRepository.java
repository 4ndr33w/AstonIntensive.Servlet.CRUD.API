package repositories.synchronous;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.dtos.UserDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StaticConstants;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
    private static final String schema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String projectsTable = PropertiesConfiguration.getProperties().getProperty("jdbc.projects-table");
    private static final String projectUsersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");
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
                    logger.info("ProjectRepository: Create: Created project: {}", project);
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
            logger.error("ProjectRepository: findByAdminId: Failed to find projects. Query string: {}", queryString);
            String message = String.format("%s; adminId: %s", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, adminId);
            throw new RuntimeException(message , e);
        }

        logger.info("ProjectRepository: findByAdminId: Found projects: {}", projects);
        return Optional.of(projects);
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
            throw new RuntimeException(message , e);
        }

        return Optional.of(projects);
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

            // Позже всё равно смаппится в ProjectDto,
            // где хранятся только
            // UUID пользователей
            updatedUsers.add(userDto);

            var result = projectUsersRepository.addUserToProject(userId, projectId);
            if(result ) {
                logger.info("User: {} was added to project: {}", userId, projectId);
                project.setProjectUsers(updatedUsers);
            }
            logger.info("ProjectRepository: addUserToProject: Added user: {} to project: {}", userId, projectId);
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
                logger.info("User: {} was removed from project: {}", userId, projectId);
                updatedUsers.remove(userDto);
                project.setProjectUsers(updatedUsers);
            }
            logger.info("ProjectRepository: RemoveUserFromProject: Removed user: {} from project: {}", userId, projectId);
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
    public Optional<Project> update(Project item) {
        return null;
    }


}
