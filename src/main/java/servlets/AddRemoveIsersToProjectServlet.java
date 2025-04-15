package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ProjectsController;
import models.dtos.ProjectDto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/projects/users")
public class AddRemoveIsersToProjectServlet extends HttpServlet {

    private final ProjectsController projectController;

    public AddRemoveIsersToProjectServlet() throws SQLException {
        this.projectController = new ProjectsController();
    }

    /*@Override
    public void init() {
        this.projectController = new ProjectsController();
    }*/

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            UUID projectId = UUID.fromString(req.getParameter("projectId"));
            UUID userId = UUID.fromString(req.getParameter("userId"));

            ProjectDto result = projectController.addUserToProject(userId, projectId);

            resp.setContentType("application/json");
            new ObjectMapper().writeValue(resp.getWriter(), result);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // Обработка ошибки
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try {
            UUID projectId = UUID.fromString(req.getParameter("projectId"));
            UUID userId = UUID.fromString(req.getParameter("userId"));

            ProjectDto result = projectController.removeUserFromProject(userId, projectId);

            resp.setContentType("application/json");
            new ObjectMapper().writeValue(resp.getWriter(), result);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // Обработка ошибки
        }
    }
}
