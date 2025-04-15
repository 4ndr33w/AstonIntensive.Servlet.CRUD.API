package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.UsersController;
import models.dtos.UserDto;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Servlet для получения списка всех пользователей
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/users/all")
public class GetAllUsersServlet extends HttpServlet {

    private final UsersController controller;

    public GetAllUsersServlet() {
        super();
        controller = new UsersController();
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            List<UserDto> users = controller.getAll();

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(users);

            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
