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
    @Override
    public List<ProjectDto> getByUserId(UUID userId) {
        Objects.requireNonNull(userId);

        try {
            var result = projectService.getByUserIdAsync(userId).get();
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
    @Override
    public List<ProjectDto> getByAdminId(UUID adminId) {
        Objects.requireNonNull(adminId);

        try {
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
    @Override
    public ProjectDto getProject(UUID projectId) {
        Objects.requireNonNull(projectId);

        try {
            var result = projectService.getByIdAsync(projectId).get();
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
    @Override
    public ProjectDto create(Project project) {
        Objects.requireNonNull(project);

        try {
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
    @Override
    public boolean delete(UUID projectId) {
        Objects.requireNonNull(projectId);

        try {
            return projectService.deleteByIdAsync(projectId).get();
        }
        catch (NoSuchElementException e) {
            logger.warn("ProjectController: deleteById: {}", e.getMessage());
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        } catch (ExecutionException | InterruptedException | SQLException e) {
            logger.error("ProjectController: deleteById: {}", e.getMessage());
            throw new RuntimeException(e);
        }
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
    public ProjectDto addUserToProject(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(projectId);
        try {
            CompletableFuture<Project> future = projectService.addUserToProjectAsync(userId, projectId);
            Project project = future.get();
            return ProjectMapper.toDto(project);
        }
        catch (Exception e) {
            logger.error("ProjectController: addUserToProject:\n Exception(230) {}", e.getMessage());
            throw new RuntimeException(e);
        }
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
    public ProjectDto removeUserFromProject(UUID userId, UUID projectId) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(projectId);
        try {
            CompletableFuture<Project> future = projectService.removeUserFromProjectAsync(userId, projectId);
            Project project = future.get();
            return ProjectMapper.toDto(project);
        }
        catch (Exception e) {
            logger.error("ProjectController: removeUserFromProject: {}", e.getMessage());
            throw new RuntimeException(e);
        }
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
    public ProjectDto updateProject(ProjectDto projectDto) {
        Objects.requireNonNull(projectDto);

        Project project = ProjectMapper.mapToEntity(projectDto, List.of());

        try {
              Project updatedProject = projectService.updateByIdAsync(project).join();

                return ProjectMapper.toDto(updatedProject);

        } catch (CompletionException ex) {
            if (ex.getCause() instanceof NoSuchElementException) {
                logger.warn("ProjectController: updateProject:\n {}", ex.getMessage());
                throw new ProjectNotFoundException("Project not found with id: " + projectDto.getId());
            }
            logger.error("ProjectController: updateProject: {}", ex.getMessage());
            throw new ProjectUpdateException("Failed to update project", ex);
        }
    }
}
