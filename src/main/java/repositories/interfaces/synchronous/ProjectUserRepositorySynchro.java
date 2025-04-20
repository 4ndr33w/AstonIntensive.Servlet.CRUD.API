package repositories.interfaces.synchronous;

import models.dtos.ProjectUsersDto;
import repositories.interfaces.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectUserRepositorySynchro {

    Optional<List<ProjectUsersDto>> findByUserId(UUID userId);
    Optional<List<ProjectUsersDto>> findByProjectId(UUID projectId);
    boolean deleteUserFromProject(UUID userId, UUID projectId);
    boolean addUserToProject(UUID userId, UUID projectId);
    Optional<List<ProjectUsersDto>> findByProjectIds(List<UUID> projectIds);
    Optional<List<ProjectUsersDto>> findByUserIds(List<UUID> userIds);
}
