package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/api/v1/projects")
public class ProjectsServlet extends HttpServlet {

    //private final Project
    private ObjectMapper objectMapper = new ObjectMapper();
}
