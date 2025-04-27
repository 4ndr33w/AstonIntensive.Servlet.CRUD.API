package services;

import models.dtos.ProjectDto;
import models.dtos.ProjectUsersDto;
import models.dtos.UserDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.ProjectRepository;
import repositories.ProjectUsersRepositoryImpl;
import repositories.UsersRepository;
import repositories.interfaces.ProjectUserRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.NoProjectsFoundException;
import utils.exceptions.ProjectNotFoundException;
import utils.mappers.ProjectMapper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsService implements ProjectService {

    Logger logger = LoggerFactory.getLogger(ProjectsService.class);
    private final repositories.interfaces.ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;

    public ProjectsService() {
        this.projectRepository = new ProjectRepository();
        this.userRepository = new UsersRepository();
        this.projectUserRepository = new ProjectUsersRepositoryImpl();
    }

    public ProjectsService(repositories.interfaces.ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = new UsersRepository();
        this.projectUserRepository = new ProjectUsersRepositoryImpl();
    }

    @Override
    public CompletableFuture<List<ProjectDto>> getProjectsByUserIdAsync(UUID userId) throws SQLException, NoProjectsFoundException, NullPointerException, RuntimeException{
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        var projectUsers = projectUserRepository.findByUserIdAsync(userId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return findProjectsFromProjectUsers(projectUsers);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private List<ProjectDto> findProjectsFromProjectUsers(CompletableFuture<List<ProjectUsersDto>> projectUsersFuture) throws SQLException, NoProjectsFoundException {

        List<ProjectUsersDto> projectUsers = projectUsersFuture.join();
        List<UUID> userIds = projectUsers.stream().map(ProjectUsersDto::getProjectId).toList();
        var projects = projectRepository.findByProjectIdsAsync(userIds).join();
        if(projects.isEmpty()) {
            return new ArrayList<>();
        }

        return projects.stream().map(ProjectMapper::toDto).toList();
    }

    @Override
    public CompletableFuture<List<ProjectDto>> getByAdminIdAsync(UUID adminId) throws SQLException, NoProjectsFoundException, NullPointerException {
        Objects.requireNonNull(adminId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        CompletableFuture<List<Project>> projectsFuture = projectRepository.findByAdminIdAsync(adminId);
        var projectIds = getProjectIds(projectsFuture);
        var projectUsersFuture = projectIds
                .thenCompose(this::getUserIdsFromProjectUsersByProjectIds);

        return projectsFuture.thenCombine(projectUsersFuture, (projects, usersMap) -> {
            return projects.stream()
                    .map(project -> {
                        ProjectDto dto = ProjectMapper.toDto(project);

                        dto.setProjectUsersIds(usersMap.getOrDefault(project.getId(), Collections.emptyList()));
                        return dto;
                    })
                    .collect(Collectors.toList());
        });
    }

    private CompletableFuture<List<UUID>> getProjectIds(CompletableFuture<List<Project>> projectsFuture) throws SQLException, NoProjectsFoundException {
        return projectsFuture.thenApply(projects -> {
            if (projects == null || projects.isEmpty()) {
                throw new NoProjectsFoundException(StaticConstants.PROJECTS_NOT_FOUND_EXCEPTION_MESSAGE);
            }
            return projects.stream().map(Project::getId).toList();
        });
    }
    private CompletableFuture<Map<UUID, List<UUID>>> getUserIdsFromProjectUsersByProjectIds(List<UUID> projectIds)
            throws DatabaseOperationException, NullPointerException {

        try {
            return projectUserRepository.findByProjectIdsAsync(projectIds)
                    .thenApply(projectUsers -> {
                        return projectUsers
                                .stream()
                                .collect(Collectors.groupingBy(
                                        ProjectUsersDto::getProjectId,
                                        Collectors.mapping(
                                                ProjectUsersDto::getUserId,
                                                Collectors.toList()
                                        )
                                ));
                    });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException, DatabaseOperationException, NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.findByIdAsync(projectId)
                .thenCompose(project -> {
                    if (project == null) {
                        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    if (userId.equals(project.getAdminId())) {
                        logger.error(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE);
                        throw new IllegalArgumentException(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE);
                    }

                    return addUserToProjectUser(userId, projectId, project);
                });
    }
    private CompletableFuture<ProjectDto> addUserToProjectUser(UUID userId, UUID projectId, Project project) {
        try {
            return projectUserRepository.addUserToProjectAsync(userId, projectId)
                    .thenApply(success -> {
                        if (!success) {
                            throw new DatabaseOperationException(StaticConstants.FAILED_TO_UPDATE_PROJECT_USERS_EXCEPTION_MESSAGE);
                        }
                        List<UUID> updatedUsers = new ArrayList<>();

                        var projectDto = ProjectMapper.toDto(project);
                        if( projectDto.getProjectUsersIds() != null) {
                            updatedUsers = new ArrayList<>(projectDto.getProjectUsersIds());
                        }
                        //-----------------------------------------
                        updatedUsers.add(userId);
                        projectDto.setProjectUsersIds(updatedUsers.stream().toList());

                        return projectDto;
                    });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<ProjectDto> removeUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException, DatabaseOperationException, NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        Objects.requireNonNull(projectId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.findByIdAsync(projectId)
                .thenCompose(project -> {
                    if (project == null) {
                        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    if (userId.equals(project.getAdminId())) {
                        return CompletableFuture.failedFuture(
                                new IllegalArgumentException(StaticConstants.ADMIN_CANNOT_BE_ADDED_TO_PROJECT_EXCEPTION_MESSAGE)
                        );
                    }
                    try {
                        return projectUserRepository.deleteUserFromProjectAsync(userId, projectId)
                                .thenApply(success -> {
                                    if (!success) {
                                        throw new DatabaseOperationException(StaticConstants.FAILED_TO_UPDATE_PROJECT_USERS_EXCEPTION_MESSAGE);
                                    }
                                    List<UserDto> updatedUsers = new ArrayList<>();

                                    if (project.getProjectUsers() == null) {
                                        project.setProjectUsers(new ArrayList<>());
                                        return ProjectMapper.toDto(project);
                                    }
                                    else {
                                        updatedUsers = new ArrayList<>(project.getProjectUsers());
                                        var user = updatedUsers.stream().filter(userDto -> userDto.getId().equals(userId)).findFirst();
                                        updatedUsers.remove(user.get());

                                        project.setProjectUsers(updatedUsers);
                                        return ProjectMapper.toDto(project);
                                    }
                                });
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<ProjectDto> createAsync(Project project) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException {
        Objects.requireNonNull(project, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.createAsync(project)
                .thenApply(ProjectMapper::toDto);
    }

    @Override
    public CompletableFuture<ProjectDto> getByIdAsync(UUID id) throws SQLException, RuntimeException, ProjectNotFoundException {
        return projectRepository.findByIdAsync(id)
                .thenCompose(project -> {
                    if (project == null) {
                        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
                }
                    return CompletableFuture.completedFuture(ProjectMapper.toDto(project));
                    });
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) throws SQLException, NullPointerException {
        Objects.requireNonNull(id, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.deleteAsync(id);
    }

    @Override
    public CompletableFuture<ProjectDto> updateByIdAsync(ProjectDto projectDto) throws SQLException, NullPointerException {
        Objects.requireNonNull(projectDto, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return projectRepository.updateAsync(ProjectMapper.mapToEntity(projectDto, List.of()))
                .thenApply(updatedProject -> {
                    if (updatedProject == null) {
                        throw new ProjectNotFoundException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
                    }
                    return ProjectMapper.toDto(updatedProject);
                });
    }
}
