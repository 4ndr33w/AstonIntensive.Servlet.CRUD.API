package services;

import models.dtos.ProjectDto;
import models.entities.User;
import repositories.ProjectsRepositoryImplementation;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.UserService;
import utils.mappers.ProjectMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersService implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectsRepository;

    public UsersService() throws SQLException {
        this.userRepository = new UsersRepositoryImplementation();
        this.projectsRepository = new ProjectsRepositoryImplementation();
    }

    @Override
    public CompletableFuture<User> getByIdAsync(UUID id) throws SQLException {
        return userRepository.findByIdAsync(id)
                .thenCompose(user -> {
                    if (user == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return enrichUserWithProjects(user);
                })
                .exceptionally(ex -> {
                    System.err.println("Error fetching user by id: " + ex.getMessage());
                    return null;
                });
    }

    @Override
    public CompletableFuture<User> createAsync(User user) throws Exception {
        if (user == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User cannot be null"));
        }
        CompletableFuture<User> userFuture = userRepository.createAsync(user);

        return userFuture
                //.thenApplyAsync(UserMapper::toDto)
                .exceptionally(ex -> {
                    System.err.println("Error creating user: " + ex.getMessage());

                    throw new CompletionException(ex.getCause() != null ? ex.getCause() : ex);
                });
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) throws SQLException {
        // Проверка входного параметра
        if (id == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User ID cannot be null"));
        }

        // Вызов метода репозитория для удаления
        return userRepository.deleteAsync(id)
                .thenApply(deleted -> {
                    if (!deleted) {
                        throw new NoSuchElementException("User with id " + id + " not found");
                    }
                    return true;
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof SQLException) {
                        throw new CompletionException("Database error while deleting user", ex.getCause());
                    }
                    throw new CompletionException(ex);
                });
    }

    @Override
    public CompletableFuture<List<User>> getAllAsync() throws SQLException {


        return userRepository.findAllAsync()
                .thenCompose(users -> {

                    List<CompletableFuture<User>> userFutures = users.stream()
                            .map(this::enrichUserWithProjects)
                            .collect(Collectors.toList());

                    return CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> userFutures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList()));
                })
                .exceptionally(ex -> {
                    System.err.println("Error fetching users: " + ex.getMessage());
                    throw new CompletionException(ex);
                });
    }

    private CompletableFuture<User> enrichUserWithProjects(User user) {
        return projectsRepository.findByUserIdAsync(user.getId())
                .thenCompose(projects -> {
                    List<CompletableFuture<ProjectDto>> projectFutures = projects.stream()
                            .map(project -> {
                                        try {
                                            return projectsRepository.findByIdAsync(project.getId())
                                                    .thenApply(fullProject -> {
                                                        ProjectDto projectDto = ProjectMapper.toDto(fullProject);

                                                        List<User> users = fullProject.getProjectUsers().stream()
                                                                .map(u -> {
                                                                    try {
                                                                        return userRepository.findByIdAsync(u.getId())
                                                                                //.thenApply(UserMapper::toDto)
                                                                                .join();
                                                                    } catch (Exception e) {
                                                                        throw new CompletionException(e);
                                                                    }
                                                                })
                                                                .collect(Collectors.toList());

                                                        projectDto.setProjectUsersIds(users.stream().map(User::getId).collect(Collectors.toList()));
                                                        return projectDto;
                                                    });
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            )
                            .collect(Collectors.toList());

                    // Комбинируем все асинхронные операции с проектами
                    return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                //User _user = user;
                                user.setProjects(
                                        projectFutures.stream()
                                                .map(CompletableFuture::join)
                                                .collect(Collectors.toList())
                                );
                                return user;
                            });
                });
    }


    //TODO: Реализовать остальные методы

    @Override
    public CompletableFuture<User> getUserByEmailAsync(String email) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> updateByIdAsync(UUID id, User entity) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> getUserByUserNameAsync(String username) throws Exception {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> updateEmailAsync(String oldEmail, String newEmail) throws Exception {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> updatePasswordAsync(UUID userId, String password) throws Exception {
        return CompletableFuture.completedFuture(null);
    }
}
