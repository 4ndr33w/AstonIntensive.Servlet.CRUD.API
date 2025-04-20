package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
//import configurations.LoggerConfiguration;
import com.fasterxml.jackson.databind.SerializationFeature;
import controllers.UsersController;
import models.dtos.ErrorDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.abstractions.BaseServlet;
import utils.StaticConstants;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
*/


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
    private final controllers.interfaces.UserControllerInterface userController;

    public GetAllUsersServlet() {
        super();
        //userController = new UsersController();
        userController = new controllers.UserControllerSynchronous();
    }

    @Override
    public void init() throws ServletException {

    }


    //protected Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    /*protected ObjectMapper objectMapper = new ObjectMapper();
    protected Utils utils;

    protected void printResponse(int statusCode, String path, String message, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        ErrorDto error = new ErrorDto(
                statusCode,
                path,
                message);
        resp.setContentType("application/json");
        try {
            logger.error(error.toString());
            String jsonResponse = objectMapper.writeValueAsString(error);
            resp.setStatus(statusCode);
            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();
        }
        catch (IOException e) {
            logger.error("Ошибка сервера" + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            out.print("Ошибка сервера" + e.getMessage());
            out.flush();
        }
    }

    protected void printResponse(int statusCode, String path, String message, Exception ex, HttpServletResponse resp) throws IOException {

        ErrorDto error = new ErrorDto(
                statusCode,
                path,
                message);
        resp.setContentType("application/json");
        try {
            String jsonResponse = objectMapper.writeValueAsString(error);
            resp.setStatus(statusCode);
            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();
            logger.error(error.toString());
            logger.error("Ошибка сервера" + ex.getMessage());

        }
        catch (IOException e) {
            logger.error(error.toString());
            logger.error("Ошибка сервера" + e.getMessage());
            PrintWriter out = resp.getWriter();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("Ошибка сервера" + e.getMessage());
            out.flush();
        }
    }

    protected Project parseProjectFromRequest(HttpServletRequest req) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return objectMapper.readValue(req.getInputStream(), Project.class);
        } catch (Exception e) {
            logger.error(String.format("Servlet: Error. Парсинг не удался. Request path: %s\nException: %s", "/projects", e.getMessage()));
            throw new DataParsingException("Ошибка чтения данных объекта", e);
        }
    }

    protected User parseUserFromRequest(HttpServletRequest req) throws IOException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return objectMapper.readValue(req.getInputStream(), User.class);
        }
        catch (Exception e) {
            logger.error(String.format("Servlet: Error. Парсинг не удался. Request path: %s\nException: %s", "/projects", e.getMessage()));
            throw new DataParsingException("Ошибка чтения данных объекта", e);
        }

    }*/
















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
