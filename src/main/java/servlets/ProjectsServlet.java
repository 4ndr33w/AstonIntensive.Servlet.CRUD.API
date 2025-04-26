package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import controllers.ProjectsController;
import controllers.interfaces.BaseProjectController;
import models.dtos.ProjectDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.abstractions.BaseServlet;
import utils.StaticConstants;
import utils.Utils;
import utils.exceptions.InvalidIdExceptionMessage;
import utils.exceptions.ProjectNotFoundException;
import utils.exceptions.RequiredParameterException;
import utils.mappers.ProjectMapper;


import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
*/
import java.io.IOException;
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

    //private final ProjectControllerInterface projectController;
    private final BaseProjectController projectController;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Utils utils;

    public ProjectsServlet() {
        super();
        this.projectController = new ProjectsController();
        //this.projectController = new ProjectControllerSynchronous();

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

        String id = req.getParameter("id");

        AsyncContext asyncContext = req.startAsync();
        executor.execute(() -> {
            try {
                if (id == null) {
                    throw new RequiredParameterException(StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE);
                }

                boolean idValidation = utils.validateId(id);
                if (!idValidation) {
                    throw new InvalidIdExceptionMessage(StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE);
                }
                UUID projectId = UUID.fromString(id);

                var result = projectController.getByProjectId(projectId);
                ProjectDto project = (ProjectDto) result.get();

                String jsonResponse = new ObjectMapper().writeValueAsString(project);

                asyncSuccesfulResponse(
                        HttpServletResponse.SC_OK,
                        jsonResponse,
                        asyncContext);
            } catch (Exception e) {
                handleAsyncError(asyncContext, e,"/api/v1/projects");
            }
            finally {
                if (asyncContext != null) {
                    asyncContext.complete();
                }
            }
        });
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

        AsyncContext asyncContext = req.startAsync();
        executor.execute(() -> {
            try {
                Project project = parseProjectFromRequest((HttpServletRequest) asyncContext.getRequest());

                var result = projectController.create(project);

                ProjectDto projectDto = (ProjectDto) result.get();
                String jsonResponse = new ObjectMapper().writeValueAsString(projectDto);

                asyncSuccesfulResponse(
                        HttpServletResponse.SC_ACCEPTED,
                        jsonResponse,
                        asyncContext);

            } catch (Exception e) {
                handleAsyncError(asyncContext, e,"/api/v1/projects");
            }
            finally {
                if (asyncContext != null) {
                    asyncContext.complete();
                }
            }
        });
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

        String id = req.getParameter("id");
        AsyncContext asyncContext = req.startAsync();
        executor.execute(() -> {
            try {
                if (id == null) {
                    throw new RequiredParameterException(StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE);
                }

                boolean idValidation = utils.validateId(id);
                if (!idValidation) {
                    throw new InvalidIdExceptionMessage(StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE);
                }
                UUID projectId = UUID.fromString(id);

                var result = projectController.getByProjectId(projectId);
                Boolean isDeleted = (Boolean) result.get();

                if (isDeleted) {
                    printResponse(
                            HttpServletResponse.SC_OK,
                            "/api/v1/projects",
                            StaticConstants.REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE,
                            resp);
                }

            } catch (Exception e) {
                handleAsyncError(asyncContext, e,"/api/v1/projects");
            }
            finally {
                if (asyncContext != null) {
                    asyncContext.complete();
                }
            }
        });
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String id = req.getParameter("id");

        AsyncContext asyncContext = req.startAsync();
        executor.execute(() -> {
            if (id == null) {
                throw new RequiredParameterException(StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE);
            }

            boolean idValidation = utils.validateId(id);
            if (!idValidation) {
                throw new InvalidIdExceptionMessage(StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE);
            }
            UUID projectId = UUID.fromString(id);

            try {
                Project project = parseProjectFromRequest(req);
                project.setId(projectId);

                var result = projectController.update(project);
                ProjectDto updatedProject = (ProjectDto) result.get();

                String jsonResponse = new ObjectMapper().writeValueAsString(updatedProject);

                asyncSuccesfulResponse(
                        HttpServletResponse.SC_ACCEPTED,
                        jsonResponse,
                        asyncContext);
            }
            catch (Exception e) {
                handleAsyncError(asyncContext, e,"/api/v1/projects");
            }
            finally {
                if (asyncContext != null) {
                    asyncContext.complete();
                }
            }
        });
    }
}
