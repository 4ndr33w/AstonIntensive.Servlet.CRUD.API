package servlets.abstractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import models.dtos.ErrorDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class BaseServlet extends HttpServlet {

    protected Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected Utils utils;

    protected void printResponse(int statusCode, String path, String message, HttpServletResponse resp) throws IOException {

        ErrorDto error = new ErrorDto(
                statusCode,
                "/api/v1/users",
                message);
        try {
            String jsonResponse = objectMapper.writeValueAsString(error);
            resp.setStatus(statusCode);
            PrintWriter out = resp.getWriter();
            out.print(jsonResponse);
            out.flush();
            logger.error(error.toString());
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

    protected void printResponse(int statusCode, String path, String message, Exception ex, HttpServletResponse resp) throws IOException {

        ErrorDto error = new ErrorDto(
                statusCode,
                "/api/v1/users",
                message);
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
            logger.info(String.format("Servlet: парсинг объекта проекта в объект класса Project. Request body: %s", objectMapper.writeValueAsString(req.getInputStream())));
            return objectMapper.readValue(req.getInputStream(), Project.class);
        } catch (IOException e) {
            logger.error(String.format("Servlet: Error. Парсинг не удался. Request path: %s\nException: %s", "/projects", e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    protected User parseUserFromRequest(HttpServletRequest req) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("Парсинг данных пользователя из запроса...");
        return objectMapper.readValue(req.getInputStream(), User.class);
    }
}
