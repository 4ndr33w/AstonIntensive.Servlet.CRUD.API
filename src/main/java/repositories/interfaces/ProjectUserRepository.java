package repositories.interfaces;

import models.dtos.ProjectUsersDto;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Вспомогательный интерфейс для организации
 * связи Many To Many между таблицами Project и User
 *
 * @see models.dtos.ProjectUsersDto
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectUserRepository {

    CompletableFuture<List<ProjectUsersDto>> findByUserId(UUID userId);
    CompletableFuture<List<ProjectUsersDto>> findByProjectId(UUID projectId);
    CompletableFuture<Boolean> deleteUserFromProject(UUID userId, UUID projectId);
    CompletableFuture<Boolean> addUserToProject(UUID userId, UUID projectId);
    CompletableFuture<List<ProjectUsersDto>> findByProjectIds(List<UUID> projectIds);
}
