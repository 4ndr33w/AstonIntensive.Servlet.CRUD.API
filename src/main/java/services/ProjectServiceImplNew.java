package services;

import models.dtos.ProjectUsersDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import repositories.ProjectUsersRepositoryImpl;
import repositories.ProjectsRepositoryImplementation;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.ProjectUserRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;
import utils.StaticConstants;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectServiceImplNew implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;

    public ProjectServiceImplNew() {
        this.projectRepository = new ProjectsRepositoryImplementation();
        this.userRepository = new UsersRepositoryImplementation();
        this.projectUserRepository = new ProjectUsersRepositoryImpl();
    }

    @Override
    public CompletableFuture<Project> createAsync(Project project) {
        Objects.requireNonNull(project, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        CompletableFuture<Project> projectFuture = projectRepository.createAsync(project);

        return projectFuture
                .exceptionally(ex -> {
                    throw new CompletionException(ex.getCause() != null ? ex.getCause() : ex);
                });
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.deleteAsync(id)
                .thenApply(deleted -> deleted)

                .exceptionally(ex -> {
                    if (ex.getCause() instanceof SQLException) {
                        throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, ex.getCause());
                    } else {
                        throw new CompletionException(ex);
                    }});
    }

    @Override
    public CompletableFuture<Project> getByIdAsync(UUID id) {
        return projectRepository.findByIdAsync(id)
                .thenCompose(project -> {
                    if (project == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return fillProjectUsers(project);
                })
                .exceptionally(ex -> {
                    throw new CompletionException("Failed to get project by id: " + id, ex);
                });
    }

    public CompletableFuture<Project> fillProjectUsers(Project project) {
        return projectUserRepository.findByProjectId(project.getId())
                .thenCompose(projectUsersDtos -> {
                    if (projectUsersDtos.isEmpty()) {
                        project.setProjectUsers(Collections.emptyList());
                        return CompletableFuture.completedFuture(project);
                    }

                    List<CompletableFuture<User>> userFutures = projectUsersDtos.stream()
                            .map(dto -> {
                                try {
                                    return userRepository.findByIdAsync(dto.getUserId());
                                }
                                catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .toList();

                    return CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                List<UserDto> users = userFutures.stream()
                                        .map(CompletableFuture::join)
                                        .filter(Objects::nonNull)
                                        .map(UserMapper::toDto)
                                        .collect(Collectors.toList());

                                project.setProjectUsers(users);
                                return project;
                            });
                });
    }







    @Override
    public CompletableFuture<List<Project>> getByUserIdAsync(UUID userId) {
        return projectRepository.findByUserIdAsync(userId)
                .thenCompose(projects -> {
                    if (projects == null || projects.isEmpty()) {
                        return CompletableFuture.completedFuture(Collections.emptyList());
                    }
                    return fillProjectsWithUsers(projects);
                })
                .exceptionally(ex -> {
                    throw new CompletionException("Failed to get projects by user id: " + userId, ex);
                });
    }

    @Override
    public CompletableFuture<List<Project>> getByAdminIdAsync(UUID adminId) {
        if (adminId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }
        return projectRepository.findByAdminIdAsync(adminId)
                .thenCompose(projects -> {
                    if (projects == null || projects.isEmpty()) {
                        return CompletableFuture.completedFuture(Collections.emptyList());
                    }
                    return fillProjectsWithUsers(projects);
                })
                .exceptionally(ex -> {
                    throw new CompletionException("Failed to get projects by admin id: " + adminId, ex);
                });
    }

    public CompletableFuture<List<Project>> fillProjectsWithUsers(List<Project> projects) {
        // Собираем все ID проектов для batch-запроса
        List<UUID> projectIds = projects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        // Получаем всех пользователей для всех проектов одним запросом
        return projectUserRepository.findByProjectIds(projectIds)
                .thenCompose(projectUsersMap -> {
                    // Группируем пользователей по projectId
                    Map<UUID, List<UUID>> usersByProjectId = projectUsersMap.stream()
                            .collect(Collectors.groupingBy(
                                    ProjectUsersDto::getProjectId,
                                    Collectors.mapping(ProjectUsersDto::getUserId, Collectors.toList())
                            ));

                    // Для каждого проекта получаем данные пользователей
                    List<CompletableFuture<Project>> projectFutures = projects.stream()
                            .map(project -> {
                                List<UUID> userIds = usersByProjectId.getOrDefault(project.getId(), Collections.emptyList());
                                return fillUsersForProject(project, userIds);
                            })
                            .collect(Collectors.toList());

                    return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> projectFutures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList()));
                });
    }

    // Протестировано, работает.
    public CompletableFuture<Project> fillUsersForProject(Project project, List<UUID> userIds) {
        if (userIds.isEmpty()) {
            project.setProjectUsers(Collections.emptyList());
            return CompletableFuture.completedFuture(project);
        }

        // Получаем данные всех пользователей одним batch-запросом
        return userRepository.findAllByIdsAsync(userIds)
                .thenApply(users -> {
                    List<UserDto> userDtos = users.stream()
                            .map(UserMapper::toDto)
                            .collect(Collectors.toList());
                    project.setProjectUsers(userDtos);
                    return project;
                });
    }


    @Override
    public CompletableFuture<Project> addUserToProjectAsync(UUID userId, UUID projectId) {
        if (userId == null || projectId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE)
            );
        }
        return projectRepository.findByIdAsync(projectId)
                .thenCompose(project -> {
                    if (project == null) {
                        return CompletableFuture.failedFuture(

                                new NoSuchElementException(String.format("$s: %s", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE, projectId))
                        );
                    }

                    if (userId.equals(project.getAdminId())) {
                        return CompletableFuture.failedFuture(
                                new IllegalArgumentException(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE)
                        );
                    }

                    // Добавляем пользователя в проект через репозиторий
                    return projectUserRepository.addUserToProject(userId, projectId)
                            .thenApply(success -> {
                                if (!success) {
                                    throw new CompletionException(
                                            new SQLException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE)
                                    );
                                }
                                Set<UserDto> updatedUsers = new HashSet<>(project.getProjectUsers());
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

                                List<UserDto> updatedUsers = new ArrayList<>(project.getProjectUsers());
                                var user = updatedUsers.stream().filter(userDto -> userDto.getId().equals(userId)).findFirst();
                                updatedUsers.remove(user);
                                project.setProjectUsers(updatedUsers);

                                return project;
                            });
                });
    }






    @Override
    public CompletableFuture<Project> updateByIdAsync(UUID id, Project entity) {
        return null;
    }
}
