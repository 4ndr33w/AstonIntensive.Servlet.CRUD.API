package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import controllers.ProjectControllerSynchronous;
import controllers.ProjectsController;
import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StaticConstants;
import utils.Utils;
import utils.exceptions.ProjectNotFoundException;
import utils.mappers.ProjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

/**
 * Сервлет представляет эндпойнт
 * для обработки запросов по {@code CRUD} операциям
 * с проектами {@code Project}
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/projects")
public class ProjectsServlet extends HttpServlet {

    Logger logger = LoggerFactory.getLogger(ProjectsServlet.class);

    private final ProjectControllerInterface projectController;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Utils utils;

    public ProjectsServlet() {
        super();
        this.projectController = new ProjectsController();
        utils = new Utils();
    }

    /**
     * HTTP GET запрос
     * метод возвращает DTO-объект проекта {@code ProjectDto},
     * включая {@code id} пользователей, участвующих в проекте,
     *
     * <p>
     *     метод принимает параметр в адресной строке:
     *     <ul>
     *         <li>{@code Id}</li>
     *     </ul>
     * </p>
     *
     * @param req
     * @param resp
     * @return 200 OK
     * @return 400 Bad Request
     * @return 404 Not Found
     * @throws RuntimeException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Logger logger = LoggerFactory.getLogger(ProjectsServlet.class);

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
                logger.error(String.format("Servlet: Error. Invalid ID format. Request path: %s", path));
                return;
            }

            boolean idValidation = utils.validateId(id);
            if (!idValidation) {

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE));
                logger.error(String.format("Servlet: Error. Invalid ID format. Request path: %s", path));
                return;
            }
            UUID projectId = UUID.fromString(id);

            ProjectDto project = projectController.getProject(projectId);

            if (project != null) {

                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(project);

                resp.setStatus(HttpServletResponse.SC_OK);
                PrintWriter out = resp.getWriter();
                out.print(jsonResponse);
                out.flush();
                logger.info(String.format("Servlet: Request path: %s. Response: %s", path, jsonResponse));

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE));
                logger.error(String.format("Servlet: Error. Project not found. Request path: %s", path));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INTERNAL_SERVER_ERROR_MESSAGE));
            logger.error(String.format("Servlet: Error. Request path: %s\nException: %s", path, e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    /**
     * HTTP POST запрос
     * Создание нового проекта
     * метод возвращает DTO-объект проекта {@code ProjectDto}
     *
     * <pre>{@code
     * {
     *  "name": "Default Project"
     *  "description": "Default Project Description"
     *  "adminId": "41096054-cbd7-4308-8411-905ae6f03aa6"
     *  "projectStatus": 0
     *  "image": null
     * }
     * }</pre>
     *
     * @param req
     * @param resp
     * @return 201 Created
     * @return 400 Bad Request
     * @return 500 Internal Server Error
     * @throws RuntimeException
     * @throws IllegalArgumentException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Project project = parseProjectFromRequest(req);

            logger.info("Servlet: Парсинг выполнен\n Выполнение метода создания проекта");
            ProjectDto createdProject = projectController.create(project);
            logger.info(String.format("Servlet: Проект создан. Created project: %s", objectMapper.writeValueAsString(createdProject)));
            resp.setStatus(HttpServletResponse.SC_CREATED);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.writeValue(resp.getWriter(), createdProject);

            logger.info(String.format("Servlet: Отображение данных на клиент. Response: %s", objectMapper.writeValueAsString(createdProject)));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            logger.error(String.format("Servlet: Error %s\nException: %s", IllegalArgumentException.class.getName(), e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            //resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.OPERATION_FAILED_ERROR_MESSAGE));
            e.printStackTrace();
            logger.error(String.format("Servlet: Error. Request path: %s\nException: %s\nstackTrace: %s", "Ошибка сервере", e.getMessage(), e.getStackTrace()));
        }
    }

    private Project parseProjectFromRequest(HttpServletRequest req) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            logger.info(String.format("Servlet: парсинг объекта проекта в объект класса Project. Request body: %s", objectMapper.writeValueAsString(req.getInputStream())));
            return objectMapper.readValue(req.getInputStream(), Project.class);
        } catch (IOException e) {
            logger.error(String.format("Servlet: Error. Парсинг не удался. Request path: %s\nException: %s", "/projects", e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    /**
     * HTTP DELETE запрос
     * метод удаляет проект по {@code id}.
     * Возвращает сообщение о успешном удалении
     *
     * <p>
     *     метод принимает параметр в адресной строке:
     *     <ul>
     *         <li>{@code Id}</li>
     *     </ul>
     * </p>
     *
     * @param req
     * @param resp
     * @return 200 OK
     * @return 400 Bad Request
     * @return 404 Not Found
     * @throws RuntimeException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String id = req.getParameter("id");

        try {

            if (id == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.ID_REQUIRED_AD_PARAMETER_ERROR_MESSAGE));
                logger.error(String.format("Servlet: Error. Id required in request path. Request path: %s", "/projects"));
                return;
            }
            UUID projectId = UUID.fromString(id);

            boolean isDeleted = projectController.delete(projectId);

            if (isDeleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(String.format("{\"message\":\"%s\"}", StaticConstants.REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE));
                logger.info(String.format("Servlet: Request path: %s. Response: %s", "/projects", "{\"message\":\"Request completed successfully\"}"));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE));
                logger.error(String.format("Servlet: Error. Project not found. Request path: %s", "/projects"));
            }

        } catch (java.io.IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE));
            logger.error(String.format("Servlet: Error. Invalid ID format. Request path: %s", "/projects"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String id = req.getParameter("id");

        if(utils.validateId(id)) {
            UUID projectId = UUID.fromString(id);

            try {
                Project project = parseProjectFromRequest(req);
                project.setId(projectId);

                logger.info("Servlet: Парсинг выполнен\n Выполнение метода апдейта проекта");
                ProjectDto updatedProject = projectController.updateProject(ProjectMapper.toDto(project));
                logger.info(String.format("Servlet: Проект обновлён. Updated project: %s", objectMapper.writeValueAsString(updatedProject)));
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                objectMapper.writeValue(resp.getWriter(), updatedProject);

            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(resp.getWriter(),
                        Map.of("error", "Invalid input", "message", e.getMessage()));

            } catch (ProjectNotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(),
                        Map.of("error", "Project not found", "message", e.getMessage()));

            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(resp.getWriter(),
                        Map.of("error", "Server error", "message", e.getMessage()));
            }
        }

        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE));
        logger.error(String.format("Servlet: Error. Invalid ID format. Request path: %s", "/projects"));
    }
}
