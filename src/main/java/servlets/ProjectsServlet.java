package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ProjectsController;
import models.dtos.ProjectDto;
import models.entities.Project;
import utils.StaticConstants;
import utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/projects")
public class ProjectsServlet extends HttpServlet {

    //private final Project
    private final ProjectsController controller;// = new ProjectsController();
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Utils utils;

    public ProjectsServlet() throws SQLException {
        super();
        this.controller = new ProjectsController();
        utils = new Utils();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        var path = req.getPathInfo();

        // Не получилось вычленить Id из req.getPathInfo(), разделяя строку на массив
        // если быть точнее, то /{id} воспринимался как несуществующий endpoint
        // поэтому пришлось использовать параметр запроса
        String id = req.getParameter("id");

        try {
            if (id == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.ID_REQUIRED_AD_PARAMETER_ERROR_MESSAGE));
                return;
            }

            boolean idValidation = utils.validateId(id);
            if (!idValidation) {

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE));
                return;
            }
            UUID projectId = UUID.fromString(id);

            ProjectDto project = controller.getProject(projectId);

            if (project != null) {

                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(project);

                resp.setStatus(HttpServletResponse.SC_OK);
                PrintWriter out = resp.getWriter();
                out.print(jsonResponse);
                out.flush();

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Project project = parseProjectFromRequest(req);

            ProjectDto createdProject = controller.create(project);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(resp.getWriter(), createdProject);

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            //resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.OPERATION_FAILED_ERROR_MESSAGE));
            e.printStackTrace();
        }
    }

    private Project parseProjectFromRequest(HttpServletRequest req) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(req.getInputStream(), Project.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String id = req.getParameter("id");

        try {

            if (id == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.ID_REQUIRED_AD_PARAMETER_ERROR_MESSAGE));
                return;
            }
            UUID projectId = UUID.fromString(id);

            boolean isDeleted = controller.delete(projectId);

            if (isDeleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(String.format("{\"message\":\"%s\"}", StaticConstants.REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE));
            }

        } catch (java.io.IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //throw new IllegalArgumentException(e);
            //resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE));
        }
    }
}
