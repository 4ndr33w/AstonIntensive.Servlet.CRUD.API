package controllers;

import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.User;
import services.UsersService;
import services.interfaces.UserService;
import utils.StaticConstants;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Класс контроллера, предоставляющего методы для работы с пользователями
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersController {

    public UserService userService;

    public UsersController() throws SQLException {
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
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    public List<UserDto> getAll() throws SQLException, ExecutionException, InterruptedException {
        return userService.getAllAsync().get().stream().map(UserMapper::toDto).toList();
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
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public UserDto getUser(UUID userId) throws SQLException, ExecutionException, InterruptedException {
        if (userId != null) {
            var result = userService.getByIdAsync(userId).get();
            if (result!= null) {
                return UserMapper.toDto(result);
            }
            return null;
        }
        else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
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
     * @throws Exception
     * @throws IllegalArgumentException
     */
    public UserDto create(User user) throws Exception {
        if (user != null) {
            return UserMapper.toDto(userService.createAsync(user).get());
        }
        else {
            throw new Exception(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
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
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    public boolean delete(UUID userId) throws SQLException, ExecutionException, InterruptedException {
        if (userId != null) {
            return userService.deleteByIdAsync(userId).get();
        } else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
    }
    /*
        public boolean delete(UUID id) throws SQLException, ExecutionException, InterruptedException {
        if (id != null) {
            return projectService.deleteByIdAsync(id).get();
        } else {
            throw new IllegalArgumentException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
    }
     */
}
