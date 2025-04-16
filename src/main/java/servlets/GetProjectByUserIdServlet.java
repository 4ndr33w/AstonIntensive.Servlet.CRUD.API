package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ProjectControllerSynchronous;
import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.abstractions.BaseServlet;
import utils.StaticConstants;
import utils.Utils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

/**
 * Сервлет обработки GET-запроса
 * для получения всех проектов пользователя
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/projects/user")
public class GetProjectByUserIdServlet extends BaseServlet {

    private final ProjectControllerInterface projectController;

    Logger logger = LoggerFactory.getLogger(GetProjectByUserIdServlet.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Utils utils;

    public GetProjectByUserIdServlet() {
        super();
        this.utils = new Utils();
        this.projectController = new ProjectControllerSynchronous();
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

        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            String id = req.getParameter("id");
            if (id == null) {
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/projects/user",
                        StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                        resp);
                return;
            }
            boolean idValidation = utils.validateId(id);
            if(!idValidation) {
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/projects/user",
                        StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE,
                        resp);
                return;
            }

            logger.info(String.format("Все идет штатно"));
            List<ProjectDto> projects = projectController.getByUserId(UUID.fromString(id));

            if(projects == null || projects.size() == 0) {
                printResponse(
                        HttpServletResponse.SC_NOT_FOUND,
                        "/api/v1/projects/user",
                        StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE,
                        resp);
            }
            else{
                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(projects);

                PrintWriter out = resp.getWriter();
                out.print(jsonResponse);
                out.flush();
                logger.info("Ответ отправлен клиенту");
            }
        }
        catch (Exception e) {
            printResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "/api/v1/projects/user",
                    StaticConstants.REQUEST_VALIDATION_ERROR_MESSAGE,
                    e,
                    resp);
        }
    }
}
