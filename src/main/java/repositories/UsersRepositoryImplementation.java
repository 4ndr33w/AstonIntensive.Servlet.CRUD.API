package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.entities.User;
import repositories.interfaces.UserRepository;
import utils.StaticConstants;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static utils.mappers.UserMapper.mapResultSetToUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersRepositoryImplementation implements UserRepository, AutoCloseable{

    private static final String usersSchema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String usersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");
    private final String usersTableName = String.format("%s.%s", usersSchema, usersTable);
    private final SqlQueryStrings sqlQueryStrings;
    private static final ExecutorService dbExecutor;

    static {
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    public UsersRepositoryImplementation() {
        sqlQueryStrings = new SqlQueryStrings();
    }

    /**
     * Асинхронный поиск всех пользователей
     * <p>Возвращает {@code Optional}, содержащий коллекцию пользователей</p>
     *
     * @see User
     * @return {@code Optional<List<User>>} контейнер с результатом запроса:
     *         <ul>
     *             <li>Optional с непустым списком - если пользователи найдены</li>
     *             <li>Optional с null - если пользователи не найдены</li>
     *             <li>пустой Optional - при ошибке выполнения</li>
     *         </ul>
     * @throws RuntimeException если произошла ошибка при выполнении SQL-запроса.
     *         Исключение-обертка для оригинального исключения БД, которое можно получить через
     *         {@link Throwable#getCause()}. Может содержать следующие исходные исключения:
     *         <ul>
     *             <li>{@link java.sql.SQLException} - ошибка SQL-запроса</li>
     *             <li>{@link java.sql.SQLTimeoutException} - превышено время выполнения запроса</li>
     *             <li>{@link java.sql.SQLSyntaxErrorException} - синтаксическая ошибка в запросе</li>
     *         </ul>
     */
    @Override
    public CompletableFuture<List<User>> findAllAsync() {
        return CompletableFuture.supplyAsync(() -> {
            String queryString = sqlQueryStrings.findAllQueryString(usersTableName);
            try (JdbcConnection conn = new JdbcConnection();
                 ResultSet rs = conn.executeQuery(queryString )) {

                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                return users;
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
    }

    /**
     * Асинхронное создание пользователя
     *
     * @param item объект пользователя для создания (не null)
     * @return CompletableFuture с созданным пользователем (с заполненным ID)
     * @throws RuntimeException если произошла ошибка при выполнении операции
     */
    @Override
    public CompletableFuture<User> createAsync(User item) {
        return CompletableFuture.supplyAsync(() -> {
            if (item == null) {
                throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
            }
            String queryString = sqlQueryStrings.createUserString(usersTableName, item);

            try (JdbcConnection jdbcConnection = new JdbcConnection();
                 Statement statement = jdbcConnection.statement()) {

                int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

                if (affectedRows == 0) {
                    throw new SQLException(StaticConstants.ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE);
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId((UUID) generatedKeys.getObject(1));
                        return item;
                    }
                    throw new SQLException(StaticConstants.FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE);
                }
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
    }

    /**
     * Асинхронное удаление пользователя по идентификатору {@code id}
     *
     * <p>Выполняет удаление данных из таблицы в отдельном потоке. Операция не блокирует вызывающий поток.</p>
     *
     * <p>Поведение метода:</p>
     * <ul>
     *     <li>Возвращает {@code CompletableFuture} с {@code true}, если:
     *         <ul>
     *             <li>Удаление выполнено успешно</li>
     *             <li>Затронута хотя бы одна строка в таблице</li>
     *         </ul>
     *     </li>
     *     <li>Возвращает {@code CompletableFuture} с {@code false}, если:
     *         <ul>
     *             <li>Параметр {@code id} равен {@code null}</li>
     *             <li>Не найдена строка для удаления (запрос выполнен, но affectedRows = 0)</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * <p>Особенности реализации:</p>
     * <ul>
     *     <li>Использует выделенный пул потоков ({@code dbExecutor}) для выполнения операции</li>
     *     <li>Автоматически управляет ресурсами соединения с БД (try-with-resources)</li>
     * </ul>
     *
     * @param id идентификатор пользователя ({@code UUID}), не null
     * @return {@code CompletableFuture<Boolean>} результат операции:
     *         <ul>
     *             <li>{@code true} - удаление выполнено успешно</li>
     *             <li>{@code false} - удаление не выполнено (см. условия выше)</li>
     *         </ul>
     * @throws RuntimeException если произошла ошибка при выполнении операции. Исключение содержит:
     *         <ul>
     *             <li>{@link java.sql.SQLException} - ошибка SQL-запроса</li>
     *             <li>{@link java.sql.SQLTimeoutException} - превышение времени ожидания</li>
     *             <li>{@link java.sql.SQLSyntaxErrorException} - синтаксическая ошибка запроса</li>
     *             <li>{@link java.util.concurrent.CompletionException} - ошибка выполнения асинхронной задачи</li>
     *         </ul>
     * @see #dbExecutor
     * @see JdbcConnection
     * @implNote Для обработки результатов используйте методы {@code CompletableFuture}:
     *           {@code thenApply()}, {@code exceptionally()} и др.
     */
    @Override
    public CompletableFuture<Boolean> deleteAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) {
                throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
            }

            String queryString = sqlQueryStrings.deleteByIdString(usersTableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                int affectedRows = jdbcConnection.executeUpdate(queryString);
                return affectedRows > 0;
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<User> findByIdAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) {
                throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
            }

            String queryString = sqlQueryStrings.findByIdString(usersTableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                var resultSet  = jdbcConnection.executeQuery(queryString);
                return resultSet.next() ? mapResultSetToUser(resultSet) : null;
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<List<User>> findAllByIdsAsync(List<UUID> userIds) {
        return CompletableFuture.supplyAsync(() -> {
            if (userIds == null || userIds.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> ids = userIds.stream().map(UUID::toString).toList();
            String sql = sqlQueryStrings.findAllByIdsString(usersTableName, ids);

            /*String sql = String.format("SELECT * FROM %s WHERE id IN (", usersTableName) +
                    userIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";*/

            try (JdbcConnection connection = new JdbcConnection();
                 PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {

                // Устанавливаем параметры для IN-условия
                for (int i = 0; i < userIds.size(); i++) {
                    statement.setObject(i + 1, userIds.get(i));
                }

                ResultSet resultSet = statement.executeQuery();
                List<User> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(UserMapper.mapResultSetToUser(resultSet));
                }

                return result;
            } catch (SQLException e) {
                throw new CompletionException("Failed to find users by ids", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }




    // TODO: реализовать остальные методы интерфейса UserRepository

    @Override
    public CompletableFuture<User> updateAsync(User item) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> findByEmailAsync(String email) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> findByUserNameAsync(String userName) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> updateEmailAsync(String oldEmail, String newEmail) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<User> updatePasswordAsync(UUID userId, String newPassword) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void close() {
        dbExecutor.shutdown();
    }
}