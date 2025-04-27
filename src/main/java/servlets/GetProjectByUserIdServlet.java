package servlets;

//import controllers.ProjectControllerSynchronous;
//import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import servlets.abstractions.BaseServlet;
import utils.StaticConstants;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.exceptions.InvalidIdExceptionMessage;
import utils.exceptions.NoProjectsFoundException;
import utils.exceptions.ProjectNotFoundException;

/**
 * Сервлет обработки GET-запроса
 * для получения всех проектов пользователя
 * <p>
 * Отдельно для этого сервлета и сервлета {@code GetProjectByAdminIdServlet}
 * отдельно были созданы:
 * <ul>
 *     <li>{@code ProjectControllerSynchronous}</li>
 *     <li>{@code services.synchronous.ProjectsService}</li>
 *     <li>{@code repositories.interfaces.synchronous.}</li>
 *     <li>{@code repositories.synchronous.ProjectsRepository}</li>
 *     <li>{@code repositories.synchronous.ProjectUserRepositorySynchronous}</li>
 * </ul>
 * </p>
 *
 * Так как не успевал в установленные сроки создать рабочий функционал своей изначальной идеи
 * сделать все вызовы к БД асинхронно
 * <p>
 *     Так же, не имея до этого опыта работы с многопоточностью уже на этапе реализации сервлетов
 *     (когда слои контролллеров - сервисов - репозиториев были готовы)
 *     пришло понимание, что потоки следует начинать относительно сессии в сервлете.
 * </p>
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet(urlPatterns = "/api/v1/projects/user", asyncSupported = true)
public class GetProjectByUserIdServlet extends BaseServlet {

    //private final ProjectControllerInterface projectController;
    private final controllers.interfaces.BaseProjectController projectController;

    public GetProjectByUserIdServlet() {
        super();
        //this.projectController = new ProjectControllerSynchronous();
        projectController = new controllers.ProjectsController();
    }

    /**
     * HTTP GET запрос
     * метод возвращает список всех проектов,
     * в которых участвует пользователь с указанным {@code userId}
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
     * @throws RuntimeException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String id = req.getParameter("id");
        AsyncContext asyncContext = req.startAsync();
        executor.execute(() -> {
            try {

                if (id == null) {
                    asyncErrorResponse(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "/api/v1/projects/user",
                            StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                            asyncContext);
                    return;
                }
                boolean idValidation = utils.validateId(id);
                if(!idValidation) {
                    throw new InvalidIdExceptionMessage(StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE);
                }

                var result = projectController.getByUserId(UUID.fromString(id));

                if (result.isCompletedExceptionally()) {
                    asyncErrorResponse(
                            HttpServletResponse.SC_NOT_FOUND,
                            "/api/v1/projects/user",
                            StaticConstants.PROJECTS_NOT_FOUND_EXCEPTION_MESSAGE,
                            asyncContext);
                    return;

                }
                List<ProjectDto> projects = (List<ProjectDto>) result.join();

                if(projects == null || projects.isEmpty()) {
                    throw new NoProjectsFoundException(StaticConstants.PROJECTS_NOT_FOUND_EXCEPTION_MESSAGE);
                }
                else{

                    ObjectMapper mapper = new ObjectMapper();
                    String jsonResponse = mapper.writeValueAsString(projects);

                    asyncSuccesfulResponse(HttpServletResponse.SC_OK,
                            jsonResponse,
                            asyncContext);
                }
            }
            catch (Exception e) {
                handleAsyncError(asyncContext, e, "/api/v1/projects/user");
            }
            finally {
                if (asyncContext != null) {
                    asyncContext.complete();
                }
            }

        });

    }
}
