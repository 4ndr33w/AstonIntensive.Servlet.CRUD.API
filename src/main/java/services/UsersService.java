package services;

import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import repositories.ProjectRepository;
import repositories.UsersRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.UserService;
import utils.StaticConstants;
import utils.exceptions.*;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;

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
    private final repositories.interfaces.ProjectRepository projectsRepository;
    private final Logger logger;

    public UsersService() {
        this.userRepository = new UsersRepository();
        this.projectsRepository = new ProjectRepository();
        logger = org.slf4j.LoggerFactory.getLogger(UsersService.class);
    }

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger = org.slf4j.LoggerFactory.getLogger(UsersService.class);
        this.projectsRepository = new ProjectRepository();
    }

    @Override
    public CompletableFuture<UserDto> getByIdAsync(UUID id) throws NullPointerException, UserNotFoundException, DatabaseOperationException, ResultSetMappingException, SQLException  {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        return userRepository.findByIdAsync(id)
                .thenCompose(user -> {
                    if (user == null) {
                        throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
                        //return CompletableFuture.completedFuture(null);
                    }
                    return enrichUserWithProjects(UserMapper.toDto(user));
                });
                /*.exceptionally(ex -> {
                    String message = String.format("$s,  id: %s\ncause: %s", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE, id, ex.getCause().getMessage());
                    logger.error(message);
                    throw new UserNotFoundException(message, ex);
                });*/
    }

    /**
     * Метод для создания нового пользователя
     * @param user
     * @return
     */
    @Override
    public CompletableFuture<UserDto> createAsync(User user) throws DatabaseOperationException, NullPointerException, CompletionException, UserAlreadyExistException, SQLException {
        Objects.requireNonNull(user, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.createAsync(user).thenApply(UserMapper::toDto);
                /*.exceptionally(ex -> {
                    String message = String.format("%s, userId: %s",
                            StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE,
                            user.getId());
                    logger.error(message, ex);
                    throw new CompletionException(message, ex);
                });*/
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id)throws SQLException, DatabaseOperationException, NullPointerException, UserNotFoundException, CompletionException {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.deleteAsync(id);
                /*.exceptionally(ex -> {
                    if (ex.getCause() instanceof SQLException) {
                        throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, ex.getCause());
                    }
                    throw new CompletionException(ex);
                });*/
    }

    @Override
    public CompletableFuture<List<UserDto>> getAllAsync() throws SQLException, DatabaseOperationException, CompletionException, NoUsersFoundException, ResultSetMappingException {

        return userRepository.findAllAsync()
                .thenCompose(users -> {
                    if(users.isEmpty()) throw new NoUsersFoundException(StaticConstants.USERS_NOT_FOUND_EXCEPTION_MESSAGE);

                    List<CompletableFuture<UserDto>> userFutures = users.stream()
                            .map(UserMapper::toDto)
                            .map(this::enrichUserWithProjects)
                            .toList();

                    return CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> userFutures.stream()
                                    .map(CompletableFuture::join)
                                    .toList());
                });
    }

    private CompletableFuture<UserDto> enrichUserWithProjects(UserDto userDto) {
        return projectsRepository.findByUserIdAsync(userDto.getId())
                .thenCompose(projects -> {
                    List<CompletableFuture<ProjectDto>> projectFutures = projects.stream()
                            .map(project -> {

                                try {
                                    return projectsRepository.findByIdAsync(project.getId())
                                        .thenApply(this::findProjects);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }).toList();

                    return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> collectProjectsFromFutures(userDto, projectFutures));
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

    private UserDto collectProjectsFromFutures(UserDto userDto, List<CompletableFuture<ProjectDto>> projectFutures) {
        userDto.setProjects(
                projectFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        );
        return userDto;
    }

    @Override
    public CompletableFuture<UserDto> updateByIdAsync(UserDto userDto) throws SQLException {
        Objects.requireNonNull(userDto, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.updateAsync(UserMapper.mapToEntity(userDto))
                .thenCompose(updatedUser -> {
                    if (updatedUser == null) {
                        throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE + " id: " + userDto.getId());
                    }
                    return enrichUserWithProjects(UserMapper.toDto(updatedUser));

                });
                /*.exceptionally(ex -> {
                    if (ex.getCause() instanceof NoSuchElementException) {
                        logger.error(String.format("%s; id: %s; %s", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE, dto.getId(), ex.getCause()));
                        throw new ProjectNotFoundException(String.format("%s; id: %s; %s", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE, dto.getId(), ex.getCause()));
                    }
                    logger.error(String.format("%s; id: %s", StaticConstants.FAILED_TO_UPDATE_USER_EXCEPTION_MESSAGE, dto.getId()));
                    throw new CompletionException(String.format("%s; id: %s", StaticConstants.FAILED_TO_UPDATE_USER_EXCEPTION_MESSAGE, dto.getId()), ex.getCause());
                });*/
    }

}
