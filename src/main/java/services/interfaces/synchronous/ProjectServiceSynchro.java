package services.interfaces.synchronous;

import models.entities.Project;

import java.util.List;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectServiceSynchro extends BaseServiceSynchro<Project> {

    List<Project> getByUserId(UUID userId);
    List<Project> getByAdminId(UUID adminId);

    Project addUserToProject(UUID userId, UUID projectId);
    Project removeUserFromProject(UUID userId, UUID projectId);
}
