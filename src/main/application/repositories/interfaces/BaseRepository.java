package repositories.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic интерфейс для CRUD операций
 * используя JDBC
 * @param <T> любой класс наследник Object
 *
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseRepository<T> {

    Optional<T> findById(UUID id);
    Optional<List<T>> findByName(String name);
    Optional<List<T>> findAll() throws SQLException;

    Optional<T> create(T item) throws SQLException;
    Optional<T> update(T item);
    boolean delete(UUID id) throws SQLException;
}
