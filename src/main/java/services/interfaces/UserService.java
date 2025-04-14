package services.interfaces;

import models.dtos.UserDto;
import models.entities.User;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface UserService extends BaseService<User> {

    //CompletableFuture<UserDto> createUserAsync(User user) throws Exception;
    CompletableFuture<User> getUserByEmailAsync(String email);
    CompletableFuture<User> getUserByUserNameAsync(String username) throws Exception;
    CompletableFuture<User> updateEmailAsync(String oldEmail, String newEmail) throws Exception;
    CompletableFuture<User> updatePasswordAsync(UUID userId, String password) throws Exception;
    CompletableFuture<List<User>> getAllAsync() throws SQLException;
}
