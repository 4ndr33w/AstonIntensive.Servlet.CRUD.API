package repositories;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfiguration;
import models.entities.User;
import repositories.interfaces.UserRepository;
import utils.sqls.SqlQueryStrings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static utils.mappers.UserMapper.mapResultSetToUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersRepository implements UserRepository, AutoCloseable{
    private static final String usersSchema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String usersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");
    private final SqlQueryStrings sqlQueryStrings;
    private static final ExecutorService dbExecutor;

    static {
        /*dbExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("jdbc-worker-%d").build()
                );*/
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    public UsersRepository() throws SQLException {
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
            try (JdbcConnection conn = new JdbcConnection();
                 PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s.%s", usersSchema, usersTable));
                 ResultSet rs = stmt.executeQuery()) {

                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                return users;
            } catch (Exception e) {
                throw new CompletionException(e);
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
                throw new IllegalArgumentException("User item cannot be null");
            }

            String tableName = String.format("%s.%s", usersSchema, usersTable);
            String queryString = sqlQueryStrings.createUserString(tableName, item);

            try (JdbcConnection jdbcConnection = new JdbcConnection();
                 Statement statement = jdbcConnection.statement()) {

                int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

                if (affectedRows == 0) {
                    throw new RuntimeException("Failed to create user, no rows affected");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId((UUID) generatedKeys.getObject(1));
                        return item;
                    }
                    throw new RuntimeException("Failed to retrieve generated keys");
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
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
                return false;
            }

            String tableName = String.format("%s.%s", usersSchema, usersTable);
            String queryString = sqlQueryStrings.deleteByIdString(tableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                int affectedRows = jdbcConnection.executeUpdate(queryString);
                return affectedRows > 0;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<User> findByIdAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) {
                return null;
            }

            String tableName = String.format("%s.%s", usersSchema, usersTable);
            String queryString = sqlQueryStrings.findByIdString(tableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                var resultSet  = jdbcConnection.executeQuery(queryString);
                return resultSet.next() ? mapResultSetToUser(resultSet) : null;
            }
            catch (Exception e) {
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