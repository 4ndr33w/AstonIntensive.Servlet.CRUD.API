package repositories.interfaces;

import models.entities.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Generic интерфейс для CRUD операций для работы
 * с репозиторием пользователей

 * @see models.entities.User
 * @author 4ndr33w
 * @version 1.0
 */
public interface UserRepository extends BaseRepository<User> {

    CompletableFuture<List<User>> findAllByIdsAsync(List<UUID> userIds);
}
