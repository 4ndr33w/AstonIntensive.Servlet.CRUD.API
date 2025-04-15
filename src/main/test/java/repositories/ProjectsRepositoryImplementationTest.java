package repositories;

import configurations.JdbcConnection;
import models.entities.Project;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import repositories.interfaces.ProjectRepository;
import testUtils.Utils;
import utils.sqls.SqlQueryStrings;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsRepositoryImplementationTest {

    @Mock
    private JdbcConnection jdbcConnection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet generatedKeys;

    //@Mock
    private SqlQueryStrings sqlQueryStrings = new SqlQueryStrings();

    @InjectMocks
    private ProjectsRepositoryImplementation projectsRepositoryImplementation;


    @Test
    public void createTest() {
        try {
            ProjectRepository projectsRepository = new ProjectRepositoryNew();

            Project project = Utils.testProject1;


            var result = projectsRepository.createAsync(project);

            var resultProject = result.get();
            assertNotNull(result);
            var id = resultProject.getId();

            assertEquals(UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"), resultProject.getAdminId());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void getByIdTest() throws ExecutionException, InterruptedException, SQLException {
        ProjectRepository projectsRepository = new ProjectRepositoryNew();
        UUID id = UUID.fromString("9658455a-348b-4d4d-ad08-cb562da4f8c4");
        var result = projectsRepository.findByIdAsync(id)
                //.thenApplyAsync(ProjectMapper::toDto)
                .exceptionally(ex -> {
                    //System.err.println("Error fetching users: " + ex.getMessage());
                    return null; // Fallback
                }).get();
        var project = result;

        assertEquals("testProject2", result.getName());
    }

    @Test
    public void getProjectsByAdminId() {
        try {
            ProjectRepository projectsRepository = new ProjectRepositoryNew();

            UUID adminId = UUID.fromString("7f1111e0-8020-4de6-b15a-601d6903b9eb");

            var result = projectsRepository.findByAdminIdAsync(adminId);

            var resultProject = result.get();
            assertNotNull(result);

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addUserToProjectTest() throws ExecutionException, InterruptedException, SQLException {
        try {
            ProjectRepository projectsRepository = new ProjectRepositoryNew();
            UUID userId = UUID.fromString("5b99127d-27bd-44a4-8dfd-ffba74aecacb");
            UUID projectId = UUID.fromString("9658455a-348b-4d4d-ad08-cb562da4f8c4");

            var result = projectsRepository.addUserToProjectAsync(userId, projectId);

            var resultProject = result.get();

            assertEquals(0, resultProject.getProjectUsersIds().indexOf(userId));
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof SQLException) {
                System.err.println("Error adding user to project: " + e.getMessage());
            }
        }
    }

    @Test
    public void findByUserIdTest() throws ExecutionException, InterruptedException, SQLException {
        try {
            ProjectRepository projectsRepository = new ProjectRepositoryNew();
            UUID userId = UUID.fromString("d6df284c-b302-48c2-84d0-fd6d61c75f38");

            var result = projectsRepository.findByUserIdAsync(userId);

            var resultProject = result.get();

            assertNotNull(result);
        }
        catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof SQLException) {
                System.err.println("Error adding user to project: " + e.getMessage());
            }
        }
    }

    @Test
    public void deleteCorrectTest() {
        UUID id = UUID.fromString("9036beb3-9b8f-4dc8-b748-25b3ccdb71e9");

        try {
            ProjectRepository projectRepository = new ProjectsRepositoryImplementation();
            var result = projectRepository.deleteAsync(id).get();

            assertTrue(result);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void removeUserFromProjectTest() throws ExecutionException, InterruptedException, SQLException {
        try {
            ProjectRepository projectsRepository = new ProjectsRepositoryImplementation();
            UUID userId = UUID.fromString("5b99127d-27bd-44a4-8dfd-ffba74aecacb");
            UUID projectId = UUID.fromString("9658455a-348b-4d4d-ad08-cb562da4f8c4");

            var result = projectsRepository.RemoveUserFromProjectAsync(userId, projectId);

            var resultProject = result.get();

            assertEquals(0, resultProject.getProjectUsersIds().indexOf(userId));
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof SQLException) {
                System.err.println("Error adding user to project: " + e.getMessage());
            }
        }
    }
}
