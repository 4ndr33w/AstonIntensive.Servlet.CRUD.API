package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ProjectControllerSynchronous;
import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Сервлет с эндпойнтами для добавления / удаления
 * пользователей в / из проекта
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/projects/users")
public class AddRemoveUsersToProjectServlet extends HttpServlet {

    private final ProjectControllerInterface projectController;
    //private final ProjectsController projectController;

    public AddRemoveUsersToProjectServlet() {
        this.projectController = new ProjectControllerSynchronous();
    }

    /**
     * HTTP POST запрос
     * метод добавляет пользователя в проект
     * <p>
     *     метод принимает два параметра в адресной строке:
     *     <ul>
     *         <li>{@code projectId}</li>
     *         <li>{@code userId}</li>
     *     </ul>
     * </p>
     * @param req
     * @param resp
     *
     * @return 200 OK
     * @return 400 Bad Request
     *
     * @throws Exception
     */
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
        }
    }

    /**
     * HTTP DELETE запрос
     * метод удаляет пользователя в проект
     * <p>
     *     метод принимает два параметра:
     *     <ul>
     *         <li>{@code projectId}</li>
     *         <li>{@code userId}</li>
     *     </ul>
     * </p>
     * @param req
     * @param resp
     *
     * @return 200 OK
     * @return 400 Bad Request
     *
     * @throws Exception
     */
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
        }
    }
}
