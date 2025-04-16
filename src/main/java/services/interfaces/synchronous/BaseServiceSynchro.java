package services.interfaces.synchronous;

import java.util.Optional;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseServiceSynchro<T> {
    T create(T entity);
    T getById(UUID id);
    boolean deleteById(UUID id);
    T updateById(T entity);
}
