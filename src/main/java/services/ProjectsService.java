package services;

import models.entities.Project;
import repositories.ProjectsRepositoryImplementation;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsService implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectsService() throws SQLException {
        this.projectRepository = new ProjectsRepositoryImplementation();
        this.userRepository = new UsersRepositoryImplementation();
    }

    @Override
    public CompletableFuture<List<Project>> getByUserIdAsync(UUID userId) {
        // 1. Валидация входного параметра
        if (userId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }

        // 2. Асинхронный запрос к репозиторию
        return projectRepository.findByUserIdAsync(userId)
                .thenApply(projects -> {
                    // 3. Обработка результата
                    if (projects == null) {
                        return new ArrayList<Project>();
                    }
                    return projects;
                })
                .exceptionally(ex -> {
                    // 4. Обработка ошибок
                    System.out.println(String.format("Failed to load user projects for user ID: %s", userId));
                    return Collections.emptyList(); // Или можно пробросить исключение дальше
                });
    }
    /*public CompletableFuture<List<Project>> getByUserIdAsync(UUID userId) {
        if (userId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }

        var projectFuture = projectRepository.findByUserIdAsync(userId)
                .thenCompose(projects -> {
                    if (projects.isEmpty()) {
                        return CompletableFuture.completedFuture(Collections.emptyList());
                    }
                    return projectFuture;
                });

        return projectRepository.findByUserIdAsync(userId)
                .thenCompose(projects -> {
                    if (projects.isEmpty()) {
                        return CompletableFuture.completedFuture(Collections.emptyList());
                    }

                    // Загружаем дополнительные данные для проектов (если нужно)
                    //return enrichProjectsWithAdditionalData(projects);
                    return enrichProjectsWithAdditionalData(projects);
                    //return CompletableFuture.completedFuture(Collections.emptyList());
                })
                .exceptionally(ex -> {
                    //log.error("Failed to get projects for user {}: {}", userId, ex.getMessage());
                    throw new CompletionException("Failed to load user projects", ex);
                });
    }*/

    private CompletableFuture<List<Project>> enrichProjectsWithAdditionalData(List<Project> projects) {
        // Здесь можно добавить загрузку дополнительных данных
        // Например, информации о пользователях проектов

        // В простейшем случае просто возвращаем проекты без изменений
        return CompletableFuture.completedFuture(projects);

    /* Пример с загрузкой пользователей:
    List<UUID> projectIds = projects.stream()
        .map(Project::getId)
        .collect(Collectors.toList());

    return projectUserRepository.findByProjectIds(projectIds)
        .thenApply(projectUsers -> {
            Map<UUID, List<User>> usersByProjectId = ... // группировка
            projects.forEach(project ->
                project.setUsers(usersByProjectId.get(project.getId()))
            );
            return projects;
        });
    */
    }

    @Override
    public CompletableFuture<List<Project>> getByAdminIdAsync(UUID adminId) throws SQLException {
        // 1. Валидация входного параметра
        if (adminId == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }

        // 2. Асинхронный запрос к репозиторию
        return projectRepository.findByAdminIdAsync(adminId)
                .thenApply(projects -> {
                    // 3. Обработка результата
                    if (projects == null) {
                        return new ArrayList<Project>();
                    }
                    return projects;
                })
                .exceptionally(ex -> {
                    // 4. Обработка ошибок
                    System.out.println(String.format("Failed to load user projects for user ID: %s", adminId));
                    return Collections.emptyList(); // Или можно пробросить исключение дальше
                });
    }

    @Override
    public CompletableFuture<Project> addUserToProjectAsync(UUID userId, UUID projectId) {
        return null;
    }

    @Override
    public CompletableFuture<Project> removeUserFromProjectAsync(UUID userId, UUID projectId) {

        return null;
    }

    @Override
    public CompletableFuture<Project> createAsync(Project project) throws Exception {
        if (project == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User cannot be null"));
        }

        CompletableFuture<Project> projectFuture = projectRepository.createAsync(project);

        return projectFuture
                .exceptionally(ex -> {
                    throw new CompletionException(ex.getCause() != null ? ex.getCause() : ex);
                });
    }


    @Override
    public CompletableFuture<Project> getByIdAsync(UUID id) throws SQLException {
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
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) throws SQLException {
        if (id == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }

        return projectRepository.deleteAsync(id)
                .thenApply(deleted -> {
                    if (!deleted) {
                        throw new NoSuchElementException("User with id " + id + " not found");
                    }
                    return true;
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof SQLException) {
                        throw new CompletionException("Database error while deleting user", ex.getCause());
                    } else {
                        throw new CompletionException(ex);
                }});
    }


    @Override
    public CompletableFuture<Project> updateByIdAsync(UUID id, Project entity) {
        return null;
    }
}
