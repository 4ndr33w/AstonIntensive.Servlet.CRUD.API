package controllers;

import models.dtos.ProjectDto;
import models.entities.Project;
import org.junit.Test;
import testUtils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsControllerTest {

    ProjectsController controller;

    @Test
    public void getByAdminIdTest() throws SQLException, ExecutionException, InterruptedException {
        ProjectsController projectController = new ProjectsController();
        UUID userId = UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa0");
        var result = projectController.getByAdminId(userId);

        var reslt = result;

        assertNotNull(reslt);
    }

}
