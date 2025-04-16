package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ProjectControllerSynchronous;
import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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


    public AddRemoveUsersToProjectServlet() {
        //this.projectController = new ProjectControllerSynchronous();
        this.projectController = new controllers.ProjectsController();
    }

    /**
     * HTTP POST запрос
     * метод добавляет пользователя в проект
     * <p>
     *     метод принимает два параметра в адресной строке:
     *     <ul>
     *         <li>{@code projectid}</li>
     *         <li>{@code userid}</li>
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Logger logger = LoggerFactory.getLogger(AddRemoveUsersToProjectServlet.class);
        try {
            UUID projectId = UUID.fromString(req.getParameter("projectid"));
            UUID userId = UUID.fromString(req.getParameter("userid"));

            ProjectDto result = projectController.addUserToProject(userId, projectId);

            logger.info("User with id {} was added to project with id {}", userId, projectId);
            resp.setContentType("application/json");
            new ObjectMapper().writeValue(resp.getWriter(), result);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(e.getCause().getMessage());
            logger.error(e.getCause().getMessage());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(e.getCause().getMessage());
            resp.getWriter().write("\n");
            resp.getWriter().write(e.getMessage());
            resp.getWriter().write("\n");
            logger.error(e.getCause().getMessage());
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

        Logger logger = LoggerFactory.getLogger(AddRemoveUsersToProjectServlet.class);
        try {
            UUID projectId = UUID.fromString(req.getParameter("projectId"));
            UUID userId = UUID.fromString(req.getParameter("userId"));

            ProjectDto result = projectController.removeUserFromProject(userId, projectId);

            logger.info("User with id {} was removed from project with id {}", userId, projectId);
            resp.setContentType("application/json");
            new ObjectMapper().writeValue(resp.getWriter(), result);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
