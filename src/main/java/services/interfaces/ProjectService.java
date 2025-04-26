package services.interfaces;

import models.dtos.ProjectDto;
import models.entities.Project;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectService extends BaseService<ProjectDto, Project>{

    CompletableFuture<List<ProjectDto>> getByUserIdAsync(UUID userId) throws SQLException;
    CompletableFuture<List<ProjectDto>> getByAdminIdAsync(UUID adminId) throws SQLException;

    CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId) throws SQLException;
    CompletableFuture<ProjectDto> removeUserFromProjectAsync(UUID userId, UUID projectId) throws SQLException;
}
