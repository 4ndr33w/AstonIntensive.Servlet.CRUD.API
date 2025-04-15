package services.synchronous;

import models.dtos.ProjectUsersDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.ProjectUserRepository;
import repositories.interfaces.UserRepository;
import services.UsersService;
import services.interfaces.ProjectService;
import testUtils.Utils;
import utils.mappers.ProjectMapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import utils.mappers.UserMapper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectsServiceTest extends Utils{

    @Mock
    private repositories.synchronous.ProjectsRepository projectRepository;

    @Mock
    private repositories.ProjectUsersRepositoryImpl projectUserRepository;

    @Mock
    private repositories.UsersRepositoryImplementation userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private services.synchronous.ProjectsService projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getByUserId_ShouldReturnProjectsWithUsers() {

        try {

            when(projectRepository.findByUserId(userId)).thenReturn(Optional.of(projects));

            when(projectUserRepository.findByProjectIds(Arrays.asList(projectId1, projectId2)).get())
                    .thenReturn(Arrays.asList(dto1, dto2, dto3));

            when(userRepository.findAllByIdsAsync(Arrays.asList(user1Id, user2Id)).get())
                    .thenReturn(List.of(testUser1, testUser2));

            when(UserMapper.toDto(testUser1)).thenReturn(userDto1);
            when(UserMapper.toDto(testUser2)).thenReturn(userDto2);

            List<Project> result = projectService.getByUserId(userId);

            assertEquals(2, result.size());
            assertEquals(2, result.get(0).getProjectUsers().size()); // Project1 has 2 users
            assertEquals(1, result.get(1).getProjectUsers().size()); // Project2 has 1 user
            verify(projectRepository).findByUserId(userId);
            verify(projectUserRepository).findByProjectIds(Arrays.asList(projectId1, projectId2));
            verify(userRepository).findAllByIdsAsync(Arrays.asList(user1Id, user2Id));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getByUserId_ShouldReturnEmptyListWhenNoProjects() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(projectRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        List<Project> result = projectService.getByUserId(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(projectRepository).findByUserId(userId);
        verifyNoInteractions(projectUserRepository, userRepository, projectMapper);
    }

    @Test
    public void getByAdminId_ShouldReturnProjectsWithUsers() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        try {
            // Mock project
            Project project = testProject1;
            when(projectRepository.findByAdminId(adminId)).thenReturn(Optional.of(Collections.singletonList(project)));

            // Mock project users
            ProjectUsersDto dto = new ProjectUsersDto(projectId, userId);
            when(projectUserRepository.findByProjectIds(Collections.singletonList(projectId)).join())
                    .thenReturn(Collections.singletonList(dto));

            User user = testUser1;
            when(userRepository.findAllByIdsAsync(Collections.singletonList(userId)).join())
                    .thenReturn(Collections.singletonList(user));

            UserDto userDto = userDto1;
            when(UserMapper.toDto(user)).thenReturn(userDto);

            List<Project> result = projectService.getByAdminId(adminId);

            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getProjectUsers().size());
            assertEquals(userDto, result.get(0).getProjectUsers().get(0));
            verify(projectRepository.findByAdminId(adminId).get());
            verify(projectUserRepository.findByProjectIds(Collections.singletonList(projectId)).get());
            verify(userRepository.findAllByIdsAsync(Collections.singletonList(userId)).get());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
/*
    @Test
    void getById_ShouldReturnProjectWithUsers() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Mock project
        Project project = new Project(projectId, "Test Project");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Mock project users
        ProjectUsersDto dto = new ProjectUsersDto(projectId, userId);
        when(projectUserRepository.findByProjectId(projectId))
                .thenReturn(Collections.singletonList(dto));

        // Mock user
        User user = new User(userId, "Test User");
        when(userRepository.findAllByIds(Collections.singletonList(userId)))
                .thenReturn(Collections.singletonList(user));

        // Mock mapper
        UserDto userDto = new UserDto(userId, "Test User");
        when(projectMapper.toUserDto(user)).thenReturn(userDto);

        // Act
        Project result = projectService.getById(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(projectId, result.getId());
        assertEquals(1, result.getProjectUsers().size());
        assertEquals(userDto, result.getProjectUsers().get(0));
        verify(projectRepository).findById(projectId);
        verify(projectUserRepository).findByProjectId(projectId);
        verify(userRepository).findAllByIds(Collections.singletonList(userId));
    }*/

    @Test
    public void testGetByAdminId_SingleProject() {
        // Подготовка
        UUID adminId = UUID.fromString("d6df284c-b302-48c2-84d0-fd6d61c75f38");
        UUID projectId = testProject1.getId();
        UUID userId = testUser1.getId();

        Project project = testProject1;
        List<Project> projects = Collections.singletonList(project);

        ProjectUsersDto projectUserDto = testProjectUserDto1;
        List<ProjectUsersDto> projectUsers = Collections.singletonList(projectUserDto);

        User user = testUser1;
        List<User> users = Collections.singletonList(user);

        when(projectRepository.findByAdminId(adminId)).thenReturn(Optional.of(projects));
        when(projectUserRepository.findByProjectIds(Collections.singletonList(projectId))).thenReturn(CompletableFuture.completedFuture(projectUsers));
        when(userRepository.findAllByIdsAsync(Collections.singletonList(userId))).thenReturn(CompletableFuture.completedFuture(users));

        List<Project> result = projectService.getByAdminId(adminId);

        assertEquals(1, result.size());
        Project enrichedProject = result.get(0);
        assertEquals(projectId, enrichedProject.getId());
        assertEquals(1, enrichedProject.getProjectUsers().size());
        UserDto userDto = enrichedProject.getProjectUsers().get(0);
        assertEquals(userId, userDto.getId());
        assertEquals(testUser1.getUserName(), userDto.getUserName());
    }

    @Test
    public void testGetByAdminId_MultipleProjects() {
        // Подготовка данных
        UUID adminId = UUID.randomUUID();
        UUID projectId1 = UUID.randomUUID();
        UUID projectId2 = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        // Создание тестовых проектов
        Project project1 = testProject1;
        Project project2 = testProject2;
        List<Project> projects = List.of(project1, project2);

        // Создание связей проекта с пользователями
        ProjectUsersDto projectUser1 = testProjectUserDto1;
        ProjectUsersDto projectUser2 = testProjectUserDto2;
        ProjectUsersDto projectUser3 = testProjectUserDto3;
        List<ProjectUsersDto> projectUsers = List.of(projectUser1, projectUser2, projectUser3);

        // Создание пользователей
        User user1 = testUser1;
        User user2 = testUser2;
        List<User> users = List.of(user1, user2);

        // Настройка моков
        when(projectRepository.findByAdminId(adminId)).thenReturn(Optional.of(projects));
        when(projectUserRepository.findByProjectIds(List.of(projectId1, projectId2))).thenReturn(CompletableFuture.completedFuture(projectUsers));
        when(userRepository.findAllByIdsAsync(List.of(userId1, userId2))).thenReturn(CompletableFuture.completedFuture(users));

        // Выполнение метода
        List<Project> result = projectService.getByAdminId(adminId);

        // Проверка результатов
        assertEquals(2, result.size());

        Project enrichedProject1 = result.get(0);
        Project enrichedProject2 = result.get(1);

        assertEquals(2, enrichedProject1.getProjectUsers().size());
        assertEquals(1, enrichedProject2.getProjectUsers().size());

        // Проверка названий проектов
        assertTrue(result.stream().map(Project::getName).toList()
                .containsAll(List.of(testProject1.getName(), testProject2.getName())));

        // Проверка пользователей в проектах
        assertEquals(testUser1.getUserName(), enrichedProject1.getProjectUsers().stream()
                .map(UserDto::getUserName)
                .toList()
                .get(0));
        var test = result.stream().map(Project::getName).collect(Collectors.toList());
        System.out.println(test);

        assertEquals(testUser2.getUserName(), enrichedProject1.getProjectUsers().stream()
                .map(UserDto::getUserName)
                .toList()
                .get(1));

        assertEquals(testUser3.getUserName(), enrichedProject2.getProjectUsers().stream()
                .map(UserDto::getUserName)
                .toList()
                .get(0));


    }
}
