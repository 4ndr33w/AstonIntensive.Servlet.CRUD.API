package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.entities.Project;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import repositories.interfaces.ProjectRepository;
import testUtils.Utils;
import utils.mappers.ProjectMapper;
import utils.sqls.SqlQueryStrings;

import static utils.mappers.ProjectMapper.toDto;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    public void getByIdTest() throws ExecutionException, InterruptedException, SQLException {
        ProjectRepository projectsRepository = new ProjectsRepository();
        UUID id = UUID.fromString("d2d4b92a-f9db-4001-9c04-26b150c75310");
        var result = projectsRepository.findByIdAsync(id)
                .thenApplyAsync(ProjectMapper::toDto)
                .exceptionally(ex -> {
                    //System.err.println("Error fetching users: " + ex.getMessage());
                    return null; // Fallback
                }).get();

        assertEquals("testProject1", result.getName());
    }

    @Test
    public void getProjectsByAdminId() {
        try {
            ProjectRepository projectsRepository = new ProjectsRepository();

            UUID adminId = UUID.fromString("7f1111e0-8020-4de6-b15a-601d6903b9eb");

            var result = projectsRepository.findByAdminIdAsync(adminId);

            var resultProject = result.get();
            assertNotNull(result);

        } catch (ExecutionException | InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addUserToProjectTest() throws ExecutionException, InterruptedException, SQLException {
        try {
            ProjectRepository projectsRepository = new ProjectsRepository();
            var project = toDto(Utils.testProject1);
            UUID userId = UUID.fromString("ce3f5f07-20ee-4976-b17c-4460604b5a1b");
            UUID projectId = UUID.fromString("9658455a-348b-4d4d-ad08-cb562da4f8c4");


            String schema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
            String projectsTable = PropertiesConfiguration.getProperties().getProperty("jdbc.projects-table");
            String projectUsersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.project-users-table");
            sqlQueryStrings = new SqlQueryStrings();
            String tableName = String.format("%s.%s", schema, projectUsersTable);
            String queryString = sqlQueryStrings.addUserIntoProjectString(tableName, projectId.toString(), userId.toString());
            //var sqlString = sqlQueryStrings.addUserIntoProjectString();

            //project.setId(projectId);

            var result = projectsRepository.addUserToProjectAsync(userId, projectId);

            var resultProject = result.get();

            assertEquals(0, resultProject.getProjectUsersIds().indexOf(userId));
        } catch (ExecutionException | InterruptedException | SQLException e) {
            if (e.getCause() instanceof SQLException) {
                System.err.println("Error adding user to project: " + e.getMessage());
            }
        }
    }
}
