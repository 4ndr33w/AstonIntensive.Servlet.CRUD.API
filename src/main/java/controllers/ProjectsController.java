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
import java.util.Objects;
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

    public ProjectsController() {

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
     * @throws NullPointerException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws NoSuchElementException
     * @throws RuntimeException
     */
    public List<ProjectDto> getByUserId(UUID userId) {
        Objects.requireNonNull(userId);

        try {
            var result = projectService.getByUserIdAsync(userId).get();
            return result.stream().map(ProjectMapper::toDto).toList();
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
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
     * @throws NullPointerException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws NoSuchElementException
     * @throws RuntimeException
     */
    public List<ProjectDto> getByAdminId(UUID adminId){
        Objects.requireNonNull(adminId);

        try {
            return projectService.getByAdminIdAsync(adminId).get().stream().map(ProjectMapper::toDto).toList();
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
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
     * @throws NullPointerException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws NoSuchElementException
     * @throws RuntimeException
     */
    public ProjectDto getProject(UUID projectId) {
        Objects.requireNonNull(projectId);

        try {
            var result = projectService.getByIdAsync(projectId).get();
            return ProjectMapper.toDto(result);
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
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
     * @throws NullPointerException
     * @throws NoSuchElementException
     * @throws InterruptedException
     * @throws RuntimeException
     */
    public ProjectDto create(Project project) {
        Objects.requireNonNull(project);

        try {
            return ProjectMapper.toDto(projectService.createAsync(project).get());
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
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
     * @throws NoSuchElementException
     * @throws RuntimeException
     */
    public boolean delete(UUID projectId) {
        Objects.requireNonNull(projectId);

        try {
            return projectService.deleteByIdAsync(projectId).get();
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
}
