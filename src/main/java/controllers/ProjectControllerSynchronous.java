package controllers;

import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    Logger logger = LoggerFactory.getLogger(ProjectControllerSynchronous.class);
    private final services.interfaces.synchronous.ProjectServiceSynchro projectService;

    public ProjectControllerSynchronous() {

        this.projectService = new services.synchronous.ProjectsService();
    }

    /**
     * Получить список всех проектов по {@code userId},
     * который является администратором
     * <p>
     *     Применяется в сервлете {@code GetProjectsByUserIdServlet}
     *     для загрузки в проект списка проектов пользователя
     * </p>
     * <p>
     *     Не получилось в установленные сроки реализовать этот функционал
     *     в асинхронном контроллере
     * </p>
     * @param userId
     * @return {@code List<ProjectDto>}
     * @throws NoSuchElementException
     * @throws NullPointerException
     */
    public List<ProjectDto> getByUserId(UUID userId) {
        Objects.requireNonNull(userId);

        try {
            var result = projectService.getByUserId(userId);
            logger.info("ProjectControllerSynchro: getByUserId Получение всех проектов пользователя");
            return result.stream().map(ProjectMapper::toDto).toList();
        } catch (NoSuchElementException e) {
            logger.error(String.format("ProjectControllerSynchro: getByUserId Ошибка получения всех проектов пользователя: %s", e.getMessage()));
            throw new NoSuchElementException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получить список всех проектов по {@code adminId},
     * который является администратором
     * <p>
     *     Применяется в сервлете {@code GetProjectsByUserIdServlet}
     *     для загрузки в проект списка проектов пользователя
     * </p>
     * <p>
     *     Не получилось в установленные сроки реализовать этот функционал
     *     в асинхронном контроллере
     * </p>
     * @param adminId
     * @return {@code List<ProjectDto>}
     * @throws NoSuchElementException
     * @throws NullPointerException
     */
    public List<ProjectDto> getByAdminId(UUID adminId){
        Objects.requireNonNull(adminId);

        try {
            logger.info("ProjectControllerSynchro: getByAdminId Получение всех проектов администратора");
            return projectService.getByAdminId(adminId).stream().map(ProjectMapper::toDto).toList();
        }
        catch (NoSuchElementException e) {
            logger.error(String.format("ProjectControllerSynchro: getByAdminId Ошибка получения проектов администратора: %s", e.getMessage()));
            throw new NoSuchElementException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получить проект по {@code Id} проекта
     * <p>
     *     Метод не применяется.
     *     Используется в асинхроной реализации интерфейса
     * </p>
     * @param projectId
     * @return {@code ProjectDto}
     */
    public ProjectDto getProject(UUID projectId) {
        /*Objects.requireNonNull(projectId);

        try {
            logger.info("ProjectControllerSynchro: getProject Получение проекта по id");
            var result = projectService.getById(projectId);
            return ProjectMapper.toDto(result);
        }
        catch (NoSuchElementException e) {
            logger.error(String.format("ProjectControllerSynchro: getProject Ошибка получения проекта по id: %s", e.getMessage()));
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        }*/
        return null;
    }

    /**
     * Создать новый проект: {@code project}
     * <p>
     *     Метод не применяется.
     *     Используется в асинхроной реализации интерфейса
     * </p>
     * @param project
     * @return {@code ProjectDto}
     */
    public ProjectDto create(Project project) {
        /*Objects.requireNonNull(project);

        try {
            logger.info("ProjectControllerSynchro: create Создание нового проекта");
            return ProjectMapper.toDto(projectService.create(project));
        }
        catch (NoSuchElementException e) {
            logger.error(String.format("ProjectControllerSynchro: create Ошибка создания нового проекта: %s", e.getMessage()));
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        }*/
        return null;
    }

    /**
     * Удалить проект
     * <p>
     *     Метод не применяется.
     *     Используется в асинхроной реализации интерфейса
     * </p>
     * @param projectId
     * @return {@code boolean}
     */
    public boolean delete(UUID projectId) {
        /*Objects.requireNonNull(projectId);

        try {
            logger.info("ProjectControllerSynchro: delete Удаление проекта по id");
            return projectService.deleteById(projectId);
        }
        catch (NoSuchElementException e) {
            logger.error(String.format("ProjectControllerSynchro: delete Ошибка удаления проекта по id: %s", e.getMessage()));
            throw new NoSuchElementException(StaticConstants.PROJECT_NOT_FOUND_EXCEPTION_MESSAGE);
        }*/
        return false;
    }

    /**
     * Добавить пользователя в проект по
     * {@code userId} и {@code projectId}
     * <p>
     *     Метод не применяется.
     *     Используется в асинхроной реализации интерфейса
     * </p>
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     */
    public ProjectDto addUserToProject(UUID userId, UUID projectId) {
        /*try {
            logger.info("ProjectControllerSynchro: addUserToProject Добавление пользователя в проект");
            Project project = projectService.addUserToProject(userId, projectId);

            return ProjectMapper.toDto(project);
        }
        catch (Exception e) {
            logger.error(String.format("ProjectControllerSynchro: addUserToProject Ошибка добавления пользователя в проект: %s", e.getMessage()));
            throw new RuntimeException(e);
        }*/
        return null;
    }

    /**
     * Удалить пользователя из проекта по
     * {@code userId} и {@code projectId}
     * <p>
     *     Метод не применяется.
     *     Используется в асинхроной реализации интерфейса
     * </p>
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     */
    public ProjectDto removeUserFromProject(UUID userId, UUID projectId) {
       /* try {
            logger.info("ProjectControllerSynchro: removeUserFromProject Удаление пользователя из проекта");
            Project project = projectService.removeUserFromProject(userId, projectId);
            return ProjectMapper.toDto(project);
        }
        catch (Exception e) {
            logger.error(String.format("ProjectControllerSynchro: removeUserFromProject Ошибка удаления пользователя из проекта: %s", e.getMessage()));
            throw new RuntimeException(e);
        }*/
        return null;
    }

    /**
     * Обновить проект
     * <p>
     *     Метод не используется;
     *     Применяется метод другой реализации контроллера {@code ProjectsController}
     * </p>
     *
     * @param projectDto
     * @return {@code null}
     */
    @Override
    public ProjectDto updateProject(ProjectDto projectDto) {
        return null;
    }
}
