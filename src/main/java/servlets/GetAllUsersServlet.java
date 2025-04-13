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
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/users/all")
public class GetAllUsersServlet extends HttpServlet {

    private final UsersController controller;

    public GetAllUsersServlet() throws SQLException {
        super();
        controller = new UsersController();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {

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
        catch (SQLException | ExecutionException | InterruptedException e) {
            if(e instanceof SQLException) {
                resp.sendError(500);
            }
            if(e instanceof InterruptedException) {
                resp.sendError(400);
            }
            if(e instanceof ExecutionException) {
                resp.sendError(404);
            }
            else {
                resp.sendError(400);
            }
        }
    }
}
