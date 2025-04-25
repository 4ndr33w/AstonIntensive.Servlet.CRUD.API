package repositories.interfaces;

import models.entities.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.ProjectRepositoryNew;
import utils.StaticConstants;
import utils.exceptions.DatabaseOperationException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static utils.mappers.ProjectMapper.mapResultSetToProject;

/**
 * Generic интерфейс для CRUD операций
 * используя JDBC
 * @param <T> любой класс наследник Object
 *
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseRepository<T> {

    CompletableFuture<T> findByIdAsync(UUID id);
    CompletableFuture<List<T>> findAllAsync() throws SQLException;
    CompletableFuture<T> createAsync(T item);
    CompletableFuture<T> updateAsync(T item);
    CompletableFuture<Boolean> deleteAsync(UUID id) throws SQLException;

    default UUID getGeneratedKeyFromRequest(Statement statement) throws SQLException {
        var set = statement.getGeneratedKeys();

        if(set.next()) {
            return (UUID) set.getObject(1);
        }
        else {
            Logger logger = LoggerFactory.getLogger(BaseRepository.class);
            logger.error(StaticConstants.FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE);
            throw new DatabaseOperationException(StaticConstants.FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE);
        }
    }
}
