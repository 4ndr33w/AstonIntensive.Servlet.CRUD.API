package controllers;

import models.dtos.UserDto;
import models.entities.User;
import services.UsersService;
import services.interfaces.UserService;
import utils.mappers.UserMapper;

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
        return userService.getAllAsync().get().stream().map(UserMapper::toDto).toList();
    }

    public UserDto getUser(UUID id) throws SQLException, ExecutionException, InterruptedException {
        if (id != null) {
            return UserMapper.toDto(userService.getByIdAsync(id).get());
        } else {
            throw new IllegalArgumentException("Id is null");
        }
    }

    public UserDto create(User user) throws Exception {
        if (user != null) {
            return UserMapper.toDto(userService.createAsync(user).get());
        }
        else {
            throw new Exception("User is null");
        }
    }

    public boolean delete(UUID id) throws SQLException, ExecutionException, InterruptedException {
        if (id != null) {
            return userService.deleteByIdAsync(id).get();
        } else {
            throw new IllegalArgumentException("Id is null");
        }
    }
}
