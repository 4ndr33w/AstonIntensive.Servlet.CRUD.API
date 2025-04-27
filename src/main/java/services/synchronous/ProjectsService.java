package services.synchronous;

import configurations.JdbcConnection;
import models.dtos.ProjectUsersDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*import repositories.interfaces.synchronous.ProjectUserRepositorySynchro;
import repositories.synchronous.ProjectUsersRepositorySynchronous;
import repositories.synchronous.UsersRepositorySynchronous;*/
import services.interfaces.synchronous.ProjectServiceSynchro;
import utils.StaticConstants;
import utils.exceptions.ProjectNotFoundException;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author 4ndr33w
 * @version 1.0
 *//*
public class ProjectsService implements ProjectServiceSynchro {

    Logger logger = LoggerFactory.getLogger(ProjectsService.class);
    private final repositories.interfaces.synchronous.ProjectRepoSynchro projectRepository;
    private final UsersRepositorySynchronous userRepository;
    private final ProjectUserRepositorySynchro projectUsersRepository;

    public ProjectsService() {
        this.projectRepository = new repositories.synchronous.ProjectsRepository();
        this.userRepository = new UsersRepositorySynchronous();
        this.projectUsersRepository = new ProjectUsersRepositorySynchronous();
    }

    public ProjectsService(
            repositories.interfaces.synchronous.ProjectRepoSynchro projectRepository, ProjectUserRepositorySynchro projectUsersRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = new UsersRepositorySynchronous();
        this.projectUsersRepository = projectUsersRepository;
    }

    @Override
    public List<Project> getByUserId(UUID userId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        var projectUsersOptional = projectUsersRepository.findByUserId(userId);
        if(projectUsersOptional.isPresent()) {
            var projectUsers = projectUsersOptional.get();
            var projectIds = projectUsers.stream().map(ProjectUsersDto::getProjectId).toList();
            var projectsOptional = projectRepository.findByProjectIds(projectIds);

            if(projectsOptional.isPresent()) {
                return projectsOptional.get().stream()
                        .map(this::compileProjectWithUsers)
                        .collect(Collectors.toList());
            }
            throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        }
        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Override
    public List<Project> getByAdminId(UUID adminId) {
        Objects.requireNonNull(adminId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        Optional<List<Project>> projectsOpt = projectRepository.findByAdminId(adminId);
        if (projectsOpt.isEmpty() || projectsOpt.get().isEmpty()) {
            logger.error("ProjectService: Projects not found for admin with id: {}", adminId);
            throw new ProjectNotFoundException(StaticConstants.NO_PROJECTS_FOUND_BY_ADMIN_ID_EXCEPTION_MESSAGE);
        }

        List<Project> projects = projectsOpt.get();
        return compileMultipleProjectsWithUsers(projects);
    }

    @Override
    public Project create(Project entity) throws SQLException {
        Objects.requireNonNull(entity, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.create(entity);
    }

    @Override
    public Project getById(UUID id) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        if(projectOpt.isPresent()) {
            return compileProjectWithUsers(projectOpt.get());
        }
        else throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean deleteById(UUID id) throws CompletionException, SQLException {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        return projectRepository.delete(id);
    }

    @Override
    public Project updateById(Project entity) throws SQLException {
        Objects.requireNonNull(entity, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        return projectRepository.update(entity);
    }

    @Override
    public Project addUserToProject(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        var projectOptional = projectRepository.findById(projectId);

        if(projectOptional.isPresent()) {
            var project = projectOptional.get();

            var projectWithUsers =  compileProjectWithUsers(project);

            try (JdbcConnection connection = new JdbcConnection()) {

                var result = projectUsersRepository.addUserToProject(userId, projectId);

                if(result) {
                    var projectUsers = projectWithUsers.getProjectUsers();
                    var newUser = new UserDto();
                    newUser.setId(userId);
                    if(projectUsers == null) {
                        projectUsers = new ArrayList<>();
                    }
                    projectUsers.add(newUser);
                    project.setProjectUsers(projectUsers);

                    return project;
                }
               else throw new RuntimeException("Error adding user to project");

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Override
    public Project removeUserFromProject(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        var projectOptional = projectRepository.findById(projectId);

        if(projectOptional.isPresent()) {
            var project = projectOptional.get();

            var projectWithUsers =  compileProjectWithUsers(project);

            try (JdbcConnection connection = new JdbcConnection()) {

                var result = projectUsersRepository.deleteUserFromProject(userId, projectId);

                if(result) {
                    var projectUsers = projectWithUsers.getProjectUsers();
                    var newUser = new UserDto();
                    newUser.setId(userId);
                    if(projectUsers == null) {
                        projectUsers = new ArrayList<>();
                    }
                    var existingProjectUser = projectUsers.stream().filter(user -> user.getId().equals(userId)).findFirst().orElse(null);

                    if(existingProjectUser != null) {
                        projectUsers.remove(existingProjectUser);
                    }
                    project.setProjectUsers(projectUsers);

                    return project;
                }
                else throw new RuntimeException("Error adding user to project");

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
    }


    private List<ProjectUsersDto> getProjectUsersByProjectId(UUID projectId) {
        var projectUsersOptional = projectUsersRepository.findByProjectId(projectId);
        if (projectUsersOptional.isEmpty()) {
            return Collections.emptyList();
        }
        return projectUsersOptional.get();
   }

    private List<UserDto> getUsersByIds(List<UUID> userIds) {
        Objects.requireNonNull(userIds, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.findAllByIds(userIds)
                .get()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    private Project compileProjectWithUsers(Project project) {

        var projectUsers = getProjectUsersByProjectId(project.getId());
        if(projectUsers.size() > 0) {
            var users = getUsersByIds(projectUsers
                    .stream()
                    .map(ProjectUsersDto::getUserId)
                    .toList());
            return new Project(project, users);
        }
        return project;
    }

    private List<Project> compileMultipleProjectsWithUsers(List<Project> projects) {
        return projects.stream()
                .map(this::compileProjectWithUsers)
                .collect(Collectors.toList());
    }
}
*/