package servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/users")
public class UsersServlet extends HttpServlet {

    private final UsersController controller;

    public UsersServlet() throws SQLException {
        super();
        controller = new UsersController();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /*try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            String path = req.getPathInfo();
            UUID id = UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6");
            UserDto user = controller.getUser(id);

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(user);

            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();
        } catch (ExecutionException | SQLException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }*/

        String action = req.getServletPath();

        switch (action) {
            case "/users/all":
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
                /*
                var jsonResponse = getAllUsers(resp);
                PrintWriter out = resp.getWriter();
                out.print(jsonResponse);
                out.flush();
                //getUser(UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"), resp, req.getPathInfo());*/
                break;
            default:
                String pathInfo = req.getPathInfo();
                if (pathInfo != null) {
                    String stringId = pathInfo.substring(7);
                    String[] parts = pathInfo.split("/");
                    if (parts.length > 1) {
                        try {
                            UUID id = UUID.fromString(stringId);
                            getUser(id, resp, stringId);
                        } catch (IllegalArgumentException e) {
                            // Обработка ошибки, если id не является корректным UUID
                        }
                    }
                }
                break;
        }


    }

    private String getAllUsers(HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            List<UserDto> users = controller.getAll();

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(users);

            return jsonResponse;

            /*PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();*/
        } catch (SQLException | ExecutionException | InterruptedException e) {
            if (e instanceof SQLException) {
                resp.sendError(500);
                return "500 Error: SQL Error";
            }
            if (e instanceof InterruptedException) {
                resp.sendError(400);
                return "400 Error: Bad Request";
            }
            if (e instanceof ExecutionException) {
                resp.sendError(404);
                return "404 Error: Not Found";
            } else {
                resp.sendError(400);
                return "400 Error: Bad Request";
            }
        }
    }

    private void getUser(UUID id, HttpServletResponse resp, String pathInfo) {
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            UserDto user = controller.getUser(id);

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(user);

            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();
        } catch (ExecutionException | SQLException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
