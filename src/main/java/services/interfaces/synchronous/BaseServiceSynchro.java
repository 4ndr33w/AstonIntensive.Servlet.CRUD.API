package services.interfaces.synchronous;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseServiceSynchro<T> {
    T create(T entity) throws SQLException;
    T getById(UUID id);
    boolean deleteById(UUID id) throws SQLException;
    T updateById(T entity) throws SQLException;
}
