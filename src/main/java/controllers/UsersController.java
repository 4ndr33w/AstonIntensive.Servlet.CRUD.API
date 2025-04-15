package controllers;

import models.dtos.UserDto;
import models.entities.User;
import services.UsersService;
import services.interfaces.UserService;
import utils.StaticConstants;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Класс контроллера,
 * Предоставляет методы для{@code CRUD}-операций с данными пользователей
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersController {

    public UserService userService;

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
}
