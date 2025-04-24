package services.synchronous;

import models.dtos.ProjectDto;
import models.dtos.ProjectUsersDto;
import models.entities.User;
import services.interfaces.synchronous.UserServiceSynchro;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.UserNotFoundException;
import utils.mappers.ProjectMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersService implements UserServiceSynchro {

    repositories.interfaces.synchronous.UserRepositorySynchro userRepository;
    repositories.interfaces.synchronous.ProjectUserRepositorySynchro projectUserRepository;
    repositories.interfaces.synchronous.ProjectRepoSynchro projectRepository;


    public UsersService() {

        userRepository = new repositories.synchronous.UsersRepositorySynchronous();
        projectUserRepository = new repositories.synchronous.ProjectUsersRepositorySynchronous();
        projectRepository = new repositories.synchronous.ProjectsRepository();
    }

    @Override
    public List<User> getAll() throws DatabaseOperationException, UserNotFoundException, SQLException{

        var usersOptional = userRepository.findAll();
        if(usersOptional.isPresent()) {

            var users = usersOptional.get();
            users = combineUsersWithProjects(users);
            return users;
        }
        throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    private List<User> combineUsersWithProjects(List<User> users) {
        List<UUID> userIds = users.stream().map(User::getId).toList();

        Map<UUID, List<ProjectDto>> adminProjectsMap = getAdminProjectsMap (userIds);
        Map<UUID, List<ProjectUsersDto>> userProjectsMap = getUserProjectsMap (userIds );

        List<UUID> allProjectIds = userProjectsMap.size()  > 0? getProjectIds(userProjectsMap) : new ArrayList<>();
        Map<UUID, ProjectDto> projectsMap = allProjectIds.size() > 0?  projectsMap (allProjectIds ) : new HashMap<>();

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
                    if (userProjects.size() > 0) {
                        user.setProjects(userProjects);
                    }
                    else  user.setProjects(List.of());
                    return user;
                })
                .toList();
    }

    Map<UUID, List<ProjectDto>> getAdminProjectsMap (List<UUID> userIds) {

        return findAllProjectsByAdminIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(ProjectDto::getAdminId));
    }
    Map<UUID, List<ProjectUsersDto>> getUserProjectsMap (List<UUID> userIds) {

        return findAllProjectUsersByUserIds(userIds)
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

    Map<UUID, ProjectDto> projectsMap (List<UUID> projectIds ) {
        return findProjectDtos(projectIds)
                .stream()
                .collect(Collectors.toMap(ProjectDto::getId, Function.identity()));
    }


    private User combineSingleUserWithProjects(User user) {
        List<ProjectDto> adminProjects = findAllProjectsByAdminIds(List.of(user.getId()));
        List<ProjectUsersDto> userProjectRelations = findAllProjectUsersByUserIds(List.of(user.getId()));

        List<ProjectDto> allProjects = Stream.concat(
                adminProjects.stream(),
                getAllUsersProjects(userProjectRelations, user.getId()).stream()
        ).distinct().toList();

        user.setProjects(allProjects);
        return user;
    }

    private List<ProjectDto> findAllProjectsByAdminIds(List<UUID> adminIds) {
        Objects.requireNonNull(adminIds, "AdminIds cannot be null");

        var projectsByAdminIdsOptional = projectRepository.findByAdminIds(adminIds);
        if(projectsByAdminIdsOptional.isPresent()) {
            return projectsByAdminIdsOptional.get().stream().map(ProjectMapper::toDto).toList();
        }
        return List.of();
    }

    private List<ProjectUsersDto> findAllProjectUsersByUserIds(List<UUID> userId) {

        var projectUsers = projectUserRepository.findByUserIds(userId);

        if(projectUsers.isPresent()) {
            return projectUsers.get();
        }
        return List.of();
    }

    private List<ProjectDto> getAllUsersProjects (List<ProjectUsersDto> projectUsers, UUID userId) {
        if (projectUsers.isEmpty()) {
            return List.of();
        }

        List<UUID> projectIds = projectUsers.stream()
                .filter(pu -> pu.getUserId().equals(userId))
                .map(ProjectUsersDto::getProjectId)
                .toList();

        return findProjectDtos(projectIds);
    }

    private List<ProjectDto> findProjectDtos(List<UUID> projectIds) {

        var projects = projectRepository.findByProjectIds(projectIds);

        if(projects.isPresent()) {
            return projects.get().stream().map(ProjectMapper::toDto).toList();
        }
        return List.of();
    }

    @Override
    public User create(User entity) {
        Objects.requireNonNull(entity, "User cannot be null");

        try {
            return userRepository.create(entity);
        }
        catch (SQLException e) {
            throw new DatabaseOperationException(StaticConstants.DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public User getById(UUID id) {
        Objects.requireNonNull(id, "Id cannot be null");

        return userRepository.findById(id)
                .map(this::combineSingleUserWithProjects)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public boolean deleteById(UUID id) throws SQLException {
        Objects.requireNonNull(id, "Id cannot be null");

        return userRepository.delete(id);
    }

    @Override
    public User updateById(User entity) throws SQLException {
        Objects.requireNonNull(entity, "User cannot be null");

        return userRepository.update(entity);
    }
}
