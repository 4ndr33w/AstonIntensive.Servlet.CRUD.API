package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
//import configurations.LoggerConfiguration;
import controllers.UsersController;
import models.dtos.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.abstractions.BaseServlet;
import utils.StaticConstants;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet для получения списка всех пользователей
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/users/all")
public class GetAllUsersServlet extends BaseServlet {

    Logger logger = LoggerFactory.getLogger(GetAllUsersServlet.class);
    private final UsersController userController;

    public GetAllUsersServlet() {
        super();
        userController = new UsersController();
    }

    /**
     * HTTP GET запрос
     * метод возвращает список всех пользователей
     *
     * @param req
     * @param resp
     *     * @return 200 OK
     * @return 400 Bad Request
     *     * @throws RuntimeException
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            List<UserDto> users = userController.getAll();

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(users);

            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();

            logger.info("GetAllUsersServlet started");
        }
        catch (Exception e) {
            printResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "/api/v1/users/all",
                    StaticConstants.REQUEST_VALIDATION_ERROR_MESSAGE,
                    e,
                    resp);
        }
    }
}
