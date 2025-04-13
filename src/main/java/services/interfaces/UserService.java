package services.interfaces;

import models.dtos.UserDto;
import models.entities.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface UserService extends BaseService<UserDto> {

    CompletableFuture<UserDto> createUserAsync(User user) throws Exception;
    CompletableFuture<UserDto> getUserByEmailAsync(String email);
    CompletableFuture<UserDto> getUserByUserNameAsync(String username) throws Exception;
    CompletableFuture<UserDto> updateEmailAsync(String oldEmail, String newEmail) throws Exception;
    CompletableFuture<UserDto> updatePasswordAsync(UUID userId, String password) throws Exception;
}
