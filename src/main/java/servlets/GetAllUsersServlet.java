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
import servlets.sessionProcessing.SessionProcessingTask;
import utils.StaticConstants;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
*/


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
@WebServlet(urlPatterns = "/api/v1/users/all", asyncSupported = true)
public class GetAllUsersServlet extends BaseServlet {

    Logger logger = LoggerFactory.getLogger(GetAllUsersServlet.class);
    //private final controllers.interfaces.UserControllerInterface userController;
    private final controllers.interfaces.BaseUserController userController;

    public GetAllUsersServlet() {
        super();
        userController = new UsersController();
        //userController = new controllers.UserControllerSynchronous();
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
/*

                asyncContext.getResponse().setContentType("application/json");
                asyncContext.getResponse().setCharacterEncoding("UTF-8");
                ((HttpServletResponse) asyncContext.getResponse()).setStatus(HttpServletResponse.SC_OK);
                asyncContext.getResponse().getWriter().write(jsonResponse);*/
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
