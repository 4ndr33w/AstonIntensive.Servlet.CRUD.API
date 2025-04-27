package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.dtos.ProjectDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.abstractions.BaseServlet;
import utils.StaticConstants;
import utils.exceptions.InvalidIdExceptionMessage;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Сервлет с эндпойнтами для добавления / удаления
 * пользователей в / из проекта
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet(urlPatterns = "/api/v1/projects/users", asyncSupported = true)
public class AddRemoveUsersToProjectServlet extends BaseServlet {

    private final controllers.interfaces.BaseProjectController projectController;

    public AddRemoveUsersToProjectServlet() {
        super();
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

        actionHandler(req, models.enums.ActionType.POST);
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

        actionHandler(req, models.enums.ActionType.DELETE);
    }

    private void actionHandler(HttpServletRequest req, models.enums.ActionType actionType) {
        AsyncContext asyncContext = req.startAsync();

        executor.execute(() -> {
            String projectIdString = asyncContext.getRequest().getParameter("projectid");
            String userIdString = asyncContext.getRequest().getParameter("userid");
            try {
                if (projectIdString == null || userIdString == null ) {
                    asyncErrorResponse(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "/api/v1/projects/users",
                            StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                            asyncContext);
                }
                else {
                    boolean projectIdValidation = utils.validateId(projectIdString);
                    boolean userIdValidation = utils.validateId(userIdString);

                    if (!projectIdValidation || !userIdValidation) {
                        throw new InvalidIdExceptionMessage(StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE);
                    }
                    UUID projectId = UUID.fromString(projectIdString);
                    UUID userId = UUID.fromString(userIdString);

                    CompletableFuture<?> result = null;

                    switch (actionType) {
                        case POST -> {
                            result = projectController.addUserToProject(userId, projectId );
                            break;
                        }
                        case DELETE -> {
                            result = projectController.removeUserFromProject(userId, projectId );
                            break;
                        }
                    }

                    if(result.isCompletedExceptionally()) {
                        handleAsyncError(asyncContext, (Exception) result.get(),"/api/v1/projects/users");
                    }
                    else {
                        var updatedProject = (ProjectDto) result.get();
                        String jsonResponse = new ObjectMapper().writeValueAsString(updatedProject);

                        asyncSuccesfulResponse(
                                HttpServletResponse.SC_OK,
                                jsonResponse,
                                asyncContext);
                    }
                }
            }
            catch (Exception e) {
                handleAsyncError(asyncContext, e,"/api/v1/projects/users");
            }
            finally {
                if (asyncContext != null) {
                    asyncContext.complete();
                }
            }
        });
    }
}
