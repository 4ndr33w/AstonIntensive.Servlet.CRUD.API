package services;

import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.User;
import repositories.ProjectsRepository;
import repositories.UsersRepository;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.BaseService;
import services.interfaces.UserService;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static utils.mappers.UserMapper.toDto;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersService implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectsRepository;

    public UsersService() throws SQLException {
        this.userRepository = new UsersRepository();
        this.projectsRepository = new ProjectsRepository();
    }

    @Override
    public CompletableFuture<UserDto> getByIdAsync(UUID id) {
        return userRepository.findByIdAsync(id)
                .thenApplyAsync(UserMapper::toDto)
                .exceptionally(ex -> {
                    //System.err.println("Error fetching users: " + ex.getMessage());
                    return null; // Fallback
                });
    }

    @Override
    public CompletableFuture<UserDto> createUserAsync(User user) throws Exception {
        if (user == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("User cannot be null"));
        }
        CompletableFuture<User> userFuture = userRepository.createAsync(user);

        return userFuture
                .thenApplyAsync(UserMapper::toDto)
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
    public CompletableFuture<List<UserDto>> getAllAsync() throws SQLException {
        return userRepository.findAllAsync()
                .thenCompose(users -> {
                    // Для каждого пользователя загружаем проекты
                    List<CompletableFuture<UserDto>> userFutures = users.stream()
                            .map(this::enrichUserWithProjects)
                            .collect(Collectors.toList());

                    // Комбинируем все асинхронные операции
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

    private CompletableFuture<UserDto> enrichUserWithProjects(User user) {
        return projectsRepository.findByUserIdAsync(user.getId())
                .thenCompose(projects -> {
                    // Для каждого проекта асинхронно загружаем его пользователей
                    List<CompletableFuture<ProjectDto>> projectFutures = projects.stream()
                            .map(project -> projectsRepository.findByIdAsync(project.getId())
                                    .thenApply(fullProject -> {
                                        ProjectDto dto = ProjectMapper.toDto(fullProject);

                                        List<UserDto> userDtos = fullProject.getProjectUsers().stream()
                                                .map(u -> {
                                                    try {
                                                        return userRepository.findByIdAsync(u.getId())
                                                                .thenApply(UserMapper::toDto)
                                                                .join(); // Блокируем, так как уже в асинхронном контексте
                                                    } catch (Exception e) {
                                                        throw new CompletionException(e);
                                                    }
                                                })
                                                .collect(Collectors.toList());

                                        dto.setProjectUsersIds(userDtos.stream().map(UserDto::getId).collect(Collectors.toList()));
                                        return dto;
                                    })
                            )
                            .collect(Collectors.toList());

                    // Комбинируем все асинхронные операции с проектами
                    return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                UserDto userDto = UserMapper.toDto(user);
                                userDto.setProjects(
                                        projectFutures.stream()
                                                .map(CompletableFuture::join)
                                                .collect(Collectors.toList())
                                );
                                return userDto;
                            });
                });
    }


    //TODO: Реализовать остальные методы

    @Override
    public CompletableFuture<UserDto> getUserByEmailAsync(String email) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDto> updateByIdAsync(UUID id, UserDto entity) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDto> getUserByUserNameAsync(String username) throws Exception {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDto> updateEmailAsync(String oldEmail, String newEmail) throws Exception {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDto> updatePasswordAsync(UUID userId, String password) throws Exception {
        return CompletableFuture.completedFuture(null);
    }
}
