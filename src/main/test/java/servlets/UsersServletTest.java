package servlets;

//import org.junit.jupiter.api.Test;
import controllers.UsersController;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
//import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.*;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersServletTest {

/*
    @Ignore
    @Test
    public void doDelete_ShouldReturnSuccess_WhenUserExists() throws Exception {

        UUID testUserId = UUID.randomUUID();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        UsersController controller = new UsersController();// = mock(UsersController.class);
        controller = mock(UsersController.class);
        when(controller.delete(testUserId)).thenReturn(true);

        // Создаем экземпляр сервлета с mock контроллером
        UsersServlet servlet = new UsersServlet() {

            //@Override

            /*UsersController createController() throws SQLException {
                return controller;
            }*/
        //};
/*
        String pathInfo = request.getPathInfo();
        String test = pathInfo.split("/")[1];

        // Настраиваем request mock
        when(request.getPathInfo()).thenReturn("/" + testUserId.toString());

        // Настраиваем response mock для проверки вывода
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Act
        servlet.doDelete(request, response);

        // Assert
        verify(controller).delete(testUserId);
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_OK);

        writer.flush();
        assertTrue(stringWriter.toString().contains("User deleted successfully"));
    }*/
}
