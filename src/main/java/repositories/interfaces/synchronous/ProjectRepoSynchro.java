package repositories.interfaces.synchronous;

import models.entities.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Базовый интерфейс CRUD-операций для работы с репозиторием проектов
 *
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectRepoSynchro extends BaseRepositorySynchro<Project> {

    Project addUserToProject(UUID userId, UUID projectId);
    Project RemoveUserFromProject(UUID userId, UUID projectId);
    Optional<List<Project>> findByAdminId(UUID adminId);
    Optional<List<Project>> findByUserId(UUID userId);

}
