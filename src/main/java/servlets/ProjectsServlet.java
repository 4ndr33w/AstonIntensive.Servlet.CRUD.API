package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ProjectsController;
import models.dtos.ProjectDto;
import models.dtos.UserDto;
import utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        var path = req.getPathInfo();

        // Не получилось вычленить Id из req.getPathInfo(), разделяя строку на массив
        // если быть точнее, то /{id} воспринимался как несуществующий endpoint
        // поэтому пришлось использовать параметр запроса
        String id = req.getParameter("id");
        if (id == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Требуется указать Id\"}");
            return;
        }

        boolean idValidation = utils.validateId(id);
        if(!idValidation) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат Id\"}");
            return;
        }
        try {
            UUID userId = UUID.fromString(id);

            ProjectDto projects = controller.getByProjectId(userId);

            if (projects != null) {

                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(projects);

                resp.setStatus(HttpServletResponse.SC_OK);
                PrintWriter out = resp.getWriter();
                out.print(jsonResponse);
                out.flush();

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Пользователь не найден\"}");
            }
        } catch (SQLException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
