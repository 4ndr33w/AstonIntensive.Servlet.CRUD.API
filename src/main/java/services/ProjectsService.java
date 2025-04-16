package services;

import configurations.ThreadPoolConfiguration;
import models.dtos.UserDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.ProjectRepositoryNew;
import repositories.ProjectUsersRepositoryImpl;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.ProjectUserRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;
import utils.StaticConstants;
import utils.exceptions.ProjectNotFoundException;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsService implements ProjectService {

    Logger logger = LoggerFactory.getLogger(ProjectsService.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;
    private static final ExecutorService dbExecutor;

    public ProjectsService() {
        this.projectRepository = new ProjectRepositoryNew();
        this.userRepository = new UsersRepositoryImplementation();
        this.projectUserRepository = new ProjectUsersRepositoryImpl();
    }

    public ProjectsService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = new UsersRepositoryImplementation();
        this.projectUserRepository = new ProjectUsersRepositoryImpl();
    }
    static {
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    @Override
    public CompletableFuture<List<Project>> getByUserIdAsync(UUID userId) {
        if (userId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }

        return projectRepository.findByUserIdAsync(userId)
                .thenApply(projects -> {
                    if (projects == null) {
                        return new ArrayList<Project>();
                    }
                    return projects;
                })
                .exceptionally(ex -> {
                    System.out.println(String.format("Failed to load user projects for user ID: %s", userId));
                    return Collections.emptyList();
                });
    }

    @Override
    public CompletableFuture<List<Project>> getByAdminIdAsync(UUID adminId) {
        // 1. Валидация входного параметра
        if (adminId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }

        // 2. Асинхронный запрос к репозиторию
        return projectRepository.findByAdminIdAsync(adminId)
                .thenApply(projects -> {
                    if (projects == null) {
                        return new ArrayList<Project>();
                    }
                    return projects;
                })
                .exceptionally(ex -> {
                    System.out.println(String.format("Failed to load user projects for user ID: %s", adminId));
                    return Collections.emptyList(); // Или можно пробросить исключение дальше
                });
    }

    @Override
    public CompletableFuture<Project> addUserToProjectAsync(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.findByIdAsync(projectId)
                .thenCompose(project -> {
                    if (project == null) {
                        return CompletableFuture.failedFuture(

                                new NoSuchElementException(String.format("$s: %s", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, projectId))
                        );
                    }
                    if (userId.equals(project.getAdminId())) {
                        logger.error(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE);
                        return CompletableFuture.failedFuture(
                                new IllegalArgumentException(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE)
                        );
                    }

                    return projectUserRepository.addUserToProject(userId, projectId)
                            .thenApply(success -> {
                                if (!success) {
                                    throw new CompletionException(
                                            new SQLException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE)
                                    );
                                }
                                List<UserDto> updatedUsers = new ArrayList<>();

                                if(project.getProjectUsers() != null) {
                                    updatedUsers = new ArrayList<>(project.getProjectUsers());
                                }
                                //-----------------------------------------
                                // Так как в данном случае позже в ProjectDto
                                // у нас List<UserDto> будет урезан до
                                // List<UUID> userIds, то в данной ситуации
                                // считаю это допустимым решением
                                UserDto newUserDto = new UserDto();
                                newUserDto.setId(userId);
                                //-----------------------------------------
                                    updatedUsers.add(newUserDto);
                                    project.setProjectUsers(updatedUsers.stream().toList());

                                return project;
                            });
                });
    }

    @Override
    public CompletableFuture<Project> removeUserFromProjectAsync(UUID userId, UUID projectId) {
        if (userId == null || projectId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE)
            );
        }

        return projectRepository.findByIdAsync(projectId)
                .thenCompose(project -> {
                    if (project == null) {
                        return CompletableFuture.failedFuture(
                                new NoSuchElementException("Project not found with ID: " + projectId)
                        );
                    }

                    if (userId.equals(project.getAdminId())) {
                        return CompletableFuture.failedFuture(
                                new IllegalArgumentException(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE)
                        );
                    }

                    return projectUserRepository.deleteUserFromProject(userId, projectId)
                            .thenApply(success -> {
                                if (!success) {
                                    throw new CompletionException(
                                            new SQLException("Failed to remove user from project in database")
                                    );
                                }

                                List<UserDto> updatedUsers = new ArrayList<>();

                                if (project.getProjectUsers() == null) {

                                    project.setProjectUsers(new ArrayList<>());

                                    return project;
                                }
                                else {
                                    updatedUsers = new ArrayList<>(project.getProjectUsers());
                                    var user = updatedUsers.stream().filter(userDto -> userDto.getId().equals(userId)).findFirst();
                                    updatedUsers.remove(user.get());

                                    project.setProjectUsers(updatedUsers);
                                    return project;
                                }
                            });
                });
    }

    @Override
    public CompletableFuture<Project> createAsync(Project project) {
        Objects.requireNonNull(project, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        CompletableFuture<Project> projectFuture = projectRepository.createAsync(project);

        return projectFuture
                .exceptionally(ex -> {
                    throw new RuntimeException("Error creating project by id");
                });
    }

    @Override
    public CompletableFuture<Project> getByIdAsync(UUID id) {
        return projectRepository.findByIdAsync(id)
                .thenCompose(project -> {
                    if (project == null) {
                        return CompletableFuture.completedFuture(null);
                }
                    return CompletableFuture.completedFuture(project);
                    })
                .exceptionally(ex -> {
                    throw new RuntimeException("Error fetching project by id");
                });
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.deleteAsync(id)
                .thenApply(deleted -> {
                    if (!deleted) {
                        return false;
                    }
                    return true;
                })
                .exceptionally(ex -> {
                    throw new RuntimeException("Error deleting project by id");
                });
    }

    @Override
    public CompletableFuture<Project> updateByIdAsync(Project project) {
        Objects.requireNonNull(project, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.updateAsync(project)
                .thenApply(updatedProject -> {
                    if (updatedProject == null) {
                        throw new CompletionException(
                                new ProjectNotFoundException("Project with id " + project.getId() + " not found"));
                    }
                    return updatedProject;
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof NoSuchElementException) {
                        logger.error("Project with id {} not found", project.getId(), ex.getCause());
                        throw new CompletionException(
                                new ProjectNotFoundException(String.format("%s; id: %s; %s", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, project.getId(), ex.getCause())));
                    }
                    logger.error("Failed to update project with id: {}", project.getId(), ex);
                    throw new CompletionException("Failed to update project", ex.getCause());
                });
    }
}
