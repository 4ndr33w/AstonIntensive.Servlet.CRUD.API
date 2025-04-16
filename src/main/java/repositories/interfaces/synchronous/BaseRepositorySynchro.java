package repositories.interfaces.synchronous;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Базовый интерфейс CRUD-операций для работы с репозиторием с синхронным доступом
 *
 * @param <T> тип сущности
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseRepositorySynchro<T> {
    Optional<T> findById(UUID id);
    Optional<List<T>> findAll();
    T create(T item);
    Optional<T> update(T item);
    boolean delete(UUID id);
}
