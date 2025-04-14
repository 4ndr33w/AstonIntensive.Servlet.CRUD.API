package services;

import models.entities.User;
import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersServiceTest {
/*
    @Test
    public void getAllTest() throws SQLException {

        UsersService usersService = new UsersService();


        var users = usersService.getAll();

        assertNotNull(users);
    }*/

    @Test
    public void getAllAsyncTest() throws SQLException, ExecutionException, InterruptedException {
        UsersService usersService = new UsersService();

        var users = usersService.getAllAsync();
        var test = users.get();

        assertNotNull(test);
    }

    @Test
    public void test1() throws SQLException, ExecutionException, InterruptedException {

        var _user = new User();



        UsersService usersService = new UsersService();
        _user = usersService.getByIdAsync(UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6")).get();
        var test = usersService.enrichUserWithProjects(_user);

        var result = test.get();

        assertNotNull(result);
    }
}
