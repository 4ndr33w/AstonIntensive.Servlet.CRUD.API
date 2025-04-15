package services.synchronous;

import models.dtos.ProjectUsersDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import repositories.ProjectUsersRepositoryImpl;
import repositories.ProjectsRepositoryImplementation;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import repositories.synchronous.ProjectUsersRepositorySynchronous;
import services.interfaces.synchronous.ProjectServiceSynchro;
import utils.StaticConstants;
import utils.mappers.UserMapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsService implements ProjectServiceSynchro {

    private final repositories.interfaces.synchronous.ProjectRepoSynchro projectRepository;
    private final UserRepository userRepository;
    private final ProjectUsersRepositoryImpl projectUsersRepository;

    public ProjectsService() {
        this.projectRepository = new repositories.synchronous.ProjectsRepository();
        this.userRepository = new UsersRepositoryImplementation();
        this.projectUsersRepository = new ProjectUsersRepositoryImpl();
    }


    @Override
    public List<Project> getByUserId(UUID userId) {

        Optional<List<Project>> projectsOpt = projectRepository.findByUserId(userId);
        if (projectsOpt.isEmpty() || projectsOpt.get().isEmpty()) {
            return Collections.emptyList();
        }

        List<Project> projects = projectsOpt.get();
        return enrichProjectsWithUsers(projects);
    }
    /*public List<Project> getByUserId(UUID userId) {
        Objects.requireNonNull(userId, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);


        List<Project> projects = new ArrayList<>();
        try {
            var projectUsers = projectUsersRepository.findByUserId(userId).get();
            for (var projectUser : projectUsers) {
                var result = projectRepository.findById(projectUser.getProjectId()).get();
                projects.add(result);
            }

        }
        catch (CompletionException | InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof NullPointerException) {
                return result.get();
            }
        }

        return  null;
    }*/

    @Override
    public List<Project> getByAdminId(UUID adminId) {
        // Получаем проекты, где пользователь является админом
        Optional<List<Project>> projectsOpt = projectRepository.findByAdminId(adminId);
        if (projectsOpt.isEmpty() || projectsOpt.get().isEmpty()) {
            return Collections.emptyList();
        }

        List<Project> projects = projectsOpt.get();
        var result = enrichProjectsWithUsers(projects);
        return result;
    }

    @Override
    public Project addUserToProject(UUID userId, UUID projectId) {
        return null;
    }

    @Override
    public Project removeUserFromProject(UUID userId, UUID projectId) {
        return null;
    }

    @Override
    public Project create(Project entity) {
        Objects.requireNonNull(entity, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        //var result = projectRepository.create(entity);
        return projectRepository.create(entity);
    }

    @Override
    public Project getById(UUID id) {
        // Получаем проект по ID
        Optional<Project> projectOpt = projectRepository.findById(id);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Project not found with id: " + id);
        }

        Project project = projectOpt.get();
        return enrichProjectWithUsers(project);
    }

    @Override
    public boolean deleteById(UUID id) {
        return false;
    }

    @Override
    public Project updateById(UUID id, Project entity) {
        return null;
    }

    private List<Project> enrichProjectsWithUsers(List<Project> projects) {
        // Собираем ID всех проектов
        List<UUID> projectIds = projects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        try {
            List<ProjectUsersDto> projectUsers = projectUsersRepository.findByProjectIds(projectIds).get();

            Map<UUID, List<ProjectUsersDto>> usersByProjectId = projectUsers.stream()
                    .collect(Collectors.groupingBy(ProjectUsersDto::getProjectId));

            Set<UUID> userIds = projectUsers.stream()
                    .map(ProjectUsersDto::getUserId)
                    .collect(Collectors.toSet());

            Map<UUID, User> usersMap = userRepository.findAllByIdsAsync(new ArrayList<>(userIds))
                    .get()
                    .stream()
                    .collect(Collectors.toMap(User::getId, user -> user));

            return projects.stream()
                    .map(project -> {
                        List<ProjectUsersDto> projectUserDtos = usersByProjectId.getOrDefault(project.getId(), Collections.emptyList());
                        List<UserDto> userDtos = projectUserDtos.stream()
                                .map(dto -> {
                                    User user = usersMap.get(dto.getUserId());
                                    return user != null ? UserMapper.toDto(user) : null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        project.setProjectUsers(userDtos);
                        return project;
                    })
                    .collect(Collectors.toList());
        }
        catch (CompletionException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Project enrichProjectWithUsers(Project project) {
        try {
            List<ProjectUsersDto> projectUsers = projectUsersRepository.findByProjectId(project.getId()).get();

            if (!projectUsers.isEmpty()) {

                List<UUID> userIds = projectUsers.stream()
                        .map(ProjectUsersDto::getUserId)
                        .collect(Collectors.toList());

                List<User> users = userRepository.findAllByIdsAsync(userIds).get();

                List<UserDto> userDtos = users.stream()
                        .map(UserMapper::toDto)
                        .collect(Collectors.toList());
                project.setProjectUsers(userDtos);
            } else {
                project.setProjectUsers(Collections.emptyList());
            }
            return project;
        }
        catch (CompletionException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
