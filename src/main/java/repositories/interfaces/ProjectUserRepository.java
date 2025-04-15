package repositories.interfaces;

import configurations.JdbcConnection;
import models.dtos.ProjectUsersDto;
import utils.StaticConstants;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static utils.mappers.ProjectUserMapper.mapResultSetToProjectUser;

/**
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
