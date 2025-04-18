package repositories.interfaces;

import models.dtos.ProjectDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.ProjectRepositoryNew;
import utils.StaticConstants;

import java.sql.SQLException;
import java.sql.Statement;
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

    CompletableFuture<ProjectDto> addUserToProjectAsync(UUID userId, UUID projectId);
    CompletableFuture<ProjectDto> RemoveUserFromProjectAsync(UUID userId, UUID projectId);
    CompletableFuture<List<Project>> findByAdminIdAsync(UUID adminId);
    CompletableFuture<List<Project>> findByUserIdAsync(UUID adminId);
}
