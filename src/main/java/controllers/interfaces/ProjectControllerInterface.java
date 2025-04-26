package controllers.interfaces;

import models.dtos.ProjectDto;
import models.entities.Project;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Интерфейс контроллера проекта
 * Понадобился для различной реализации работы с сервисами и репозиториями проектов
 * <p>
 *     <ul>
 *         <li>ProjectController</li>
 *     </ul>
 *     Работает с сервисом, в котором происходят асинхронные запросы к БД
 *     При таком подходе возникла проблема с реализацией некоторых моментов задания:
 *     не подгружались пользователи в проект. По этому была создана другая реализация
 * </p>
 * <p>
 * <ul>
 *      <li>ProjectControllerSynchronous</li>
 * </ul>
 *     Работает с сервисом и репозиторием синхронно
 *     в однопоточном режиме
 * </p>
 * @author 4ndr33w
 * @version 1.0
 */
public interface ProjectControllerInterface {

    /**
     * Получить список всех проектов по id пользователя
     * @param userId
     * @return {@code List<ProjectDto>}
     */
    List<ProjectDto> getByUserId(UUID userId);

    /**
     * Получить список всех проектов по {@code adminId},
     * который является администратором
     * @param adminId
     * @return {@code List<ProjectDto>}
     */
    List<ProjectDto> getByAdminId(UUID adminId);

    /**
     * Получить проект по {@code Id} проекта
     * @param projectId
     * @return {@code ProjectDto}
     */
    ProjectDto getProject(UUID projectId);

    /**
     * Создать новый проект: {@code project}
     * @param project
     * @return {@code ProjectDto}
     */
    ProjectDto create(Project project) throws SQLException;

    /**
     * Удалить проект
     *
     * @param projectId
     * @return {@code boolean}
     */
    boolean delete(UUID projectId) throws SQLException;

    /**
     * Добавить пользователя в проект по
     * {@code userId} и {@code projectId}
     *
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     */
    ProjectDto addUserToProject(UUID userId, UUID projectId);

    /**
     * Удалить пользователя из проекта по
     * {@code userId} и {@code projectId}
     *
     * @param projectId
     * @param userId
     * @return {@code ProjectDto}
     */
    ProjectDto removeUserFromProject(UUID userId, UUID projectId);

    /**
     * Обновить проект
     *
     * @param projectDto
     * @return {@code ProjectDto}
     */
    ProjectDto updateProject(ProjectDto projectDto) throws SQLException;
}
