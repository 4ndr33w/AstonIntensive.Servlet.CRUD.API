package repositories;

import configurations.JdbcConnection;
import models.entities.Project;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import repositories.interfaces.ProjectRepository;
import testUtils.Utils;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryStrings;

import static utils.mappers.ProjectMapper.toDto;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsRepositoryTest {

    @Mock
    private JdbcConnection jdbcConnection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet generatedKeys;

    //@Mock
    private SqlQueryStrings sqlQueryStrings = new SqlQueryStrings();

    @InjectMocks
    private ProjectsRepository projectsRepository;



    @Test
    public void createTest() {
        try {
            ProjectRepository projectsRepository = new ProjectsRepository();

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
    public void getByIdTest() throws ExecutionException, InterruptedException {
        ProjectRepository projectsRepository = new ProjectsRepository();
        UUID id = UUID.fromString("d2d4b92a-f9db-4001-9c04-26b150c75310");
        var result = projectsRepository.findByIdAsync(id)
                .thenApplyAsync(ProjectMapper::toDto)
                .exceptionally(ex -> {
                    //System.err.println("Error fetching users: " + ex.getMessage());
                    return null; // Fallback
                }).get();

        var project = result;
        assertEquals("testProject1", result.getName());

    }
}
