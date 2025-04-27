package services.synchronous;

import jdk.jfr.Description;
import models.entities.Project;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import testUtils.Utils;

import utils.exceptions.ProjectNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectsServiceTest extends Utils {

    @Mock
    private repositories.interfaces.synchronous.ProjectRepoSynchro projectRepository;
    @Mock private repositories.interfaces.synchronous.ProjectUserRepositorySynchro projectUserRepository;
    @InjectMocks
    services.synchronous.ProjectsService projectService;

    //---------------------------------------------------------------
    // Get By User Id
    //---------------------------------------------------------------

    @Test
    @Description("Успешко возвращаем список проектов по id пользователя")
    public void getByUserId_ShouldReturnProjectsWhenExist() {

        UUID userId = UUID.randomUUID();
        List<Project> expectedProjects = List.of(
                testProject1
        );

        when(projectUserRepository.findByUserId(userId))
                .thenReturn(Optional.of(List.of(testProjectUserDto1)));

        when(projectRepository.findByProjectIds(List.of(testProjectUserDto1.getProjectId())))
                .thenReturn(Optional.of(expectedProjects));

        List<Project> result = projectService.getByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());

    }

    @Test(expected = ProjectNotFoundException.class)
    @Description("Успешко возвращаем пустой список, если пользователь не имеет проектов")
    public void getProjectsByUserIdAsync_ShouldReturnEmptyList_WhenUserHasNoProjects() {

        UUID userId = UUID.randomUUID();

        when(projectUserRepository.findByUserId(userId))
                .thenReturn(Optional.of(List.of(testProjectUserDto1)));

        when(projectRepository.findByProjectIds(List.of(testProjectUserDto1.getProjectId())))
                .thenReturn(Optional.empty());

        projectService.getByUserId(userId);
    }

    //---------------------------------------------------------------
    // Get By User Id
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // Get By Admin Id
    //---------------------------------------------------------------

}
