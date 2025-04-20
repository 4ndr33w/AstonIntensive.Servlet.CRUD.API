package controllers;

import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersControllerTest {

    controllers.interfaces.UserControllerInterface usersController;

    @Test
    public void getUserTest() throws SQLException, ExecutionException, InterruptedException {
        usersController = new controllers.UserControllerSynchronous();

        //UUID userId = UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa0");
        var result = usersController.getAll();

        var user = result;

        assertNotNull(user);
    }


}
