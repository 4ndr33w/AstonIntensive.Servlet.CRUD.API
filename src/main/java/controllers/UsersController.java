package controllers;

import controllers.interfaces.BaseUserController;
import models.dtos.UserDto;
import models.entities.User;
import services.UsersService;
import services.interfaces.UserService;
import utils.exceptions.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс контроллера,
 * Предоставляет методы для{@code CRUD}-операций с данными пользователей
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersController implements BaseUserController<User, UserDto> {

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
    @Override
    public CompletableFuture<List<UserDto>> getAll() throws SQLException, DatabaseOperationException, CompletionException, NoUsersFoundException, ResultSetMappingException {

        return userService.getAllAsync();
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
    @Override
    public CompletableFuture<UserDto> getUser(UUID userId) throws NullPointerException, UserNotFoundException, DatabaseOperationException, ResultSetMappingException, SQLException {
        Objects.requireNonNull(userId);

        return userService.getByIdAsync(userId);
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
    @Override
    public CompletableFuture<UserDto> create(User user) throws DatabaseOperationException, NullPointerException, CompletionException, UserAlreadyExistException, SQLException {
        Objects.requireNonNull(user);
        return userService.createAsync(user);
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
    public CompletableFuture<Boolean> delete(UUID userId)throws SQLException, DatabaseOperationException, NullPointerException, UserNotFoundException, CompletionException {
        Objects.requireNonNull(userId);
        return userService.deleteByIdAsync(userId);
    }

    @Override
    public CompletableFuture<UserDto> updateUser(UserDto userDto) throws SQLException {
        Objects.requireNonNull(userDto);

        return userService.updateByIdAsync(userDto);
    }
}
