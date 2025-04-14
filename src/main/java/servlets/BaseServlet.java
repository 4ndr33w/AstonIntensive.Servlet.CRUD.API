package servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@WebServlet("/")
public class BaseServlet extends javax.servlet.http.HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        getServletContext().getRequestDispatcher("/hello").forward(req, resp);
        resp.sendRedirect(req.getContextPath() + "/users");
    }
}
