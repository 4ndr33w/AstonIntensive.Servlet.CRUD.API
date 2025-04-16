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
 * для получения всех проектов, в которых
 * пользователь с идентификатором {@code Id}
 * является администратором
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/projects/admin")
public class GetProjectsByAdminIdServlet extends BaseServlet {

    Logger logger = LoggerFactory.getLogger(GetProjectsByAdminIdServlet.class);
    private final ProjectControllerInterface projectController;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Utils utils;

    public GetProjectsByAdminIdServlet () {
        super();
        this.utils = new Utils();
        this.projectController = new ProjectControllerSynchronous();
    }

    /**
     * HTTP GET запрос
     * метод возвращает список всех проектов,
     * в которых пользователь с идентификатором {@code Id} является администратором,
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

            var path = req.getPathInfo();

            // Не получилось вычленить Id из req.getPathInfo(), разделяя строку на массив
            // если быть точнее, то /{id} воспринимался как несуществующий endpoint
            // поэтому пришлось использовать параметр запроса
            String id = req.getParameter("id");
            if (id == null) {
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/projects/admin",
                        StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                        resp);
                return;
            }
            boolean idValidation = utils.validateId(id);
            if(!idValidation) {
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/projects/admin",
                        StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE,
                        resp);
                return;
            }

            List<ProjectDto> projects = projectController.getByAdminId(UUID.fromString(id));

            if(projects == null || projects.size() == 0) {
                printResponse(
                        HttpServletResponse.SC_NOT_FOUND,
                        "/api/v1/projects/admin",
                        StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE,
                        resp);
            }
            else {
                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(projects);

                logger.info("Servlet: Sending response. Response code: 200 OK.");
                resp.setStatus(HttpServletResponse.SC_OK);
                PrintWriter out = resp.getWriter();
                out.print(jsonResponse);
                out.flush();
            }
        }
        catch (Exception e) {
            printResponse(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "/api/v1/projects/admin",
                    StaticConstants.INTERNAL_SERVER_ERROR_MESSAGE,
                    resp);
        }
    }
}
