package services;

import models.dtos.ProjectDto;
import models.dtos.ProjectUsersDto;
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
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
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
    private final repositories.interfaces.ProjectUserRepository projectUserRepository;
    private final Logger logger;

    public UsersService() {
        this.userRepository = new UsersRepository();
        this.projectsRepository = new ProjectRepository();
        this.projectUserRepository = new repositories.ProjectUsersRepositoryImpl();
        logger = org.slf4j.LoggerFactory.getLogger(UsersService.class);
    }

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger = org.slf4j.LoggerFactory.getLogger(UsersService.class);
        this.projectsRepository = new ProjectRepository();
        this.projectUserRepository = new repositories.ProjectUsersRepositoryImpl();
    }

    @Override
    public CompletableFuture<UserDto> getByIdAsync(UUID id) throws NullPointerException, UserNotFoundException, DatabaseOperationException, ResultSetMappingException, SQLException  {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        return userRepository.findByIdAsync(id)
                .thenCompose(user -> {
                    if (user == null) {
                        throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    try {
                        return combineProjectsWithUsers(List.of(user))
                                .thenApply(users -> users.stream()
                                        .map(UserMapper::toDto)
                                        .toList().get(0));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
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
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id)throws SQLException, DatabaseOperationException, NullPointerException, UserNotFoundException, CompletionException {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.deleteAsync(id);
    }

    @Override
    public CompletableFuture<List<UserDto>> getAllAsync() throws SQLException, DatabaseOperationException, CompletionException, NoUsersFoundException, ResultSetMappingException {

        return userRepository.findAllAsync()
                .thenCompose(users -> {
                    if(users.isEmpty()) throw new NoUsersFoundException(StaticConstants.USERS_NOT_FOUND_EXCEPTION_MESSAGE);
                    try {
                        return combineProjectsWithUsers(users)
                                .thenApply(users1 -> users1.stream()
                                        .map(UserMapper::toDto).toList());
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private CompletableFuture<List<User>> combineProjectsWithUsers(List<User> users) throws SQLException, ExecutionException, InterruptedException {

        List<UUID> userIds = users.stream().map(User::getId).toList();
        Map<UUID, List<ProjectDto>> adminProjectsMap = getAdminProjectsMap(userIds);
        Map<UUID, List<ProjectUsersDto>> userProjectsMap = getUserProjectsMap (userIds );
        List<UUID> allProjectIds = !userProjectsMap.isEmpty() ? getProjectIds(userProjectsMap) : new ArrayList<>();
        Map<UUID, ProjectDto> projectsMap = !allProjectIds.isEmpty() ?  projectsMap (allProjectIds ) : new HashMap<>();

        return CompletableFuture
                .supplyAsync(() -> {
                    return users.stream()
                            .map(user -> {
                                List<ProjectDto> userProjects = new ArrayList<>();

                                if(adminProjectsMap.size() > 0)
                                {
                                    userProjects.addAll(adminProjectsMap.getOrDefault(user.getId(), List.of()));
                                }
                                if (userProjectsMap.size() > 0) {
                                    userProjects.addAll(
                                            userProjectsMap.getOrDefault(user.getId(), List.of())
                                                    .stream()
                                                    .map(pu -> projectsMap.get(pu.getProjectId()))
                                                    .filter(Objects::nonNull)
                                                    .toList()
                                    );
                                }
                                if (!userProjects.isEmpty()) {
                                    user.setProjects(userProjects);
                                }
                                else  user.setProjects(List.of());
                                return user;
                            })
                            .toList();
                });

    }

    Map<UUID, List<ProjectUsersDto>> getUserProjectsMap (List<UUID> userIds) throws SQLException, ExecutionException, InterruptedException {

        return projectUserRepository.findByUserIdsAsync(userIds).get()
                .stream()
                .collect(Collectors.groupingBy(ProjectUsersDto::getUserId));
    }

    List<UUID> getProjectIds (Map<UUID, List<ProjectUsersDto>> userProjectsMap) {

        return userProjectsMap.values()
                .stream()
                .flatMap(List::stream)
                .map(ProjectUsersDto::getProjectId)
                .distinct()
                .toList();
    }

    Map<UUID, ProjectDto> projectsMap (List<UUID> projectIds ) throws SQLException, ExecutionException, InterruptedException {
        return findProjectDtos(projectIds)
                .stream()
                .collect(Collectors.toMap(ProjectDto::getId, Function.identity()));
    }
    private List<ProjectDto> findProjectDtos(List<UUID> projectIds) throws SQLException, ExecutionException, InterruptedException {

        var projects = projectsRepository.findByProjectIdsAsync(projectIds).get();

        if(!projects.isEmpty()) {
            return projects.stream().map(ProjectMapper::toDto).toList();
        }
        return List.of();
    }

    Map<UUID, List<ProjectDto>> getAdminProjectsMap (List<UUID> userIds) throws SQLException, ExecutionException, InterruptedException {

        Map<UUID, List<ProjectDto>> adminProjectMap = findAllProjectsByAdminIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(ProjectDto::getAdminId));

        if(adminProjectMap.isEmpty()) {
            return Map.of();
        }
        else {
            return adminProjectMap;
        }
    }

    private List<ProjectDto> findAllProjectsByAdminIds(List<UUID> adminIds) throws SQLException, ExecutionException, InterruptedException {
        Objects.requireNonNull(adminIds, "AdminIds cannot be null");

        var projectsByAdminIdsOptional = projectsRepository.findByAdminIdsAsync(adminIds).get();
        if(projectsByAdminIdsOptional.size() > 0) {
            return projectsByAdminIdsOptional.stream().map(ProjectMapper::toDto).toList();
        }
        return List.of();
    }

    @Override
    public CompletableFuture<UserDto> updateByIdAsync(UserDto userDto) throws SQLException {
        Objects.requireNonNull(userDto, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return userRepository.updateAsync(UserMapper.mapToEntity(userDto))
                .thenCompose(updatedUser -> {
                    if (updatedUser == null) {
                        throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE + " id: " + userDto.getId());
                    }
                    try {
                        return combineProjectsWithUsers(List.of(updatedUser))
                                .thenApply(users -> users.stream()
                                        .map(UserMapper::toDto)
                                        .toList().get(0));
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                });
    }

}