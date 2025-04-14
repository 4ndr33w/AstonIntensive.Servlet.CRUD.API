package services.interfaces;

import models.entities.Project;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectService extends BaseService<Project>{

    CompletableFuture<List<Project>> getByUserIdAsync() throws SQLException;
    CompletableFuture<List<Project>> getByAdminIdAsync() throws SQLException;

    CompletableFuture<Project> addUserToProjectAsync();
    CompletableFuture<Project> removeUserFromProjectAsync();
}
