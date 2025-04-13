package controllers;

import models.dtos.UserDto;
import services.UsersService;
import services.interfaces.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersController {

    public UserService userService;

    public UsersController() throws SQLException {
        this.userService = new UsersService();
    }

    public List<UserDto> getAll() throws SQLException, ExecutionException, InterruptedException {
        return userService.getAllAsync().get();
    }

    public UserDto getUser(UUID id) throws SQLException, ExecutionException, InterruptedException {
        if (id != null) {
            return userService.getByIdAsync(id).get();
        } else {
            return null;
        }
    }
}
