package controllers;

import models.dtos.ProjectDto;
import models.entities.Project;
import services.ProjectServiceImplNew;
import services.interfaces.ProjectService;
import utils.StaticConstants;
import utils.mappers.ProjectMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Класс контроллера, предоставляющего методы для работы с проектами
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsController {

    private final ProjectService projectService;

    public ProjectsController() throws SQLException {

        this.projectService = new ProjectServiceImplNew();
        //this.projectService = new ProjectsService();
    }

    /**
     * Получить список проектов по Id пользователя
     * <p>
     * Метод извлекает из CompletableFuture список проектов,
     * маппит их в список объектов типа ProjectDto
     * или возвращает список из 0 элементов.
     * </p>
     * @param userId
     * @return {@code List<ProjectDto>}
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    public List<ProjectDto> getByUserId(UUID userId) throws SQLException, ExecutionException, InterruptedException {
        if (userId != null) {
            return projectService.getByUserIdAsync(userId).get().stream().map(ProjectMapper::toDto).toList();
        }
        else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получить список проектов по {@code adminId} - id создателя проекта
     * <p>
     * Метод извлекает из CompletableFuture список проектов,
     * маппит их в список объектов типа ProjectDto
     * или возвращает список из 0 элементов.
     * </p>
     * @param adminId
     * @return {@code List<ProjectDto>} или пустой список
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    public List<ProjectDto> getByAdminId(UUID adminId) throws SQLException, ExecutionException, InterruptedException {
        if (adminId != null) {
            return projectService.getByAdminIdAsync(adminId).get().stream().map(ProjectMapper::toDto).toList();
        }
        else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получить DTO проекта по {@code projectId}
     * <p>
     * Метод извлекает из CompletableFuture проект,
     * маппит его в объект типа ProjectDto
     * или null, если проект не найден.
     * </p>
     * @param projectId
     * @return {@code ProjectDto} или {@code null}
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    public ProjectDto getProject(UUID projectId) throws SQLException, ExecutionException, InterruptedException {
        if (projectId != null) {
            var result = projectService.getByIdAsync(projectId).get();
            if (result!= null) {
                return ProjectMapper.toDto(result);
            }
            return null;
        }
        else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Создать новый прпоект
     * <p>
     * Метод передаёт в сервис объект типа {@code Project},
     * обратно извлекая из {@link CompletableFuture} объект типа {@code ProjectDto}
     * </p>
     * @param project
     * @return {@code ProjectDto}
     * @throws Exception
     * @throws IllegalArgumentException
     */
    public ProjectDto create(Project project) throws Exception {
        if (project != null) {
            return ProjectMapper.toDto(projectService.createAsync(project).get());
        } else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Удалить проект
     * <p>
     * Метод передаёт в сервис Id проекта {@code projectId},
     * обратно извлекая из {@link CompletableFuture} примитив {@code boolean}
     * как маркер выполнения операции удаления.
     * </p>
     * @param projectId
     * @return {@code boolean}
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    public boolean delete(UUID projectId) throws SQLException, ExecutionException, InterruptedException {
        if (projectId != null) {
            return projectService.deleteByIdAsync(projectId).get();
        } else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
    }

    /*public ProjectDto addUserToProject(UUID userId, UUID projectId) throws SQLException, ExecutionException, InterruptedException {
        if (userId != null && projectId != null) {
            return ProjectMapper.toDto(projectService.addUserToProjectAsync(userId, projectId).get());
        } else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
    }*/
    public ProjectDto addUserToProject(UUID userId, UUID projectId) {
        try {
            CompletableFuture<Project> future = projectService.addUserToProjectAsync(userId, projectId);
            Project project = future.get();
            return ProjectMapper.toDto(project);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation was interrupted", e);
        } catch (ExecutionException e) {
            throw convertExecutionException(e.getCause());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProjectDto removeUserFromProject(UUID userId, UUID projectId) {
        try {
            CompletableFuture<Project> future = projectService.removeUserFromProjectAsync(userId, projectId);
            Project project = future.get();
            return ProjectMapper.toDto(project);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation was interrupted", e);
        } catch (ExecutionException e) {
            throw convertExecutionException(e.getCause());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private RuntimeException convertExecutionException(Throwable cause) {
        if (cause instanceof IllegalArgumentException) {
            return (IllegalArgumentException) cause;
        } else if (cause instanceof NoSuchElementException) {
            return (NoSuchElementException) cause;
        } else if (cause instanceof IllegalStateException) {
            return (IllegalStateException) cause;
        } else if (cause instanceof SQLException) {
            String message = String.format("%s: %s, %s", StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, cause.getMessage(), cause);
            return new RuntimeException(message);
        } else {
            String message = String.format("%s: %s, %s", StaticConstants.UNEXPECTED_ERROR_EXCEPTION_MESSAGE, cause.getMessage(), cause);
            return new RuntimeException(message);
        }
    }

    /*
     public List<UserDto> getAll() throws SQLException, ExecutionException, InterruptedException {
        return userService.getAllAsync().get().stream().map(UserMapper::toDto).toList();
    }
     */
}
