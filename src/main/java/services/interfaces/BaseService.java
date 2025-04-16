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

    CompletableFuture<T> createAsync(T entity);
    CompletableFuture<T> getByIdAsync(UUID id);
    CompletableFuture<Boolean> deleteByIdAsync(UUID id);
    CompletableFuture<T> updateByIdAsync(T entity);

}
