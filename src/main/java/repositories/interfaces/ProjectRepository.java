package repositories.interfaces;

import models.entities.Project;
import services.interfaces.BaseService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectRepository extends BaseRepository<Project> {

    CompletableFuture<Project> AddUserToProjectAsync(UUID userId, UUID projectId);
    CompletableFuture<Project> RemoveUserFromProjectAsync(UUID userId, UUID projectId);
    CompletableFuture<List<Project>> findByAdminIdAsync(UUID adminId);

}
