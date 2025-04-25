package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfNonStatic;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.UserRepository;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.MultipleUsersNotFoundException;
import utils.exceptions.UserAlreadyExistException;
import utils.exceptions.UserNotFoundException;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryPreparedStrings;
import utils.sqls.SqlQueryStrings;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static utils.mappers.UserMapper.mapResultSetToUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersRepository implements UserRepository, AutoCloseable{

    static String usersSchema = System.getenv("JDBC_DEFAULT_SCHEMA") != null
            ? System.getenv("JDBC_DEFAULT_SCHEMA")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");

    static String usersTable = System.getenv("JDBC_USERS_TABLE") != null
            ? System.getenv("JDBC_USERS_TABLE")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");


    private final String usersTableName = String.format("%s.%s", usersSchema, usersTable);
    private final SqlQueryStrings sqlQueryStrings;
    private final ExecutorService dbExecutor;
    private final SqlQueryPreparedStrings sqlQueryPreparedStrings;

    Logger logger = LoggerFactory.getLogger(UsersRepository.class);

    public UsersRepository() {
        sqlQueryStrings = new SqlQueryStrings();
        ThreadPoolConfNonStatic threadPoolConfNonStatic = new ThreadPoolConfNonStatic();
        dbExecutor = threadPoolConfNonStatic.getDbExecutor();
        sqlQueryPreparedStrings = new SqlQueryPreparedStrings();
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
     * @throws DatabaseOperationException
     * @throws CompletionException
     * @throws MultipleUsersNotFoundException
     */
    @Override
    public CompletableFuture<List<User>> findAllAsync() throws DatabaseOperationException, CompletionException, MultipleUsersNotFoundException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return findAll();
            }
            catch (SQLException e) {
                logger.error(String.format("%s; %s", StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e.getCause()));
                throw new DatabaseOperationException(StaticConstants.UNABLE_TO_LOAD_DB_DRIVER);
            }}, dbExecutor)
                .exceptionally(ex -> {
                    if(ex.getCause() instanceof DatabaseOperationException) {
                        throw new DatabaseOperationException(ex.getCause().getMessage());
                    }
                    if(ex.getCause() instanceof SQLException) {
                        logger.error(String.format("%s; %s", StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, ex.getCause()));
                        throw new DatabaseOperationException(String.format("%s; %s", StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, ex.getCause()));
                    }

                    else {
                        throw new CompletionException(StaticConstants.OPERATION_FAILED_ERROR_MESSAGE, ex.getCause());
                    }
                });
    }

    private List<User> findAll() throws SQLException, MultipleUsersNotFoundException {
        String queryString = sqlQueryPreparedStrings.findAllQueryString(usersTableName);
        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.prepareStatementReturningGeneratedKey(queryString)) {

            ResultSet resultSet = statement.executeQuery();
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
            if (users.isEmpty()) {
                logger.info(StaticConstants.USERS_NOT_FOUND_EXCEPTION_MESSAGE);
                throw new MultipleUsersNotFoundException(StaticConstants.USERS_NOT_FOUND_EXCEPTION_MESSAGE);
            }
            return users;
        }
        catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Асинхронное создание пользователя
     *
     * @param user объект пользователя для создания (не null)
     * @return CompletableFuture с созданным пользователем (с заполненным ID)
     * @throws DatabaseOperationException если произошла ошибка при выполнении операции
     * @throws NullPointerException если параметр {@code user} равен {@code null}
     */
    @Override
    public CompletableFuture<User> createAsync(User user) throws UserAlreadyExistException, NullPointerException {
        return CompletableFuture.supplyAsync(() -> {
            if (user == null) {
                logger.error(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
                throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
            }
            return create(user);
        }/*, dbExecutor*/);
    }

    private User create(User user) throws UserAlreadyExistException {
        String queryString = sqlQueryPreparedStrings.createUserPreparedQueryString(usersTableName);

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.prepareStatementReturningGeneratedKey(queryString)) {

            setPreparedStatementUserData(statement, user);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                logger.error(StaticConstants.USER_ALREADY_EXISTS_EXCEPTION_MESSAGE);
                throw new UserAlreadyExistException(StaticConstants.USER_ALREADY_EXISTS_EXCEPTION_MESSAGE);
            }

            user.setId(getGeneratedKeyFromRequest(statement));
            return user;
        }
        catch (Exception e) {

            if(e.getMessage().contains("duplicate key")) {
                throw new UserAlreadyExistException(StaticConstants.USER_ALREADY_EXISTS_EXCEPTION_MESSAGE, e);
            }
            throw new DatabaseOperationException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }

    private void setPreparedStatementUserData(PreparedStatement statement, User user) throws SQLException {

        long updatedTime = user.getCreatedAt().getTime();
        Timestamp updated = new Timestamp(updatedTime );

        long lastLoginTime = user.getCreatedAt().getTime();
        Timestamp lastLogin = new Timestamp(lastLoginTime );

        statement.setString(1, user.getUserName());
        statement.setString(2, user.getFirstName());
        statement.setString(3, user.getLastName());
        statement.setString(4, user.getEmail());
        statement.setString(5, user.getPassword());
        statement.setString(6, user.getPhoneNumber());
        statement.setTimestamp(7, updated);
        statement.setBytes(8, user.getUserImage());
        statement.setTimestamp(9, lastLogin);
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
     * @param id идентификатор пользователя ({@code UUID}), не null
     * @return {@code CompletableFuture<Boolean>} результат операции:
     *         <ul>
     *             <li>{@code true} - удаление выполнено успешно</li>
     *             <li>{@code false} - удаление не выполнено (см. условия выше)</li>
     *         </ul>
     * @throws CompletionException если произошла ошибка при выполнении операции. Исключение содержит:
     *         <ul>
     *             <li>{@link SQLException} - ошибка SQL-запроса</li>
     *             <li>{@link DatabaseOperationException} - превышение времени ожидания</li>
     *             <li>{@link NullPointerException} - синтаксическая ошибка запроса</li>
     *         </ul>
     * @see #dbExecutor
     * @see JdbcConnection
     * @implNote Для обработки результатов используйте методы {@code CompletableFuture}:
     *           {@code thenApply()}, {@code exceptionally()} и др.
     */
    @Override
    public CompletableFuture<Boolean> deleteAsync(UUID id)throws SQLException, DatabaseOperationException, NullPointerException, UserNotFoundException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return delete(id);
            } catch (SQLException e) {
                throw new DatabaseOperationException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE);
            }
        }/*, dbExecutor*/);
    }

    private boolean delete(UUID id) throws SQLException, DatabaseOperationException, NullPointerException, UserNotFoundException {
        if (id == null) {
            throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }

        String queryString = sqlQueryPreparedStrings.deleteByIdString(usersTableName);

        try (JdbcConnection jdbcConnection = new JdbcConnection();
        PreparedStatement statement = jdbcConnection.prepareStatement(queryString)) {
            statement.setObject(1, id, Types.OTHER);
            int affectedRows = statement.executeUpdate();
            if(affectedRows == 1) {
                return true;
            }
            if(affectedRows == 0) {
                throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
            }
        }
        catch (Exception e) {
            throw new DatabaseOperationException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
        return false;
    }

    @Override
    public CompletableFuture<User> findByIdAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            Objects.requireNonNull(id);

            String queryString = sqlQueryStrings.findByIdString(usersTableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                var resultSet  = jdbcConnection.executeQuery(queryString);
                return resultSet.next() ? mapResultSetToUser(resultSet) : null;
            }
            catch (Exception e) {
                throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
            }
        }/*, dbExecutor*/);
    }

    private User findById(UUID id) {
        String queryString = sqlQueryStrings.findByIdString(usersTableName, id.toString());

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            var resultSet  = jdbcConnection.executeQuery(queryString);
            return resultSet.next() ? mapResultSetToUser(resultSet) : null;
        }
        catch (Exception e) {
            throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public CompletableFuture<List<User>> findAllByIdsAsync(List<UUID> userIds) {
        return CompletableFuture.supplyAsync(() -> {
            if (userIds == null || userIds.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> ids = userIds.stream().map(UUID::toString).toList();
            String sql = sqlQueryStrings.findAllByIdsString(usersTableName, ids);

            try (JdbcConnection connection = new JdbcConnection();
                 PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {

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
        }/*, dbExecutor*/);
    }

    @Override
    public CompletableFuture<User> updateAsync(User user) {
        Objects.requireNonNull(user, StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);

        return CompletableFuture.supplyAsync(() -> {

            String updateQuery = sqlQueryStrings.updateUsertByIdString(
                    usersTableName, user.getId().toString(), user);

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {
                jdbcConnection.setAutoCommit(false);

                try {
                    int affectedRows = jdbcConnection.executeUpdate(updateQuery);

                    if (affectedRows == 0) {
                        logger.error(String.format("Repository: update: error: User with id %s not found", user.getId()));
                        throw new NoSuchElementException("User with id " + user.getId() + " not found");
                    }

                    logger.info("Repository: update: committed changes");
                    jdbcConnection.commit();

                    logger.info("Repository: update: return updated project: ");
                    return user;

                } catch (SQLException e) {
                    jdbcConnection.rollback();
                    logger.error(String.format("Repository: update: error: %s", e.getMessage()));
                    throw new CompletionException("Failed to update project", e);
                }
            } catch (SQLException e) {
                logger.error(String.format("Repository: update: error: %s", e.getMessage()));
                throw new CompletionException("Database connection error", e);
            } catch (Exception e) {
                logger.error(String.format("Repository: update: error: %s", e.getMessage()));
                throw new CompletionException("Unexpected error", e);
            }
        }/*, dbExecutor*/);
    }

    @Override
    public void close() {
        dbExecutor.shutdown();
    }


}