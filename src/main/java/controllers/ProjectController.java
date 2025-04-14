package controllers;

import models.dtos.ProjectDto;
import services.ProjectsService;
import services.interfaces.ProjectService;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectController {

    ProjectService projectService;

    public ProjectController() throws SQLException {
        projectService = new ProjectsService();
    }


    public CompletableFuture<List<ProjectDto>> getByAdminId(UUID adminId) throws SQLException {
        projectService.getByAdminIdAsync(adminId)
                .thenAccept(projects -> {
                    // projects содержит полные объекты с пользователями
                })
                .exceptionally(ex -> {
                    // Обработка ошибок
                    return null;
                });
        return null;
    }
}
