package controllers;

import controllers.interfaces.ProjectControllerInterface;
import models.dtos.ProjectDto;
import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.mappers.ProjectMapper;

import java.sql.SQLException;
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
        Objects.requireNonNull(projectId);

        return ProjectMapper.toDto(projectService.getById(projectId));
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
        Objects.requireNonNull(project);

        try {
            return ProjectMapper.toDto(projectService.create(project));
        }
        catch(SQLException e) {
            logger.error(String.format("ProjectControllerSynchro: create Ошибка создания проекта: %s", e.getMessage()));
            throw new DatabaseOperationException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
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
    public boolean delete(UUID projectId) throws SQLException {
         Objects.requireNonNull(projectId);

         return projectService.deleteById(projectId);
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

        Objects.requireNonNull(userId);
        Objects.requireNonNull(projectId);
        return ProjectMapper.toDto(projectService.addUserToProject(userId, projectId));
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
        Objects.requireNonNull(userId);
        Objects.requireNonNull(projectId);

          return ProjectMapper.toDto(projectService.removeUserFromProject(userId, projectId));
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
    public ProjectDto updateProject(ProjectDto projectDto)  {
        Objects.requireNonNull(projectDto);

        try {
            Project project = ProjectMapper.mapToEntity(projectDto, List.of());

            return ProjectMapper.toDto(projectService.updateById(project));
        }
        catch (SQLException e) {
            logger.error(String.format("ProjectControllerSynchro: updateProject Ошибка обновления проекта: %s", e.getMessage()));
            throw new DatabaseOperationException(StaticConstants.DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE);
        }
    }
}
