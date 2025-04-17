package services.synchronous;

import models.dtos.ProjectUsersDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.ProjectUsersRepositoryImpl;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.UserRepository;
import services.interfaces.synchronous.ProjectServiceSynchro;
import utils.StaticConstants;
import utils.mappers.UserMapper;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsService implements ProjectServiceSynchro {

    Logger logger = LoggerFactory.getLogger(ProjectsService.class);
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

    @Override
    public List<Project> getByAdminId(UUID adminId) {

        Optional<List<Project>> projectsOpt = projectRepository.findByAdminId(adminId);
        if (projectsOpt.isEmpty() || projectsOpt.get().isEmpty()) {
            logger.error("ProjectService: Projects not found for admin with id: {}", adminId);
            return Collections.emptyList();
        }

        logger.info("ProjectService: Projects found for admin with id: {}", adminId);
        List<Project> projects = projectsOpt.get();
        var result = enrichProjectsWithUsers(projects);
        logger.info("ProjectService: Projects found: {}", result);
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

        return projectRepository.create(entity);
    }

    @Override
    public Project getById(UUID id) {
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
    public Project updateById(Project entity) {
        return null;
    }

    private List<Project> enrichProjectsWithUsers(List<Project> projects) {
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

            Map<UUID, User> usersMap = handleUserIdAndUsersMap(projectUsers);

            return handleProjectsWithUserMap(projects, usersByProjectId, usersMap);
        }
        catch (CompletionException | InterruptedException | ExecutionException e) {
            logger.error("ProjectService: enrichProjectsWithUsers: Error enriching projects with users", e);
            throw new RuntimeException(e);
        }
    }
    private Map<UUID, User> handleUserIdAndUsersMap(List<ProjectUsersDto> projectUsers) throws ExecutionException, InterruptedException {
        Set<UUID> userIds = projectUsers.stream()
                .map(ProjectUsersDto::getUserId)
                .collect(Collectors.toSet());

        logger.info("ProjectService: enrichProjectsWithUsers: User IDs: {}", userIds.size());

        return userRepository.findAllByIdsAsync(new ArrayList<>(userIds))
                .get()
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private List<Project> handleProjectsWithUserMap(
            List<Project> projects, Map<UUID,
            List<ProjectUsersDto>> usersByProjectId,
            Map<UUID, User> usersMap) {
                projects.stream()
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
                    logger.info("ProjectService: enrichProjectsWithUsers: Project users: {}", project.getProjectUsers().size());
                    return project;
                })
                .toList();

        return projects;
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
                logger.info("ProjectService: enrichProjectWithUsers: Project users: {}", project.getProjectUsers().size());
            } else {
                logger.info("ProjectService: enrichProjectWithUsers: No users found for project");
                project.setProjectUsers(Collections.emptyList());
            }
            logger.info("ProjectService: enrichProjectWithUsers: Project: {}", project);
            return project;
        }
        catch (CompletionException | InterruptedException | ExecutionException e) {
            logger.error("ProjectService: enrichProjectWithUsers: Error enriching project with users", e);
            throw new RuntimeException(e);
        }
    }
}
