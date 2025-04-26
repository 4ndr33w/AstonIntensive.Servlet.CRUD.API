package services;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.ProjectRepository;
import repositories.ProjectUsersRepositoryImpl;
import repositories.UsersRepository;
import repositories.interfaces.ProjectUserRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.NoProjectsFoundException;
import utils.exceptions.ProjectNotFoundException;
import utils.exceptions.ProjectUpdateException;
import utils.mappers.ProjectMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsService implements ProjectService {

    Logger logger = LoggerFactory.getLogger(ProjectsService.class);
    private final repositories.interfaces.ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;

    public ProjectsService() {
        this.projectRepository = new ProjectRepository();
        this.userRepository = new UsersRepository();
        this.projectUserRepository = new ProjectUsersRepositoryImpl();
    }

    public ProjectsService(repositories.interfaces.ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = new UsersRepository();
        this.projectUserRepository = new ProjectUsersRepositoryImpl();
    }

    @Override
    public CompletableFuture<List<ProjectDto>> getByUserIdAsync(UUID userId) throws SQLException, NoProjectsFoundException, NullPointerException{
        if (userId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }
        return projectRepository.findByUserIdAsync(userId)
                .thenApply(projects -> {
                    if (projects == null) {
                        throw new NoProjectsFoundException(StaticConstants.PROJECTS_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    return projects.stream().map(ProjectMapper::toDto).toList();
                });/*
                .exceptionally(ex -> {
                    logger.error(String.format("Failed to load user projects for user ID: %s", userId));
                    return Collections.emptyList();
                });*/
    }

    @Override
    public CompletableFuture<List<ProjectDto>> getByAdminIdAsync(UUID adminId) throws SQLException, NoProjectsFoundException, NullPointerException {
        Objects.requireNonNull(adminId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.findByAdminIdAsync(adminId)
                .thenApply(projects -> {
                    if (projects == null) {
                          throw new NoProjectsFoundException(StaticConstants.PROJECTS_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    return projects.stream().map(ProjectMapper::toDto).toList();
                });
    }

    @Override
    public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException, DatabaseOperationException, NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.findByIdAsync(projectId)
                .thenCompose(project -> {
                    if (project == null) {
                        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    if (userId.equals(project.getAdminId())) {
                        logger.error(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE);
                        throw new IllegalArgumentException(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE);
                    }

                    return projectUserRepository.addUserToProject(userId, projectId)
                            .thenApply(success -> {
                                if (!success) {
                                    throw new DatabaseOperationException(StaticConstants.FAILED_TO_UPDATE_PROJECT_USERS_EXCEPTION_MESSAGE);
                                }
                                List<UserDto> updatedUsers = new ArrayList<>();

                                if(project.getProjectUsers() != null) {
                                    updatedUsers = new ArrayList<>(project.getProjectUsers());
                                }

                                UserDto newUserDto = new UserDto();
                                newUserDto.setId(userId);
                                //-----------------------------------------
                                    updatedUsers.add(newUserDto);
                                    project.setProjectUsers(updatedUsers.stream().toList());

                                return ProjectMapper.toDto(project);
                            });
                });
    }

    @Override
    public CompletableFuture<ProjectDto> removeUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException, DatabaseOperationException, NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.findByIdAsync(projectId)
                .thenCompose(project -> {
                    if (project == null) {
                        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    if (userId.equals(project.getAdminId())) {
                        return CompletableFuture.failedFuture(
                                new IllegalArgumentException(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE)
                        );
                    }
                    return projectUserRepository.deleteUserFromProject(userId, projectId)
                            .thenApply(success -> {
                                if (!success) {
                                    throw new DatabaseOperationException(StaticConstants.FAILED_TO_UPDATE_PROJECT_USERS_EXCEPTION_MESSAGE);
                                }
                                List<UserDto> updatedUsers = new ArrayList<>();

                                if (project.getProjectUsers() == null) {
                                    project.setProjectUsers(new ArrayList<>());
                                    return ProjectMapper.toDto(project);
                                }
                                else {
                                    updatedUsers = new ArrayList<>(project.getProjectUsers());
                                    var user = updatedUsers.stream().filter(userDto -> userDto.getId().equals(userId)).findFirst();
                                    updatedUsers.remove(user.get());

                                    project.setProjectUsers(updatedUsers);
                                    return ProjectMapper.toDto(project);
                                }
                            });
                });
    }

    @Override
    public CompletableFuture<ProjectDto> createAsync(Project project) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException {
        Objects.requireNonNull(project, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.createAsync(project)
                .thenApply(ProjectMapper::toDto);
    }

    @Override
    public CompletableFuture<ProjectDto> getByIdAsync(UUID id) throws SQLException, RuntimeException, ProjectNotFoundException {
        return projectRepository.findByIdAsync(id)
                .thenCompose(project -> {
                    if (project == null) {
                        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
                }
                    return CompletableFuture.completedFuture(ProjectMapper.toDto(project));
                    });
                /*.exceptionally(ex -> {
                    throw new RuntimeException("Service error");
                });*/
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) throws SQLException, NullPointerException {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.deleteAsync(id);
    }

    @Override
    public CompletableFuture<ProjectDto> updateByIdAsync(ProjectDto projectDto) throws SQLException, NullPointerException {
        Objects.requireNonNull(projectDto, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.updateAsync(ProjectMapper.mapToEntity(projectDto, List.of()))
                .thenApply(updatedProject -> {
                    if (updatedProject == null) {
                        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    return ProjectMapper.toDto(updatedProject);
                });
    }
}
