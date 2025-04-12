package repositories;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import models.entities.User;
import repositories.interfaces.BaseRepository;
import repositories.interfaces.UserRepository;
import utils.sqls.SqlQueryStrings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static utils.mappers.UserMapper.mapResultSetToUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersRepository implements UserRepository{
    private static final String usersSchema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    private static final String usersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");
    private final SqlQueryStrings sqlQueryStrings;

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
    public Optional<List<User>> findAll() {
        final String query = sqlQueryStrings.findAllQueryString(String.format("%s.%s", usersSchema, usersTable));
        List<User> users = new ArrayList<>();

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             PreparedStatement stmt = jdbcConnection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }

            return Optional.ofNullable(users.isEmpty() ? null : users);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Создание пользователя
     * @return {@code Optional<User>}
     * @throws RuntimeException
     */
    @Override
    public Optional<User> create(User item) {
        if (item == null) {
            return Optional.empty();
        }

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            String queryString = sqlQueryStrings.createUserString(String.format("%s.%s", usersSchema, usersTable), item);

            Statement statement = jdbcConnection.statement();

            int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

            if (affectedRows == 0) {
                return Optional.empty();
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId((UUID) generatedKeys.getObject(1));
                }
            }
            return Optional.of(item);
        } catch (Exception ex) {
            System.err.println("Failed to create user: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
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
    public boolean delete(UUID id) {
        String tableName = String.format("%s.%s", usersSchema, usersTable);

        if(id != null) {
            String queryString = sqlQueryStrings.deleteById(tableName, id.toString());

            try (JdbcConnection jdbcConnection = new JdbcConnection()) {

                int affectedRows = jdbcConnection.executeUpdate(queryString);

                return affectedRows > 0;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<List<User>> findByName(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<User> update(User item) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return Optional.empty();
    }

    @Override
    public Optional<User> updateEmail(String oldEmail, String newEmail) {
        return Optional.empty();
    }

    @Override
    public Optional<User> updatePassword(UUID userId, String newPassword) {
        return Optional.empty();
    }
}