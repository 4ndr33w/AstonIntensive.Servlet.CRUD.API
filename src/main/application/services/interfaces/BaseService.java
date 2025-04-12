package services.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseService<T> {

    T getById(UUID id);
    boolean deleteById(UUID id);
    T updateById(UUID id, T entity);
    List<T> getAll() throws SQLException;
}
