package services.interfaces;

import models.dtos.UserDto;
import models.entities.User;

import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface UserService {

    UserDto createUser(User user) throws Exception;
    UserDto getUserByEmail(String email);
    UserDto getUserByUserName(String username) throws Exception;
    UserDto updateEmail(String oldEmail, String newEmail) throws Exception;
    UserDto updatePassword(UUID userId, String password) throws Exception;
}
