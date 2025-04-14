package services;

import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsServiceTest {

    ProjectsService projectsService;

    @Test
    public void getAllTest() throws SQLException, ExecutionException, InterruptedException {

        projectsService = new ProjectsService();
        var test = projectsService.getByUserIdAsync(UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"));
        var result = test.get();

        assertNotNull(result);
    }
}
