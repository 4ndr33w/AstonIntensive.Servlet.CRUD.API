package controllers;

import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import models.entities.Project;
import utils.StaticConstants;
import utils.mappers.ProjectMapper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

/**
 * Класс для работы с проектами
 * в однопоточном режиме
 * Предоставляет методы для{@code CRUD}-операций с проектами
 *
 * @see ProjectControllerInterface
 * @see services.interfaces.synchronous.ProjectServiceSynchro
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectControllerSynchronous implements ProjectControllerInterface {

    private final services.interfaces.synchronous.ProjectServiceSynchro projectService;

    public ProjectControllerSynchronous() {

        this.projectService = new services.synchronous.ProjectsService();
    }

    /**
     * Получить список всех проектов по id пользователя
     * @param userId
     * @return {@code List<ProjectDto>}
     */
    public List<ProjectDto> getByUserId(UUID userId) {
        Objects.requireNonNull(userId);

        try {
            var result = projectService.getByUserId(userId);
            return result.stream().map(ProjectMapper::toDto).toList();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получить список всех проектов по {@code adminId},
     * который является администратором
     * @param adminId
     * @return {@code List<ProjectDto>}
     */
    public List<ProjectDto> getByAdminId(UUID adminId){
        Objects.requireNonNull(adminId);

        try {
            return projectService.getByAdminId(adminId).stream().map(ProjectMapper::toDto).toList();
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получить проект по {@code Id} проекта
     * @param projectId
     * @return {@code ProjectDto}
     */
    public ProjectDto getProject(UUID projectId) {
        Objects.requireNonNull(projectId);

        try {
            var result = projectService.getById(projectId);
            return ProjectMapper.toDto(result);
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Создать новый проект: {@code project}
     * @param project
     * @return {@code ProjectDto}
     */
    public ProjectDto create(Project project) {
        Objects.requireNonNull(project);

        try {
            return ProjectMapper.toDto(projectService.create(project));
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Удалить проект
     * @param projectId
     * @return {@code boolean}
     */
    public boolean delete(UUID projectId) {
        Objects.requireNonNull(projectId);

        try {
            return projectService.deleteById(projectId);
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Добавить пользователя в проект по
     * {@code userId} и {@code projectId}
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     */
    public ProjectDto addUserToProject(UUID userId, UUID projectId) {
        try {
            Project project = projectService.addUserToProject(userId, projectId);

            return ProjectMapper.toDto(project);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Удалить пользователя из проекта по
     * {@code userId} и {@code projectId}
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     */
    public ProjectDto removeUserFromProject(UUID userId, UUID projectId) {
        try {
            Project project = projectService.removeUserFromProject(userId, projectId);
            return ProjectMapper.toDto(project);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
