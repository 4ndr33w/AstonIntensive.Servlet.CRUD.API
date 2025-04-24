package repositories.interfaces.synchronous;

import com.google.common.base.Objects;
import configurations.JdbcConnection;
import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.BaseRepository;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;
import utils.exceptions.ProjectNotFoundException;

import static com.google.common.base.Predicates.instanceOf;
import static utils.mappers.ProjectMapper.mapResultSetToProject;
import static utils.mappers.ResultSetHandler.mapResultSetToEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import static utils.mappers.ProjectMapper.mapResultSetToProjectOptional;

/**
 * Базовый интерфейс CRUD-операций для работы с репозиторием с синхронным доступом
 *
 * @param <T> тип сущности
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseRepositorySynchro<T> {
    Optional<T> findById(UUID id);
    Optional<List<T>> findAll() throws SQLException;
    T create(T item) throws SQLException;
    T update(T item) throws SQLException;
    boolean delete(UUID id) throws SQLException;

    Class<T> getEntityClass();


    default UUID getGeneratedKeyFromRequest(ResultSet set) throws SQLException, DatabaseOperationException, NullPointerException{
        Logger logger = LoggerFactory.getLogger(BaseRepositorySynchro.class);
        if(set == null) {
            String message = "Base RepositorySynchro: getGeneratedKeyFromRequest, %s".formatted(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
            logger.error(message);
            logger.error(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
            throw new NullPointerException(message);
        }
        if(set.next()) {
            var id = (UUID) set.getObject(1);
            return id;
        }
        else {
            String message = "Base RepositorySynchro: getGeneratedKeyFromRequest, %s".formatted(StaticConstants.FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE);
            logger.error(message);
            throw new DatabaseOperationException(StaticConstants.FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE);
        }
    }

    default int executeUpdate(String queryString) throws DatabaseOperationException, SQLException {
        try (JdbcConnection connection = new JdbcConnection();
             Statement statement = connection.statement()) {

            var result = statement.executeUpdate(queryString);
            if (result == 0) {
                throw new DatabaseOperationException(StaticConstants.DATABASE_OPERATION_NO_ROWS_AFFECTED_EXCEPTION_MESSAGE);
            }
            return statement.executeUpdate(queryString);
        }
    }

    default Optional<T> retrieveSingleEntity(String queryString) throws DatabaseOperationException, SQLException {
        try (JdbcConnection connection = new JdbcConnection();
             Statement statement = connection.statement()) {
            var resultSet = statement.executeQuery(queryString);

            if (resultSet.next()) {
                return Optional.ofNullable(mapResultSetToEntity(resultSet, getEntityClass()));
            }
            return Optional.empty();
        }
    }

    default Optional<UUID> createEntity(String queryString) throws DatabaseOperationException, SQLException, NullPointerException {
        Logger logger = LoggerFactory.getLogger(BaseRepositorySynchro.class);

        try (JdbcConnection connection = new JdbcConnection();
             Statement statement = connection.statement()) {

            int affectedRows = statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);

            if (affectedRows == 1) {
                return Optional.of(getGeneratedKeyFromRequest(statement.getGeneratedKeys()));
            }
            else {
                String message = "Base RepositorySynchro: createEntity, %s".formatted(StaticConstants.FAILED_TO_CREATE_NEW_USER_EXCEPTION_MESSAGE);
                logger.error(message);
                throw new DatabaseOperationException(StaticConstants.FAILED_TO_CREATE_NEW_USER_EXCEPTION_MESSAGE);
            }
        }
    }

    default Optional<List<T>> retrieveMultipleEntities(String queryString) throws DatabaseOperationException, SQLException {

        List<T> entities = new ArrayList<>();
        try (JdbcConnection connection = new JdbcConnection();
             Statement statement = connection.statement();
             ResultSet resultSet = statement.executeQuery(queryString)) {
            while (resultSet.next()) {
                entities.add(mapResultSetToEntity(resultSet, getEntityClass()));
            }
            return Optional.ofNullable(entities);
        }
    }

}
