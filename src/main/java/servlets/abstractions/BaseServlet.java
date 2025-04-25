package servlets.abstractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import models.dtos.ErrorDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StaticConstants;
import utils.Utils;
import utils.exceptions.DataParsingException;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.UserAlreadyExistException;
import utils.exceptions.UserNotFoundException;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;*/

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet
public abstract class BaseServlet extends HttpServlet {

    protected Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected Utils utils;

    protected HttpSession session;

    protected static ExecutorService executor;

    public BaseServlet() {

        super();
        executor = configurations.ThreadPoolConfiguration.getDbExecutor();
    }

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
    protected void asyncSuccesfulResponse(int statusCode, String response, AsyncContext asyncContext) throws IOException {

        asyncContext.getResponse().setContentType("application/json");
        asyncContext.getResponse().setCharacterEncoding("UTF-8");
        ((HttpServletResponse) asyncContext.getResponse()).setStatus(HttpServletResponse.SC_OK);
        asyncContext.getResponse().getWriter().write(response);
    }

    protected void asyncErrorResponse(int statusCode, String path, String message, AsyncContext asyncContext) throws IOException {
        //try {
            ErrorDto error = new ErrorDto(
                    statusCode,
                    path,
                    message);

            String jsonResponse = new ObjectMapper().writeValueAsString(error);

            asyncContext.getResponse().setContentType("application/json");
            asyncContext.getResponse().setCharacterEncoding("UTF-8");
            ((HttpServletResponse) asyncContext.getResponse()).setStatus(statusCode);
            asyncContext.getResponse().getWriter().write(jsonResponse);
            logger.info(error.toString());
        /*}
        catch (Exception e) {
            handleAsyncError(asyncContext, e,"/api/v1/users/all");
        }
        finally {
            asyncContext.complete();
        }*/
    }

    protected void asyncErrorResponse(int statusCode, String path, String message, AsyncContext asyncContext, Exception e) {
        try {
            ErrorDto error = new ErrorDto(
                    statusCode,
                    path,
                    message);

            String jsonResponse = new ObjectMapper().writeValueAsString(error);

            asyncContext.getResponse().setContentType("application/json");
            asyncContext.getResponse().setCharacterEncoding("UTF-8");
            ((HttpServletResponse) asyncContext.getResponse()).setStatus(statusCode);
            asyncContext.getResponse().getWriter().write(jsonResponse);
            logger.error(error.toString());
            logger.error("Ошибка сервера: " + e.getCause().getMessage());
        }
        catch (Exception ex) {
            logger.error("Ошибка сервера: " + ex.getCause().getMessage());
            //handleAsyncError(asyncContext, e,"/api/v1/users/all");
        }
        finally {
            asyncContext.complete();
        }
    }

    protected void printResponse(int statusCode, String path, String message, Exception ex, HttpServletResponse resp)  {

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
            PrintWriter out = null;
            try {
                out = resp.getWriter();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
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
    }

    protected void handleAsyncError(AsyncContext asyncContext, Exception e, String path) {
// throws SQLException, DatabaseOperationException, NullPointerException, CompletionException
        try {

            var cause = e.getCause();

            /*while (cause != null) {
                e = (Exception)cause;
                cause = e.getCause();
            }*/
            int counter = 0;
            while(e.getCause() != null) {
                e = (Exception)e.getCause();
                counter++;
                if (counter > 10) break;
            }
           /* if(e.getCause() != null)
                e = (Exception)e.getCause();*/
            String message = "error";
            int statusCode = 0;
            if (e instanceof SQLException) {

                if (e.getMessage().contains(("duplicate key")) || e.getCause().getMessage().contains(("duplicate key"))) {
                    message = StaticConstants.USER_ALREADY_EXISTS_EXCEPTION_MESSAGE;
                    statusCode = HttpServletResponse.SC_BAD_REQUEST;
                }
                else {
                    message = cause.getCause().getMessage();
                    statusCode = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                }
            }
            if (e instanceof UserAlreadyExistException) {
                message = StaticConstants.USER_ALREADY_EXISTS_EXCEPTION_MESSAGE;
                statusCode = HttpServletResponse.SC_BAD_REQUEST;
            }
            if (e instanceof InterruptedException) {
                message = e.getMessage();
                statusCode = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            }
            if (e instanceof NullPointerException) {
                message = StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE;
                statusCode = HttpServletResponse.SC_BAD_REQUEST;
            }
            if (e instanceof DatabaseOperationException) {

                if (e.getCause() instanceof SQLException) {
                    this.handleAsyncError(asyncContext, (Exception) cause, path);
                }
                message = StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE;
                statusCode = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            }
            if(e instanceof UserNotFoundException) {
                message = StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;
                statusCode = HttpServletResponse.SC_NOT_FOUND;
            }
            if(e instanceof CompletionException) {
                if(cause.getCause() != null) {
                    cause = cause.getCause();
                    this.handleAsyncError(asyncContext, (Exception) cause, path);
                }
                message = StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;
                statusCode = HttpServletResponse.SC_NOT_FOUND;
            }

            asyncErrorResponse(statusCode, path, message, asyncContext, e);

        } finally {
            if (asyncContext != null) {
                asyncContext.complete();
            }
        }
    }
}
