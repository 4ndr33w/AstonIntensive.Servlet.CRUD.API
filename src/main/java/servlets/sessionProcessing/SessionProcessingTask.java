package servlets.sessionProcessing;

import controllers.interfaces.UserControllerInterface;
import models.dtos.UserDto;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class SessionProcessingTask implements Callable<List<UserDto>> {

    private final HttpSession session;
    private final UserControllerInterface userController;

    public SessionProcessingTask(HttpSession session, UserControllerInterface userController) {
        this.userController = userController;
        this.session = session;
    }

    @Override
    public List<UserDto> call() throws Exception {
        return userController.getAll();
    }
}
