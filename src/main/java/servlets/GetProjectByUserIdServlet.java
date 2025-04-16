package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ProjectControllerSynchronous;
import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import utils.StaticConstants;
import utils.Utils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class GetProjectByUserIdServlet extends HttpServlet {

    //private final Project

    private final ProjectControllerInterface projectController;

    //private final ProjectsController controller;// = new ProjectsController();
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Utils utils;

    public GetProjectByUserIdServlet() {
        super();
        this.utils = new Utils();
        this.projectController = new ProjectControllerSynchronous();
        //this.projectController = new controllers.ProjectsController();
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            var path = req.getPathInfo();

            // Не получилось вычленить Id из req.getPathInfo(), разделяя строку на массив
            // если быть точнее, то /{id} воспринимался как несуществующий endpoint
            // поэтому пришлось использовать параметр запроса
            String id = req.getParameter("id");
            if (id == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.ID_REQUIRED_AD_PARAMETER_ERROR_MESSAGE));
                return;
            }
            boolean idValidation = utils.validateId(id);
            if(!idValidation) {

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE));
                return;

            }

            List<ProjectDto> projects = projectController.getByUserId(UUID.fromString(id));

            if(projects == null || projects.size() == 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE));
            }
            else{
                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(projects);

                PrintWriter out = resp.getWriter();
                out.print(jsonResponse);
                out.flush();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
