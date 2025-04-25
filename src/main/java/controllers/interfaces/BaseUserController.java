package controllers.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseUserController<E, D> {

    CompletableFuture<List<D>> getAll() throws SQLException;
    CompletableFuture<D> getUser(UUID userId) throws SQLException;
    CompletableFuture<D> create(E user) throws SQLException;
    CompletableFuture<Boolean> delete(UUID userId) throws SQLException;
    CompletableFuture<D> updateUser(D userD) throws SQLException;
}
