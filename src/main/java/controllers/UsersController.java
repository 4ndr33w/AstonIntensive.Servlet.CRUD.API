package controllers;

import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.UsersService;
import services.interfaces.UserService;
import utils.StaticConstants;
import utils.exceptions.ProjectNotFoundException;
import utils.exceptions.ProjectUpdateException;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Класс контроллера,
 * Предоставляет методы для{@code CRUD}-операций с данными пользователей
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersController {

    public UserService userService;
    Logger logger = LoggerFactory.getLogger(ProjectsController.class);

    public UsersController() {
        this.userService = new UsersService();
    }

    /**
     * Получить список всех пользователей
     * <p>
     * Метод извлекает из {@link CompletableFuture} список объектов типа {@code List<User>},
     * маппит их в список DTO {@code List<UserDto>}
     * или возвращает {@code List<UserDto>} из 0 элементов.
     * </p>
     * @return {@code List<ProjectDto>} или пустой список
     * @throws RuntimeException
     */
    public List<UserDto> getAll() {
        try {
            return userService.getAllAsync().get().stream().map(UserMapper::toDto).toList();
        }
        catch (Exception ex) {
            throw new RuntimeException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получить пользователя по ID
     * <p>
     *     Метод извлекает из {@link CompletableFuture} объект типа {@code User},
     *     маппит его в DTO {@code UserDto},
     *     или возвращает {@code null} если объект не найден.
     * </p>
     * @param userId
     * @return {@code UserDto} или {@code null}
     * @throws NullPointerException
     * @throws RuntimeException
     */
    public UserDto getUser(UUID userId) {
        Objects.requireNonNull(userId);

        try {
            var result = userService.getByIdAsync(userId).get();
            return UserMapper.toDto(result);
        }
        catch (Exception ex) {
            throw new RuntimeException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Создать нового пользователя
     * <p>
     * Метод передаёт в сервис объект типа {@code User},
     * обратно извлекая из {@link CompletableFuture} объект типа {@code UserDto}
     * </p>
     * @param user
     * @return {@code UserDto}
     * @throws NullPointerException
     * @throws RuntimeException
     */
    public UserDto create(User user) {
        Objects.requireNonNull(user);

        try {
            return UserMapper.toDto(userService.createAsync(user).get());
        }
        catch (Exception ex) {
            throw new RuntimeException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Удалить плльзователя
     * <p>
     * Метод передаёт в сервис Id пользователя {@code userId},
     * обратно извлекая из {@link CompletableFuture} примитив {@code boolean}
     * как маркер выполнения операции удаления.
     * </p>
     * @param userId
     * @return {@code boolean}
     * @throws NullPointerException
     * @throws RuntimeException
     */
    public boolean delete(UUID userId) {
        Objects.requireNonNull(userId);
        try {
            return userService.deleteByIdAsync(userId).get();
        }
        catch (Exception ex) {
            throw new RuntimeException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE);
        }
    }

    public UserDto updateUser(UserDto userDto) {
        Objects.requireNonNull(userDto);

        User user = UserMapper.mapToEntity(userDto);

        try {
            User updatedUser = userService.updateByIdAsync(user).join();

            logger.info("UserController: updateUser:\n Successfully updated {}", updatedUser);
            return UserMapper.toDto(updatedUser);

        } catch (CompletionException ex) {
            if (ex.getCause() instanceof NoSuchElementException) {
                logger.warn("UserController: updateUser:\n {}", ex.getMessage());
                throw new ProjectNotFoundException("User not found with id: " + userDto.getId());
            }
            logger.error("UserController: updateUser: {}", ex.getMessage());
            throw new ProjectUpdateException("Failed to update user", ex);
        }
    }
}
