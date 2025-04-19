package controllers.interfaces;

import models.dtos.UserDto;
import models.entities.User;

import java.util.List;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface UserControllerInterface {

    List<UserDto> getAll();
    UserDto getUser(UUID userId);
    UserDto create(User user);
    boolean delete(UUID userId);
    UserDto updateUser(UserDto userDto);
}
