package repositories.synchronous;

import configurations.JdbcConnection;
import configurations.PropertiesConfiguration;
import configurations.ThreadPoolConfNonStatic;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.synchronous.UserRepositorySynchro;
import utils.StaticConstants;
import utils.exceptions.UserNotFoundException;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static utils.mappers.UserMapper.mapResultSetToUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersRepositorySynchronous implements UserRepositorySynchro {

    static String usersSchema = System.getenv("JDBC_DEFAULT_SCHEMA") != null
            ? System.getenv("JDBC_DEFAULT_SCHEMA")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");

    static String usersTable = System.getenv("JDBC_USERS_TABLE") != null
            ? System.getenv("JDBC_USERS_TABLE")
            : PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");


    private final String usersTableName = String.format("%s.%s", usersSchema, usersTable);
    private SqlQueryStrings sqlQueryStrings;

    Logger logger = LoggerFactory.getLogger(UsersRepositoryImplementation.class);

    public UsersRepositorySynchronous() {
        sqlQueryStrings = new SqlQueryStrings();
    }

    @Override
    public Optional<List<User>> findAllByIds(List<UUID> userIds) {
        List<String> ids = userIds.stream().map(UUID::toString).toList();
        String sql = sqlQueryStrings.findAllByIdsString(usersTableName, ids);

        try (JdbcConnection connection = new JdbcConnection();
             PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();
            List<User> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(UserMapper.mapResultSetToUser(resultSet));
            }
            return  result.isEmpty() ? Optional.empty() : Optional.of( result);

        } catch (SQLException e) {
            throw new CompletionException("Failed to find users by ids", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        Objects.requireNonNull(id);

        String queryString = sqlQueryStrings.findByIdString(usersTableName, id.toString());

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            var resultSet  = jdbcConnection.executeQuery(queryString);
            return resultSet.next() ? Optional.of(mapResultSetToUser(resultSet)) :Optional.empty();
        }
        catch (Exception e) {
            throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public Optional<List<User>> findAll() {
        String queryString = sqlQueryStrings.findAllQueryString(usersTableName);
        try (JdbcConnection conn = new JdbcConnection();
             ResultSet rs = conn.executeQuery(queryString )) {

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users.isEmpty() ? Optional.empty() : Optional.of(users);
        }
        catch (Exception e) {
            throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public User create(User item) {
        String queryString = sqlQueryStrings.createUserString(usersTableName, item);

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             Statement statement = jdbcConnection.statement()) {

            int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

            if (affectedRows == 0) {
                throw new SQLException(StaticConstants.ERROR_DURING_SAVING_DATA_INTO_DATABASE_EXCEPTION_MESSAGE);
            }

            item.setId( getGeneratedKeyFromRequest(statement) );
            return item;
        }
        catch (Exception e) {
            throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public User update(User user) {
        String updateQuery = sqlQueryStrings.updateUsertByIdString(
                usersTableName, user.getId().toString(), user);

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {

            int affectedRows = jdbcConnection.executeUpdate(updateQuery);

            if (affectedRows == 0) {
                logger.error(String.format("Repository: update: error: User with id %s not found", user.getId()));
                throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
            }
            return user;

        } catch (SQLException e) {
            logger.error(String.format("Repository: update: error: %s", e.getMessage()));
            throw new RuntimeException("Database connection error", e);
        } catch (Exception e) {
            logger.error(String.format("Repository: update: error: %s", e.getMessage()));
            throw new RuntimeException("Unexpected error", e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        String queryString = sqlQueryStrings.deleteByIdString(usersTableName, id.toString());

        try (JdbcConnection jdbcConnection = new JdbcConnection()) {
            int affectedRows = jdbcConnection.executeUpdate(queryString);
            return affectedRows > 0;
        }
        catch (Exception e) {
            throw new CompletionException(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, e);
        }
    }
}
