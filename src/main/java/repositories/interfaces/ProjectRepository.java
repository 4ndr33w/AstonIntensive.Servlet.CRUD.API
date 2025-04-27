package repositories.interfaces;

import models.dtos.ProjectDto;
import models.entities.Project;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Generic интерфейс для CRUD операций для работы
 * с репозиторием проектов
 *
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectRepository extends BaseRepository<Project> {

    CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException;
    CompletableFuture<ProjectDto> RemoveUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException;
    CompletableFuture<List<Project>> findByAdminIdAsync(UUID adminId);
    CompletableFuture<List<Project>> findByUserIdAsync(UUID userId);
    //CompletableFuture<List<Project>> findByUserIdsAsync(List<UUID> userIds) throws SQLException;
    CompletableFuture<List<Project>> findByAdminIdsAsync(List<UUID> adminIds) throws SQLException;
    CompletableFuture<List<Project>> findByProjectIdsAsync(List<UUID> projectIds) throws SQLException;
}
