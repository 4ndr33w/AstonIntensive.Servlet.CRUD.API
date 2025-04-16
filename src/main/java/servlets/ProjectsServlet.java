package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import controllers.ProjectsController;
import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.abstractions.BaseServlet;
import utils.StaticConstants;
import utils.Utils;
import utils.exceptions.ProjectNotFoundException;
import utils.mappers.ProjectMapper;

import javax.servlet.annotation.WebServlet;
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
public class ProjectsServlet extends BaseServlet {

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
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        var path = req.getPathInfo();

        // Не получилось вычленить Id из req.getPathInfo(), разделяя строку на массив
        // если быть точнее, то /{id} воспринимался как несуществующий endpoint
        // поэтому пришлось использовать параметр запроса
        String id = req.getParameter("id");

        try {
            if (id == null) {
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/projects",
                        StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                        resp);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            boolean idValidation = utils.validateId(id);
            if (!idValidation) {
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/projects",
                        StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE,
                        resp);
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
                printResponse(
                        HttpServletResponse.SC_NOT_FOUND,
                        "/api/v1/projects",
                        StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE,
                        resp);
            }
        } catch (Exception e) {
            printResponse(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "/api/v1/projects",
                    StaticConstants.INTERNAL_SERVER_ERROR_MESSAGE,
                    e,
                    resp);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

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
            printResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "/api/v1/projects",
                    StaticConstants.ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE,
                    e,
                    resp);
        } catch (Exception e) {
            printResponse(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "/api/v1/projects",
                    "Servlet Error",
                    e,
                    resp);
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
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/projects",
                        StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                        resp);
                return;
            }
            UUID projectId = UUID.fromString(id);

            boolean isDeleted = projectController.delete(projectId);

            if (isDeleted) {
                printResponse(
                        HttpServletResponse.SC_OK,
                        "/api/v1/projects",
                        StaticConstants.REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE,
                        resp);
            } else {
                printResponse(
                        HttpServletResponse.SC_NOT_FOUND,
                        "/api/v1/projects",
                        StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE,
                        resp);
            }

        } catch (java.io.IOException e) {
            printResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "/api/v1/projects",
                    StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE,
                    e,
                    resp);
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
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/projects",
                        StaticConstants.REQUEST_VALIDATION_ERROR_MESSAGE,
                        e,
                        resp);

            } catch (ProjectNotFoundException e) {
                printResponse(
                        HttpServletResponse.SC_NOT_FOUND,
                        "/api/v1/projects",
                        StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE,
                        e,
                        resp);
            } catch (Exception e) {
                printResponse(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "/api/v1/projects",
                        StaticConstants.INTERNAL_SERVER_ERROR_MESSAGE,
                        e,
                        resp);
            }
        }
    }
}
