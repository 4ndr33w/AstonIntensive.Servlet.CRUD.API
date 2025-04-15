package controllers;

import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.Project;
import services.ProjectsService;
import services.interfaces.ProjectService;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsController {

    ProjectService projectService;

    public ProjectsController() throws SQLException {

        this.projectService = new ProjectsService();
    }

    public List<ProjectDto> getByUserId(UUID id) throws SQLException, ExecutionException, InterruptedException {
        if (id != null) {
            return projectService.getByUserIdAsync(id).get().stream().map(ProjectMapper::toDto).toList();
        } else {
            throw new IllegalArgumentException("Id is null");
        }
    }

    public List<ProjectDto> getByAdminId(UUID id) throws SQLException, ExecutionException, InterruptedException {
        if (id != null) {
            return projectService.getByAdminIdAsync(id).get().stream().map(ProjectMapper::toDto).toList();
        } else {
            throw new IllegalArgumentException("Id is null");
        }
    }

    public ProjectDto getByProjectId(UUID id) throws SQLException, ExecutionException, InterruptedException {
        if (id != null) {
            var project = projectService.getByIdAsync(id).get();
            return ProjectMapper.toDto(project);
        } else {
            throw new IllegalArgumentException("Id is null");
        }
    }

    /*
     public List<UserDto> getAll() throws SQLException, ExecutionException, InterruptedException {
        return userService.getAllAsync().get().stream().map(UserMapper::toDto).toList();
    }
     */
}
