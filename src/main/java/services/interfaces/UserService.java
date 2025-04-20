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

    CompletableFuture<List<User>> getAllAsync();
}
