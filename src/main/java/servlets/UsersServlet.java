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
import utils.exceptions.InvalidIdExceptionMessage;
import utils.exceptions.UserNotFoundException;
import utils.mappers.UserMapper;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
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
@WebServlet(urlPatterns = "/api/v1/users", asyncSupported = true)
public class UsersServlet extends BaseServlet {

    private final controllers.interfaces.BaseUserController userController;

    public UsersServlet() {
        super();
        userController = new UsersController();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        String id = req.getParameter("id");
        AsyncContext asyncContext = req.startAsync();
        executor.execute(() -> {

            try {
                if (id == null) {
                    asyncErrorResponse(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "/api/v1/users",
                            StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                            asyncContext);
                }
                boolean idValidation = utils.validateId(id);

                if (!idValidation) {
                    throw new InvalidIdExceptionMessage(StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE);
                }
                UUID userId = UUID.fromString(id);
                var userDto = (UserDto) userController.getUser(userId).get();

                String jsonResponse = new ObjectMapper().writeValueAsString(userDto);

                asyncSuccesfulResponse(
                        HttpServletResponse.SC_OK,
                        jsonResponse,
                        asyncContext);
            }
            catch (Exception e) {
                handleAsyncError(asyncContext, e,"/api/v1/users");
            }
            finally {
                if (asyncContext != null) {
                    asyncContext.complete();
                }
            }
        });
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

        AsyncContext asyncContext = req.startAsync();

        executor.execute(() -> {
            try {
                User user = parseUserFromRequest((HttpServletRequest) asyncContext.getRequest());

                var result = userController.create(user);

                if(result.isCompletedExceptionally()) {
                    handleAsyncError(asyncContext, (Exception) result.get(),"/api/v1/users");
                }
                else {
                    var createdUser = result.get();
                    String jsonResponse = new ObjectMapper().writeValueAsString(createdUser);

                    asyncSuccesfulResponse(
                            HttpServletResponse.SC_OK,
                            jsonResponse,
                            asyncContext);
                }

            }  catch (Exception e) {
                handleAsyncError(asyncContext, e,"/api/v1/users");
            }
            finally {
                asyncContext.complete();
            }
        });
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
     * @return 500 Internal Server Error
     * @throws RuntimeException
     * @throws IOException
     * @throws UserNotFoundException
     * @throws utils.exceptions.DatabaseOperationException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {

        String id = req.getParameter("id");
        AsyncContext asyncContext = req.startAsync();

        executor.execute(() -> {
            try {
                if (id == null) {
                    asyncErrorResponse(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "/api/v1/users",
                            StaticConstants.ID_REQUIRED_AS_PARAMETER_ERROR_MESSAGE,
                            asyncContext);
                }

                else {
                    boolean idValidation = utils.validateId(id);
                    if (!idValidation) {
                        throw new InvalidIdExceptionMessage(StaticConstants.INVALID_ID_FORMAT_EXCEPTION_MESSAGE);
                    }
                    UUID userId = UUID.fromString(id);

                    var isDeleted = (Boolean) userController.delete(userId).get();

                    if (isDeleted) {
                        asyncSuccesfulResponse(
                                HttpServletResponse.SC_OK,
                                StaticConstants.REQUEST_COMPLETER_SUCCESSFULLY_MESSAGE,
                                asyncContext);
                    }
                }
            }
            catch (Exception e) {
                handleAsyncError(
                        asyncContext,
                        e,
                        "/api/v1/users");
            }
            finally {
                if(asyncContext != null) {
                    asyncContext.complete();
                }
            }
        });
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {

        String id = req.getParameter("id");

        AsyncContext asyncContext = req.startAsync();

        executor.execute(() -> {
            if(utils.validateId(id)) {
                UUID userId = UUID.fromString(id);

                try {
                    User user = parseUserFromRequest( (HttpServletRequest) asyncContext.getRequest() );
                    user.setId(userId);
                    UserDto updatedUser = (UserDto) userController.updateUser(UserMapper.toDto(user)).get();
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                    String jsonResponse = objectMapper.writeValueAsString(updatedUser);

                    asyncSuccesfulResponse(
                            HttpServletResponse.SC_ACCEPTED,
                            jsonResponse,
                            asyncContext);
                }
                catch (Exception e) {
                    handleAsyncError(
                            asyncContext,
                            e,
                            "/api/v1/users");
                }
                finally {
                    if(asyncContext != null) {
                        asyncContext.complete();
                    }
                }
            }
        });
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
