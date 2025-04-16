package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.UsersController;
import models.dtos.UserDto;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StaticConstants;
import utils.Utils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/users")
public class UsersServlet extends HttpServlet {

    Logger logger = LoggerFactory.getLogger(ProjectsServlet.class);

    private final UsersController userController;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Utils utils;

    public UsersServlet() {
        super();
        userController = new UsersController();
        utils = new Utils();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Не получилось вычленить Id из req.getPathInfo(), разделяя строку на массив
        // если быть точнее, то /{id} воспринимался как несуществующий endpoint
        // поэтому пришлось использовать параметр запроса
        String id = req.getParameter("id");
        if (id == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.ID_REQUIRED_AD_PARAMETER_ERROR_MESSAGE));
            logger.error("Параметр запроса ID не найден");
            return;
        }

        boolean idValidation = utils.validateId(id);
        if(!idValidation) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE));
            logger.error("Параметр запроса ID не прошел валидацию");
            return;

        }
        UUID userId = UUID.fromString(id);

        UserDto userDto = userController.getUser(userId);

        if (userDto != null) {

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(userDto);

            resp.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();
            logger.info("Пользователь найден и отправлен в ответ");
        }
        else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE));
            logger.error("Пользователь не найден");
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
            logger.info("Пользователь успешно создан");

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            logger.error("Параметры запроса не прошли валидацию");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.OPERATION_FAILED_ERROR_MESSAGE));
            e.printStackTrace();
            logger.error("Ошибка сервера");
        }
    }

    private User parseUserFromRequest(HttpServletRequest req) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("Парсинг данных пользователя из запроса...");
        return objectMapper.readValue(req.getInputStream(), User.class);
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

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String id = req.getParameter("id");

        try {

            if (id == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.ID_REQUIRED_AD_PARAMETER_ERROR_MESSAGE));
                logger.error("Параметр запроса ID не найден");
                return;
            }
            UUID userId = UUID.fromString(id);

            boolean isDeleted = userController.delete(userId);

            if (isDeleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(String.format("{\"message\":\"%s\"}", StaticConstants.REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE));
                logger.info("Пользователь успешно удален");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE));
                logger.error("Пользователь не найден");
            }

        } catch (IllegalArgumentException | IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(String.format("{\"error\":\"%s\"}", StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE));
            logger.error("Параметр запроса ID не прошел валидацию");
        }
    }
}
