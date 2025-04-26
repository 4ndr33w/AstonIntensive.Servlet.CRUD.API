package services.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseService<D, E> {

    CompletableFuture<D> createAsync(E entity) throws SQLException;
    CompletableFuture<D> getByIdAsync(UUID id) throws SQLException;
    CompletableFuture<Boolean> deleteByIdAsync(UUID id) throws SQLException;
    CompletableFuture<D> updateByIdAsync(D dto) throws SQLException;

}
