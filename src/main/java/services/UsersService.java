package services;

import models.dtos.ProjectDto;
import models.dtos.ProjectUsersDto;
import models.entities.Project;
import models.entities.User;
import repositories.ProjectUsersRepository;
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
    private final ProjectUsersRepository projectUsersRepository;

    public UsersService() throws SQLException {
        this.userRepository = new UsersRepositoryImplementation();
        this.projectsRepository = new ProjectsRepositoryImplementation();
        this.projectUsersRepository = new ProjectUsersRepository();
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

    public CompletableFuture<User> enrichUserWithProjects(User user) {
        return projectsRepository.findByUserIdAsync(user.getId())
                .thenCompose(projects -> {
                    if (projects.isEmpty()) {
                        return CompletableFuture.completedFuture(user);
                    }

                    // Получаем ID всех проектов для пакетной загрузки
                    List<UUID> projectIds = projects.stream()
                            .map(Project::getId)
                            .collect(Collectors.toList());

                    // Пакетно загружаем полные данные проектов и их пользователей
                    return projectsRepository.findAllByIdsAsync(projectIds)
                            .thenCompose(loadedProjects -> {
                                if (loadedProjects.isEmpty()) {
                                    return CompletableFuture.completedFuture(user);
                                }

                                // Загружаем всех пользователей для всех проектов одним запросом
                                return projectUsersRepository.findByProjectIds(projectIds)
                                        .thenApply(projectUsers -> {
                                            // Группируем пользователей по projectId
                                            Map<UUID, List<UUID>> usersByProjectId = projectUsers.stream()
                                                    .collect(Collectors.groupingBy(
                                                            ProjectUsersDto::getProjectId,
                                                            Collectors.mapping(ProjectUsersDto::getUserId, Collectors.toList())
                                                    ));

                                            // Преобразуем проекты в DTO и устанавливаем списки пользователей
                                            List<ProjectDto> projectDtos = loadedProjects.stream()
                                                    .map(project -> {
                                                        ProjectDto dto = ProjectMapper.toDto(project);
                                                        dto.setProjectUsersIds(
                                                                usersByProjectId.getOrDefault(project.getId(), Collections.emptyList())
                                                        );
                                                        return dto;
                                                    })
                                                    .collect(Collectors.toList());

                                            user.setProjects(projectDtos);
                                            return user;
                                        });
                            });
                })
                .exceptionally(ex -> {
                    //log.error("Failed to enrich user {} with projects: {}", user.getId(), ex.getMessage());
                    return user; // Возвращаем исходного пользователя в случае ошибки
                });
    }
    /*private CompletableFuture<User> enrichUserWithProjects(User user) {
        // Получаем все проекты пользователя (как админа и как участника)
        return projectsRepository.findByUserIdAsync(user.getId())
                .thenCompose(adminProjects ->
                        projectsRepository.findProjectsByUserIdIfUserNotProjectAdmin(user.getId())
                                .thenCombine(projectUsersRepository.findByUserId(user.getId()),
                                        (memberProjects, userProjectRelations) -> {
                                            // Объединяем все проекты пользователя
                                            Set<Project> allProjects = new HashSet<>();
                                            allProjects.addAll(adminProjects);
                                            allProjects.addAll(memberProjects);

                                            // Собираем ID всех проектов
                                            List<UUID> projectIds = allProjects.stream()
                                                    .map(Project::getId)
                                                    .collect(Collectors.toList());

                                            return new ProjectData(allProjects, projectIds, userProjectRelations);
                                        })
                )
                .thenCompose(projectData -> {
                    // Загружаем всех пользователей для всех проектов одним запросом
                    return projectUsersRepository.findByProjectIds(projectData.projectIds)
                            .thenCombine(userRepository.findAllByIdsAsync(
                                    projectData.userProjectRelations.stream()
                                            .map(ProjectUsersDto::getUserId)
                                            .distinct()
                                            .collect(Collectors.toList())
                            ), (projectUsers, users) -> {
                                // Группируем пользователей по projectId
                                Map<UUID, List<UUID>> usersByProjectId = projectUsers.stream()
                                        .collect(Collectors.groupingBy(
                                                ProjectUsersDto::getProjectId,
                                                Collectors.mapping(ProjectUsersDto::getUserId, Collectors.toList())
                                        ));

                                // Преобразуем проекты в DTO и устанавливаем списки пользователей
                                List<ProjectDto> projectDtos = projectData.projects.stream()
                                        .map(project -> {
                                            ProjectDto dto = ProjectMapper.toDto(project);
                                            dto.setProjectUsersIds(
                                                    usersByProjectId.getOrDefault(project.getId(), Collections.emptyList())
                                            );
                                            return dto;
                                        })
                                        .collect(Collectors.toList());

                                user.setProjects(projectDtos);
                                return user;
                            });
                })
                .exceptionally(ex -> {
                    log.error("Failed to enrich user with projects", ex);
                    return user; // Возвращаем пользователя без проектов в случае ошибки
                });
    }*/

    /*public CompletableFuture<User> enrichUserWithProjects(User user) {
        return projectsRepository.findByUserIdAsync(user.getId())
                .thenCompose(projects -> {
                    if (projects.isEmpty()) {
                        return CompletableFuture.completedFuture(user);
                    }

                    // Загружаем все проекты
                    List<CompletableFuture<Optional<Project>>> projectFutures = projects.stream()
                            .map(project -> {
                                try {
                                    return projectsRepository.findByIdAsync(project.getId())
                                            .thenApply(Optional::ofNullable)
                                            .exceptionally(ex -> {
                                                System.err.println("Error loading project: " + ex.getMessage());
                                                return Optional.empty();
                                            });
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .toList();

                    return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                            .thenCompose(v -> {
                                // Получаем список загруженных проектов (без null)
                                List<Project> loadedProjects = projectFutures.stream()
                                        .map(CompletableFuture::join)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .toList();

                                // Загружаем пользователей для всех проектов
                                return projectUsersRepository.findByProjectIds(
                                                loadedProjects.stream()
                                                        .map(Project::getId)
                                                        .toList())
                                        .thenApply(projectUsers -> {
                                            // Группировка и маппинг как в предыдущем примере
                                            // ...
                                            return user;
                                        });
                            });
                })
                .exceptionally(ex -> {
                    System.err.println("Error enriching user with projects: " + ex.getMessage());
                    return user;
                });
    }*/

    /*private CompletableFuture<User> enrichUserWithProjects(User user) {
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
                                                                .toList();

                                                        projectDto.setProjectUsersIds(users.stream().map(User::getId).collect(Collectors.toList()));
                                                        return projectDto;
                                                    });
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            )
                            .toList();

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
    }*/
    /*private CompletableFuture<User> enrichUserWithProjects(User user) {
        return projectsRepository.findByUserIdAsync(user.getId())
                .thenCompose(projects -> {
                    if (projects.isEmpty()) {
                        return CompletableFuture.completedFuture(user);
                    }
                    try {
                        // Загружаем полные данные проектов параллельно
                        List<CompletableFuture<ProjectDto>> projectFutures = projects.stream()
                                .map(project -> projectsRepository.findByIdAsync(project.getId())
                                        .toList());

                        return CompletableFuture.allOf(projectFutures.toArray(new CompletableFuture[0]))
                                .thenCompose(v -> {
                                    // Получаем список загруженных проектов
                                    List<ProjectDto> loadedProjects = projectFutures.stream()
                                            .map(CompletableFuture::join)
                                            .toList();

                                    // Загружаем пользователей для всех проектов одним запросом
                                    List<UUID> projectIds = loadedProjects.stream()
                                            .map(ProjectDto::getId)
                                            .toList();

                                    return projectUsersRepository.findByProjectIds(projectIds)
                                            .thenApply(projectUsers -> {
                                                // Группируем пользователей по projectId
                                                Map<UUID, List<UUID>> usersByProjectId = projectUsers.stream()
                                                        .collect(Collectors.groupingBy(
                                                                ProjectUsersDto::getProjectId,
                                                                Collectors.mapping(ProjectUsersDto::getUserId, Collectors.toList())
                                                        );

                                                // Обогащаем проекты списками пользователей
                                                loadedProjects.forEach(project ->
                                                        project.setProjectUsersIds(
                                                                usersByProjectId.getOrDefault(project.getId(), Collections.emptyList())
                                                        )
                                                );

                                                user.setProjects(loadedProjects);
                                                return user;
                                            });
                                });
                    }
                    catch (SQLException e) {
                        throw new RuntimeException("Error fetching users");
                    }

                });
    }*/



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

    private static class ProjectData {
        public final Set<Project> projects;
        public final List<UUID> projectIds;
        public final List<ProjectUsersDto> userProjectRelations;

        ProjectData(Set<Project> projects, List<UUID> projectIds, List<ProjectUsersDto> userProjectRelations) {
            this.projects = projects;
            this.projectIds = projectIds;
            this.userProjectRelations = userProjectRelations;
        }
    }
}
