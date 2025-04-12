package services;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersServiceTest {

    @Test
    public void getAllTest() throws SQLException {

        UsersService usersService = new UsersService();


        var users = usersService.getAll();

        assertNotNull(users);
    }

    @Test
    public void getAllAsyncTest() throws SQLException {
        UsersService usersService = new UsersService();

        var users = usersService.getAllAsync();

        assertNotNull(users);
    }
}
