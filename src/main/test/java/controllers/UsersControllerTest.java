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

    @Test
    public void getUserTest() throws SQLException, ExecutionException, InterruptedException {
        UsersController usersController = new UsersController();

        UUID userId = UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa0");
        var result = usersController.getUser(userId);

        var user = result;

        assertNotNull(user);
    }


}
