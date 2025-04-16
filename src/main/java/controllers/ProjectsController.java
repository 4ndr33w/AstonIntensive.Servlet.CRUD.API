package controllers;

import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ProjectsService;
import services.interfaces.ProjectService;
import servlets.ProjectsServlet;
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
 * Класс для работы с проектами
 * в многопоточном режиме
 * Предоставляет методы для{@code CRUD}-операций с проектами
 *
 * @see ProjectControllerInterface
 * @see services.interfaces.ProjectService
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsController implements ProjectControllerInterface {

    Logger logger = LoggerFactory.getLogger(ProjectsController.class);

    private final ProjectService projectService;

    public ProjectsController() {

        //this.projectService = new ProjectServiceImplNew();
        this.projectService = new ProjectsService();
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
     */
    public List<ProjectDto> getByUserId(UUID userId) {
        Objects.requireNonNull(userId);

        try {
            var result = projectService.getByUserIdAsync(userId).get();
            logger.info("ProjectController: getByUserId: {}", userId);
            return result.stream().map(ProjectMapper::toDto).toList();
        }
        catch (NoSuchElementException e) {
            logger.warn("ProjectController: getByUserId: {}", e.getMessage());
            throw new NoSuchElementException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("ProjectController: getByUserId: {}", e.getMessage());
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
    public List<ProjectDto> getByAdminId(UUID adminId) {
        Objects.requireNonNull(adminId);

        try {
            logger.info("ProjectController: getByAdminId: {}", adminId);
            return projectService.getByAdminIdAsync(adminId).get().stream().map(ProjectMapper::toDto).toList();
        }
        catch (NoSuchElementException e) {
            logger.warn("ProjectController: getByAdminId: {}", e.getMessage());
            throw new NoSuchElementException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("ProjectController: getByAdminId: {}", e.getMessage());
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
            logger.info("ProjectController: getById: {}", projectId);
            return ProjectMapper.toDto(result);
        }
        catch (NoSuchElementException e) {
            logger.warn("ProjectController: getById: {}", e.getMessage());
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("ProjectController: getById: {}", e.getMessage());
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
            logger.info("ProjectController: create: {}", project);
            return ProjectMapper.toDto(projectService.createAsync(project).get());
        }
        catch (NoSuchElementException e) {
            logger.warn("ProjectController: create: {}", e.getMessage());
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("ProjectController: create: {}", e.getMessage());
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
            logger.info("ProjectController: deleteById: {}", projectId);
            return projectService.deleteByIdAsync(projectId).get();
        }
        catch (NoSuchElementException e) {
            logger.warn("ProjectController: deleteById: {}", e.getMessage());
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("ProjectController: deleteById: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Добавить пользователя в проект по
     * {@code userId} и {@code projectId}
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws NoSuchElementException
     * @throws RuntimeException
     * @throws IllegalArgumentException
     */
    public ProjectDto addUserToProject(UUID userId, UUID projectId) {
        try {
            CompletableFuture<Project> future = projectService.addUserToProjectAsync(userId, projectId);
            Project project = future.get();
            logger.info("ProjectController: addUserToProject:\n Successfully added {}", project);
            return ProjectMapper.toDto(project);
        }
        catch (InterruptedException e) {
            logger.error("ProjectController: addUserToProject:\n InterruptedException(221) {}", e.getMessage());
            //Thread.currentThread().interrupt();
            throw new RuntimeException("ProjectController; Operation was interrupted", e);
        }
        catch (ExecutionException e) {
            logger.error("ProjectController: addUserToProject: ExecutionException(226) {}", e.getMessage());
            throw convertExecutionException(e.getCause());
        }
        catch (Exception e) {
            logger.error("ProjectController: addUserToProject:\n Exception(230) {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Удалить пользователя из проекта по
     * {@code userId} и {@code projectId}
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws NoSuchElementException
     * @throws RuntimeException
     * @throws IllegalArgumentException
     */
    public ProjectDto removeUserFromProject(UUID userId, UUID projectId) {
        try {
            CompletableFuture<Project> future = projectService.removeUserFromProjectAsync(userId, projectId);
            Project project = future.get();
            logger.info("ProjectController: removeUserFromProject: {}", project);
            return ProjectMapper.toDto(project);
        } catch (InterruptedException e) {
            logger.error("ProjectController: removeUserFromProject: {}", e.getMessage());
            //Thread.currentThread().interrupt();
            throw new RuntimeException("ProjectController; Operation was interrupted", e);
        } catch (ExecutionException e) {
            logger.error("ProjectController: removeUserFromProject: {}", e.getMessage());
            throw convertExecutionException(e.getCause());
        } catch (Exception e) {
            logger.error("ProjectController: removeUserFromProject: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private RuntimeException convertExecutionException(Throwable cause) {
        if (cause instanceof IllegalArgumentException) {
            logger.error("ProjectController: convertExecutionException: {}", cause.getMessage());
            return (IllegalArgumentException) cause;
        } else if (cause instanceof NoSuchElementException) {
            logger.error("ProjectController: convertExecutionException: {}", cause.getMessage());
            return (NoSuchElementException) cause;
        } else if (cause instanceof IllegalStateException) {
            logger.error("ProjectController: convertExecutionException: {}", cause.getMessage());
            return (IllegalStateException) cause;
        } else if (cause instanceof SQLException) {
            logger.error("ProjectController: convertExecutionException: {}", cause.getMessage());
            String message = String.format("ProjectController; %s: %s, %s", StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, cause.getMessage(), cause);
            return new RuntimeException(message);
        } else {
            logger.error("ProjectController: convertExecutionException: {}", cause.getMessage());
            String message = String.format("ProjectController; %s: %s, %s", StaticConstants.UNEXPECTED_ERROR_EXCEPTION_MESSAGE, cause.getMessage(), cause);
            return new RuntimeException(message);
        }
    }
}
