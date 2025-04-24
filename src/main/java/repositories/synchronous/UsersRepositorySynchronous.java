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
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.ResultSetMappingException;
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
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public Optional<List<User>> findAllByIds(List<UUID> userIds) {
        List<String> ids = userIds.stream().map(UUID::toString).toList();
        String sql = sqlQueryStrings.findAllByIdsString(usersTableName, ids);

        try (JdbcConnection jdbcConnection = new JdbcConnection();
             PreparedStatement statement = jdbcConnection.getConnection().prepareStatement(sql)) {

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
    public Optional<List<User>> findAll() throws DatabaseOperationException, UserNotFoundException, SQLException{
        String queryString = sqlQueryStrings.findAllQueryString(usersTableName);

        var result = this.retrieveMultipleEntities(queryString);
        if(result.isEmpty()) {
            throw new UserNotFoundException(StaticConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE);
        }
        return result;
    }

    @Override
    public User create(User item) throws SQLException, ResultSetMappingException, NullPointerException, DatabaseOperationException {
        String queryString = sqlQueryStrings.createUserString(usersTableName, item);

        Optional<UUID> newUserId = createEntity(queryString);

        if(newUserId.isPresent()) {

            var userId = newUserId.get();
            item.setId(userId);
            return item;
        }
        else {
            throw new DatabaseOperationException("Failed to create new user");
        }
    }

    @Override
    public User update(User user) throws SQLException {
        String queryString = sqlQueryStrings.updateUsertByIdString(
                usersTableName, user.getId().toString(), user);
        var result = executeUpdate(queryString);

        if(result > 0) {
            return user;
        }
        else {
            throw new DatabaseOperationException(StaticConstants.DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public boolean delete(UUID id) throws SQLException {
        String queryString = sqlQueryStrings.deleteByIdString(usersTableName, id.toString());

        return executeUpdate(queryString) > 0;
    }
}
