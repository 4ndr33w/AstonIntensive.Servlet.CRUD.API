package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import controllers.UsersController;
import models.dtos.UserDto;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.abstractions.BaseServlet;
import utils.StaticConstants;
import utils.Utils;
import utils.exceptions.UserNotFoundException;
import utils.mappers.UserMapper;

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
import java.util.UUID;

/**
 * Сервлет, обрабатывающий запросы по пути "/api/v1/users"
 * <p>
 *     стандартные {@code CRUD} операции по текущему эндпойнту;
 * </p>
 * <p>
 *
 * </p>
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/users")
public class UsersServlet extends BaseServlet {

    protected Logger logger = LoggerFactory.getLogger(UsersServlet .class);
    private final controllers.interfaces.UserControllerInterface userController;

    public UsersServlet() {
        super();
        //userController = new UsersController();
        userController = new controllers.UserControllerSynchronous();
        utils = new Utils();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String id = req.getParameter("id");
        if (id == null) {
            printResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "/api/v1/users",
                    StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                    resp);
            return;
        }
        boolean idValidation = utils.validateId(id);
        if(!idValidation) {
            printResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "/api/v1/users",
                    StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE,
                    resp);
            return;
        }
        UUID userId = UUID.fromString(id);
        try {
            UserDto userDto = userController.getUser(userId);


            String jsonResponse = objectMapper.writeValueAsString(userDto);

            resp.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();
        }
        catch (Exception e) {
            printResponse(
                    HttpServletResponse.SC_NOT_FOUND,
                    "/api/v1/users",
                    StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE,
                    e,
                    resp);
        }
    }

    /**
     * HTTP POST запрос
     * Создание нового пользователя
     * метод возвращает DTO-объект пользователя {@code UserDto}
     *
     * <pre>{@code
     * {
     *  "userName": "Andr33w"
     *  "password": "McFly"
     *  "email": "McFly@123.ru"
     *  "firstName": "Andrew"
     *  "lastName": "McFly"
     *  "phoneNumber": "+79211234567"
     *  "userRole": 0
     *  "userImage": null
     * }
     * }</pre>
     *
     * @param req
     * @param resp
     * @return 201 Created
     * @return 400 Bad Request
     * @return 500 Internal Server Error
     * @throws RuntimeException
     * @throws IllegalArgumentException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            User user = parseUserFromRequest(req);

            validateUser(user);

            UserDto createdUser = userController.create(user);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(resp.getWriter(), createdUser);

        } catch (IllegalArgumentException e) {
            printResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "/api/v1/users",
                    StaticConstants.REQUEST_VALIDATION_ERROR_MESSAGE,
                    e,
                    resp);
        } catch (Exception e) {
            printResponse(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "/api/v1/users",
                    StaticConstants.OPERATION_FAILED_ERROR_MESSAGE,
                    e,
                    resp);
        }
    }

    private void validateUser(User user) throws IllegalArgumentException {
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            logger.error("Параметр запроса USERNAME не валиден");
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            logger.error("Параметр запроса EMAIL не валиден");
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            logger.error("Параметр запроса PASSWORD не валиден");
            throw new IllegalArgumentException("Password is required");
        }
    }

    /**
     * HTTP DELETE запрос
     * метод удаляет пользователя по {@code id}.
     * Возвращает сообщение о успешном удалении
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
     * @return 404 Not Found
     * @throws RuntimeException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String id = req.getParameter("id");
        try {
            if (id == null) {
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/users",
                        StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                        resp);
                return;
            }
            UUID userId = UUID.fromString(id);

            boolean isDeleted = userController.delete(userId);

            if (isDeleted) {
                printResponse(
                        HttpServletResponse.SC_OK,
                        "/api/v1/users",
                        StaticConstants.REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE,
                        resp);
            } else {
                printResponse(
                        HttpServletResponse.SC_NOT_FOUND,
                        "/api/v1/users",
                        StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE,
                        resp);
             }

        } catch (IllegalArgumentException | IOException e) {
            printResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "/api/v1/users",
                    StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE,
                    e,
                    resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String id = req.getParameter("id");

        if(utils.validateId(id)) {
            UUID userId = UUID.fromString(id);

            try {
                User user = parseUserFromRequest(req);
                user.setId(userId);

                UserDto updatedUser = userController.updateUser(UserMapper.toDto(user));
                updatedUser.setUserRole(user.getUserRole());
                updatedUser.setUserName(user.getUserName());
                updatedUser.setEmail(user.getEmail());
                updatedUser.setCreatedAt(user.getCreatedAt());

                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                objectMapper.writeValue(resp.getWriter(), updatedUser);

            } catch (IllegalArgumentException e) {
                printResponse(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "/api/v1/users",
                        StaticConstants.REQUEST_VALIDATION_ERROR_MESSAGE,
                        e,
                        resp);

            } catch (UserNotFoundException e) {
                printResponse(
                        HttpServletResponse.SC_NOT_FOUND,
                        "/api/v1/users",
                        StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE,
                        e,
                        resp);
            } catch (Exception e) {
                printResponse(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "/api/v1/users",
                        StaticConstants.INTERNAL_SERVER_ERROR_MESSAGE,
                        e,
                        resp);
            }
        }
    }
}
