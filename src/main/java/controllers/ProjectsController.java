package controllers;

import controllers.interfaces.BaseProjectController;
import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ProjectsService;
import services.interfaces.ProjectService;
import servlets.ProjectsServlet;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.ProjectNotFoundException;
import utils.exceptions.ProjectUpdateException;
import utils.mappers.ProjectMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
public class ProjectsController implements BaseProjectController<Project, ProjectDto> {

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
    @Override
    public CompletableFuture<List<ProjectDto>> getByUserId(UUID userId) throws SQLException, RuntimeException, ProjectNotFoundException, NullPointerException {
        Objects.requireNonNull(userId);

        return projectService.getByUserIdAsync(userId);
                //.thenApply(projects -> projects.stream().map(ProjectMapper::toDto).toList());
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
    @Override
    public CompletableFuture<List<ProjectDto>> getByAdminId(UUID adminId) throws SQLException, RuntimeException, ProjectNotFoundException, NullPointerException  {
        Objects.requireNonNull(adminId);

        return projectService.getByAdminIdAsync(adminId);
                //.thenApply(projects -> projects.stream().map(ProjectMapper::toDto).toList());
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
    @Override
    public CompletableFuture<ProjectDto> getByProjectId(UUID projectId) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException{
        Objects.requireNonNull(projectId);

        return projectService.getByIdAsync(projectId);
                //.thenApply(ProjectMapper::toDto);
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
    @Override
    public CompletableFuture<ProjectDto> create(Project project) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException {
        Objects.requireNonNull(project);

        return projectService.createAsync(project);
                //.thenApply(ProjectMapper::toDto);
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
    @Override
    public CompletableFuture<Boolean> delete(UUID projectId) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException {
        Objects.requireNonNull(projectId);

        return projectService.deleteByIdAsync(projectId)
                .thenApply(result -> result);
    }

    /**
     * Добавить пользователя в проект по
     * {@code userId} и {@code projectId}
     *
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     *
     * @throws RuntimeException
     * @throws NullPointerException
     */
    @Override
    public CompletableFuture<ProjectDto> addUserToProject(UUID userId, UUID projectId) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(projectId);

        return projectService.addUserToProjectAsync(userId, projectId);
                //.thenApply(ProjectMapper::toDto);
    }

    /**
     * Удалить пользователя из проекта по
     * {@code userId} и {@code projectId}
     *
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     *
     * @throws RuntimeException
     * @throws NullPointerException
     */
    @Override
    public CompletableFuture<ProjectDto> removeUserFromProject(UUID userId, UUID projectId) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(projectId);

        return projectService.removeUserFromProjectAsync(userId, projectId);
                //.thenApply(ProjectMapper::toDto);
    }

    /**
     * Обновить данные проекта
     *
     * @param projectDto
     * @return {@code ProjectDto}
     *
     * @throws NullPointerException
     * @throws ProjectNotFoundException
     * @throws ProjectUpdateException
     */
    @Override
    public CompletableFuture<ProjectDto> update(ProjectDto projectDto) throws SQLException, DatabaseOperationException, NullPointerException,  RuntimeException {
        Objects.requireNonNull(projectDto);

        return projectService.updateByIdAsync(projectDto);
                //.thenApply(ProjectMapper::toDto);
    }
}
