package servlets;

import controllers.UsersController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.abstractions.BaseServlet;
import utils.Utils;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet для получения списка всех пользователей
 *
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet(urlPatterns = "/api/v1/users/all", asyncSupported = true)
public class GetAllUsersServlet extends BaseServlet {

    private final controllers.interfaces.BaseUserController userController;

    public GetAllUsersServlet() {
        super();
        userController = new UsersController();
    }

    @Override
    public void init() {
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
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        AsyncContext asyncContext = req.startAsync();
        executor.execute(() -> {
            try {
                var usersFuture = userController.getAll();
                var users = usersFuture.get();
                String jsonResponse = new ObjectMapper().writeValueAsString(users);

                asyncSuccesfulResponse(
                        HttpServletResponse.SC_OK,
                        jsonResponse,
                        asyncContext);
            }
            catch (Exception e) {
                handleAsyncError(asyncContext, e,"/api/v1/users/all");
            }
            finally {
                asyncContext.complete();
            }
        });
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
