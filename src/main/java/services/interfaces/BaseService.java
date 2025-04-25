package services.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseService<T> {

    CompletableFuture<T> createAsync(T entity) throws SQLException;
    CompletableFuture<T> getByIdAsync(UUID id) throws SQLException;
    CompletableFuture<Boolean> deleteByIdAsync(UUID id) throws SQLException;
    CompletableFuture<T> updateByIdAsync(T entity) throws SQLException;

}
