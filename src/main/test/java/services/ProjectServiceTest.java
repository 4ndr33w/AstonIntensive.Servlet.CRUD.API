package services;

import models.dtos.ProjectUsersDto;
import models.entities.Project;
import org.junit.Test;
import org.mockito.Mockito;
import repositories.ProjectUsersRepositoryImpl;
import repositories.interfaces.ProjectUserRepository;
import services.interfaces.ProjectService;
import testUtils.Utils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectServiceTest {

    @Test
    public void fillProjectsWithUsers_Success() throws SQLException {
        // Given
        ProjectUsersRepositoryImpl projectUserRepository = Mockito.mock(ProjectUsersRepositoryImpl.class);
        ProjectServiceImplNew projectService = new ProjectServiceImplNew();

        // Создаём тестовые проекты
        UUID projectId1 = UUID.randomUUID();
        UUID projectId2 = UUID.randomUUID();
        Project project1 = Utils.testProject1;
        Project project2 = Utils.testProject2;
        List<Project> projects = Arrays.asList(project1, project2);

        // Создаём тестовых пользователей для проектов
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        ProjectUsersDto dto1 = new ProjectUsersDto(projectId1, userId1);
        ProjectUsersDto dto2 = new ProjectUsersDto(projectId1, userId2);
        ProjectUsersDto dto3 = new ProjectUsersDto(projectId2, userId3);
        List<ProjectUsersDto> projectUsers = Arrays.asList(dto1, dto2, dto3);

        // Мокаем репозиторий
        Mockito.when(projectUserRepository.findByProjectIds(anyList()))
                .thenReturn(CompletableFuture.completedFuture(projectUsers));

        // Мокаем fillUsersForProject (если он приватный, можно использовать Reflection или изменить доступ для тестов)
        ProjectServiceImplNew spyService = Mockito.spy(projectService);
        Mockito.doReturn(CompletableFuture.completedFuture(Utils.testProject1))
                .when(spyService).fillUsersForProject(project1, Arrays.asList(userId1, userId2));
        Mockito.doReturn(CompletableFuture.completedFuture(Utils.testProject2))
                .when(spyService).fillUsersForProject(project2, Collections.singletonList(userId3));

        // When
        CompletableFuture<List<Project>> resultFuture = spyService.fillProjectsWithUsers(projects);
        List<Project> result = resultFuture.join(); // Дожидаемся выполнения

        // Then
        assertEquals(2, result.size());

        // Проверяем, что проекты заполнены пользователями
        Project filledProject1 = result.get(0);
        assertEquals(projectId1, filledProject1.getId());
        assertEquals(2, filledProject1.getProjectUsers().size()); // Должно быть 2 пользователя
        assertTrue(filledProject1.getProjectUsers().containsAll(Set.of(userId1, userId2)));

        Project filledProject2 = result.get(1);
        assertEquals(projectId2, filledProject2.getId());
        assertEquals(1, filledProject2.getProjectUsers().size()); // Должен быть 1 пользователь
        assertTrue(filledProject2.getProjectUsers().contains(userId3));

        // Проверяем, что findByProjectIds вызвался с правильными аргументами
        Mockito.verify(projectUserRepository).findByProjectIds(Arrays.asList(projectId1, projectId2));
    }

    @Test
    public void fillProjectsWithUsersTests() throws SQLException {
        ProjectUsersRepositoryImpl projectUserRepository = new ProjectUsersRepositoryImpl();
        ProjectServiceImplNew projectService = new ProjectServiceImplNew();

        var project = Utils.testProject2;
        var user1 = Utils.testUser1;
        var user2 = Utils.testUser2;

        var result = projectService.fillUsersForProject(project, List.of(user1.getId(), user2.getId()));

        var test = result.join();

        assertEquals(2, test.getProjectUsers().size());
    }

    @Test
    public void fillProjectsWithUsersTests2() throws SQLException, ExecutionException, InterruptedException {
        ProjectUsersRepositoryImpl projectUserRepository = new ProjectUsersRepositoryImpl();
        ProjectServiceImplNew projectService = new ProjectServiceImplNew();

        var project = Utils.testProject2;
        var project2 = Utils.testProject2;
        var list = List.of(project, project2);
        var test = projectService.fillProjectsWithUsers( list );

        var result = test.get();
        assertEquals(2, result.size());
    }

    @Test
    public void projectUsersIdsTests2() throws SQLException, ExecutionException, InterruptedException {
        ProjectUsersRepositoryImpl projectUserRepository = new ProjectUsersRepositoryImpl();
        ProjectServiceImplNew projectService = new ProjectServiceImplNew();
        ProjectUsersRepositoryImpl projectRepository = new ProjectUsersRepositoryImpl();

        var project = Utils.testProject2;
        var project2 = Utils.testProject2;
        var list = List.of(project.getId(), project2.getId());
        var test = projectRepository.findByProjectIds(list);

        var result = test.get();
        assertEquals(2, result.size());
    }
}
