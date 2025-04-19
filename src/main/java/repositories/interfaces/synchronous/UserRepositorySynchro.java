package repositories.interfaces.synchronous;

import models.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface UserRepositorySynchro extends BaseRepositorySynchro<User> {

    Optional<List<User>> findAllByIds(List<UUID> userIds);
}
