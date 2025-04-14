package services;

import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectUsersDto;
import models.entities.Project;
import models.entities.User;
import repositories.ProjectUsersRepository;
import repositories.ProjectsRepositoryImplementation;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static utils.mappers.UserMapper.toDto;


/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsService implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUsersRepository projectUserRepository;

    private static final ExecutorService dbExecutor;

    static {
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    public ProjectsService() throws SQLException {
        this.projectRepository = new ProjectsRepositoryImplementation();
        this.userRepository = new UsersRepositoryImplementation();
        this.projectUserRepository = new ProjectUsersRepository();
    }

    @Override
    public CompletableFuture<List<Project>> getByAdminIdAsync(UUID adminId) {
        return projectRepository.findByAdminIdAsync(adminId)
                .thenComposeAsync(projects -> {
                    if (projects == null || projects.isEmpty()) {
                        return CompletableFuture.completedFuture(Collections.emptyList());
                    }

                    // Для каждого проекта загружаем пользователей
                    List<CompletableFuture<Project>> enrichedProjects = projects.stream()
                            .map(this::enrichProjectWithUsers)
                            .toList();

                    return CompletableFuture.allOf(enrichedProjects.toArray(new CompletableFuture[0]))
                            .thenApply(v -> enrichedProjects.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList()));
                }, dbExecutor);
    }

    @Override
    public CompletableFuture<List<Project>> getByUserIdAsync(UUID userId) {
        if (userId == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("User ID cannot be null"));
        }

        return projectUserRepository.findByUserId(userId)
                .thenCompose(projectUsers -> {
                    List<UUID> projectIds = projectUsers.stream()
                            .map(ProjectUsersDto::getProjectId)
                            .collect(Collectors.toList());
                    try {
                        return loadProjectsWithUsers(projectIds);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error fetching user projects: " + ex.getMessage());
                    throw new CompletionException(ex.getCause() != null ? ex.getCause() : ex);
                });
    }

    private CompletableFuture<Project> enrichProjectWithUsers(Project project) {
        if (project == null) {
            return CompletableFuture.completedFuture(null);
        }

        return projectUserRepository.findByProjectId(project.getId())
                .thenComposeAsync(projectUsers -> {
                    List<UUID> userIds = projectUsers.stream()
                            .map(ProjectUsersDto::getUserId)
                            .collect(Collectors.toList());

                    return loadUsersByIds(userIds)
                            .thenApply(users -> {
                                project.setProjectUsers(users.stream()
                                        .map(UserMapper::toDto)
                                        .collect(Collectors.toList()));
                                return project;
                            });
                }, dbExecutor);
    }

    private CompletableFuture<List<User>> loadUsersByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        // Используем более эффективный пакетный запрос
        return userRepository.findAllByIdsAsync(userIds)
                .exceptionally(ex -> {
                    System.err.println("Error loading users: " + ex.getMessage());
                    return Collections.emptyList();
                });
    }

    private CompletableFuture<Project> loadSingleProjectWithUsers(UUID projectId) {
        try {
            return projectRepository.findByIdAsync(projectId)
                    .thenCompose(project ->
                            project != null ? enrichProjectWithUsers(project)
                                    : CompletableFuture.completedFuture(null)
                    );
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private CompletableFuture<List<Project>> loadProjectsWithUsers(List<UUID> projectIds) throws SQLException {
        if (projectIds == null || projectIds.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        try {
            List<CompletableFuture<Project>> projectFutures = projectIds.stream()
                    .map(this::loadSingleProjectWithUsers)
                    .toList();

            return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> projectFutures.stream()
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new RuntimeException(e);
            }

        }
    }





    @Override
    public CompletableFuture<Project> addUserToProjectAsync() {
        return null;
    }

    @Override
    public CompletableFuture<Project> removeUserFromProjectAsync() {


        return null;
    }

    @Override
    public CompletableFuture<Project> createAsync(Project entity) throws Exception {
        return null;
    }

    @Override
    public CompletableFuture<Project> getByIdAsync(UUID id) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) throws SQLException {
        return null;
    }

    @Override
    public CompletableFuture<Project> updateByIdAsync(UUID id, Project entity) {
        return null;
    }
}
