package services.interfaces;

import models.entities.Project;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectService extends BaseService<Project>{

    CompletableFuture<List<Project>> getByUserIdAsync(UUID userId);
    CompletableFuture<List<Project>> getByAdminIdAsync(UUID adminId);

    CompletableFuture<Project> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException;
    CompletableFuture<Project> removeUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException;
}
