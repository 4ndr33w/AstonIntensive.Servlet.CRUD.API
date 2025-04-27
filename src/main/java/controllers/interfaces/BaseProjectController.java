package controllers.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseProjectController<E, D>  {

    CompletableFuture<D> create(E project) throws SQLException;
    CompletableFuture<D> getByProjectId(UUID projectId) throws SQLException;
    CompletableFuture<Boolean> delete(UUID projectId) throws SQLException;
    CompletableFuture<D> update(D projectDto) throws SQLException;

    CompletableFuture<List<D>> getByUserId(UUID userId) throws SQLException;
    CompletableFuture<List<D>> getByAdminId(UUID adminId) throws SQLException;

    CompletableFuture<D> addUserToProject(UUID userId, UUID projectId) throws SQLException;
    CompletableFuture<D> removeUserFromProject(UUID userId, UUID projectId) throws SQLException;

}
