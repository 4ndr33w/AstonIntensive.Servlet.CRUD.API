package services;

import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.dtos.ProjectDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import repositories.ProjectRepositoryNew;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.UserService;
import utils.StaticConstants;
import utils.exceptions.ProjectNotFoundException;
import utils.exceptions.UserNotFoundException;
import utils.mappers.ProjectMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * Клас сервиса, предоставляющий методы для
 * {@code CRUD} операции над объектом {@code User}
 * <p>
 *     Выполняются асинхронные операции с репозиторием
 * </p>
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersService implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectsRepository;
    private final Logger logger;

    public UsersService() {
        this.userRepository = new UsersRepositoryImplementation();
        this.projectsRepository = new ProjectRepositoryNew();
        logger = org.slf4j.LoggerFactory.getLogger(UsersService.class);
    }

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger = org.slf4j.LoggerFactory.getLogger(UsersService.class);
        this.projectsRepository = new ProjectRepositoryNew();
    }

    @Override
    public CompletableFuture<User> getByIdAsync(UUID id) {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        return userRepository.findByIdAsync(id)
                .thenCompose(user -> {
                    if (user == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return enrichUserWithProjects(user);
                })
                .exceptionally(ex -> {
                    String message = String.format("$s,  id: %s\ncause: %s", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE, id, ex.getCause().getMessage());
                    logger.error(message);
                    throw new UserNotFoundException(message, ex);
                });
    }

    @Override
    public CompletableFuture<User> createAsync(User user) {
        Objects.requireNonNull(user, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.createAsync(user)
                .exceptionally(ex -> {
                    String message = String.format("%s, userId: %s",
                            StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE,
                            user.getId());
                    logger.error(message, ex);
                    throw new CompletionException(message, ex);
                });
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.deleteAsync(id)
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof SQLException) {
                        throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, ex.getCause());
                    }
                    throw new CompletionException(ex);
                });
    }

    @Override
    public CompletableFuture<List<User>> getAllAsync() {

        return userRepository.findAllAsync()
                .thenCompose(users -> {

                    List<CompletableFuture<User>> userFutures = users.stream()
                            .map(this::enrichUserWithProjects)
                            .toList();

                    return CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> userFutures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList()));
                })
                .exceptionally(ex -> {
                    logger.error("Error fetching users: " + ex.getMessage());
                    throw new CompletionException(ex);
                });
    }

    private CompletableFuture<User> enrichUserWithProjects(User user) {
        return projectsRepository.findByUserIdAsync(user.getId())
                .thenCompose(projects -> {
                    List<CompletableFuture<ProjectDto>> projectFutures = projects.stream()
                            .map(project -> {

                                return projectsRepository.findByIdAsync(project.getId())
                                    .thenApply(this::findProjects);
                                    }).toList();

                    return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> collectProjectsFromFutures(user, projectFutures));
                });
    }

    private ProjectDto findProjects(Project fullProject) {
        ProjectDto projectDto = ProjectMapper.toDto(fullProject);

        List<User> users = fullProject.getProjectUsers().stream()
                .map(u -> {
                    try {
                        return userRepository.findByIdAsync(u.getId())
                                .join();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                })
                .toList();

        projectDto.setProjectUsersIds(users.stream().map(User::getId).collect(Collectors.toList()));
        return projectDto;
    }

    private User collectProjectsFromFutures(User user, List<CompletableFuture<ProjectDto>> projectFutures) {
        user.setProjects(
                projectFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        );
        return user;
    }

    @Override
    public CompletableFuture<User> updateByIdAsync(User user) {
        Objects.requireNonNull(user, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.updateAsync(user)
                .thenCompose(updatedUser -> {
                    if (updatedUser == null) {
                        throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE + " id: " + user.getId());
                    }
                    return enrichUserWithProjects(updatedUser);

                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof NoSuchElementException) {
                        logger.error(String.format("%s; id: %s; %s", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE, user.getId(), ex.getCause()));
                        throw new ProjectNotFoundException(String.format("%s; id: %s; %s", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE, user.getId(), ex.getCause()));
                    }
                    logger.error(String.format("%s; id: %s", StaticConstants.FAILED_TO_UPDATE_USER_EXCEPTION_MESSAGE, user.getId()));
                    throw new CompletionException(String.format("%s; id: %s", StaticConstants.FAILED_TO_UPDATE_USER_EXCEPTION_MESSAGE, user.getId()), ex.getCause());
                });
    }

    //TODO: Остальные методы пока не реализованы

    @Override
    public CompletableFuture<User> getUserByEmailAsync(String email) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> getUserByUserNameAsync(String username){
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> updateEmailAsync(String oldEmail, String newEmail) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> updatePasswordAsync(UUID userId, String password) {
        return CompletableFuture.completedFuture(null);
    }
}
