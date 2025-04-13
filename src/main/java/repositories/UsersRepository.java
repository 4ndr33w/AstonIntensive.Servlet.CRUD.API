package repositories;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.entities.User;
import repositories.interfaces.UserRepository;
import utils.sqls.SqlQueryStrings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        dbExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("jdbc-worker-%d").build()
                );
    }

    public UsersRepository() throws SQLException {
        sqlQueryStrings = new SqlQueryStrings();
    }

    /**
     * Поиск всех пользователей
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
     * Удаление пользователя по {@code id}
     *
     * <p>Удаляет данные из таблицы по {@code id}; в случае успешного удаления возвращает {@code true}</p>
     *
     * <p>Возвращает {@code false} в случаях:
     * <ul>
     *     <li>если {@code id} равен {@code null}</li>
     *     <li>если из БД не удалось удалить строку</li>
     * </ul></p>
     *
     * @param id {@code UUID} идентификатор пользователя
     * @return {@code true} если произошло удаление хотя бы одной строки в таблице,
     *         {@code false} в противном случае
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
    public CompletableFuture<Boolean> deleteAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) {
                return false;
            }

            String tableName = String.format("%s.%s", usersSchema, usersTable);
            String queryString = sqlQueryStrings.deleteById(tableName, id.toString());

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
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<User>> findByNameAsync(String name) {
        return CompletableFuture.completedFuture(null);
    }

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