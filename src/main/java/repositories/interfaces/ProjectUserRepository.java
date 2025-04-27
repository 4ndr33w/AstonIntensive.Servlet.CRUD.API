package repositories.interfaces;

import models.dtos.ProjectUsersDto;

import java.sql.SQLException;
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

    CompletableFuture<List<ProjectUsersDto>> findByUserIdAsync(UUID userId);
    CompletableFuture<List<ProjectUsersDto>> findByProjectIdAsync(UUID projectId);
    CompletableFuture<Boolean> deleteUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException;
    CompletableFuture<Boolean> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException;
    CompletableFuture<List<ProjectUsersDto>> findByProjectIdsAsync(List<UUID> projectIds) throws SQLException;
    CompletableFuture<List<ProjectUsersDto>> findByUserIdsAsync(List<UUID> userIds) throws SQLException;

}
