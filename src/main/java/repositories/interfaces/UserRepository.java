package repositories.interfaces;

import models.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфецс для поиска пользователя:
 * <p>по логину {@code findByUserName} </p>
 * <p>и email {@code findByEmail}</p>
 * @see models.entities.User
 * @author 4ndr33w
 * @version 1.0
 */
public interface UserRepository extends BaseRepository<User> {

    CompletableFuture<User> findByEmailAsync(String email);
    CompletableFuture<User> findByUserNameAsync(String userName);
    CompletableFuture<User> updateEmailAsync(String oldEmail, String newEmail);
    CompletableFuture<User> updatePasswordAsync(UUID userId, String newPassword);

}
