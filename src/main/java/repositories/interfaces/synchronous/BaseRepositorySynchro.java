package repositories.interfaces.synchronous;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.BaseRepository;
import utils.StaticConstants;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Базовый интерфейс CRUD-операций для работы с репозиторием с синхронным доступом
 *
 * @param <T> тип сущности
 * @author 4ndr33w
 * @version 1.0
 */
public interface BaseRepositorySynchro<T> {
    Optional<T> findById(UUID id);
    Optional<List<T>> findAll();
    T create(T item);
    T update(T item);
    boolean delete(UUID id);

    default UUID getGeneratedKeyFromRequest(Statement statement) throws SQLException {
        var set = statement.getGeneratedKeys();

        if(set.next()) {
            return (UUID) set.getObject(1);
        }
        else {
            Logger logger = LoggerFactory.getLogger(BaseRepository.class);
            logger.error(StaticConstants.FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE);
            throw new SQLException(StaticConstants.FAILED_TO_RETRIEVE_GENERATED_KEYS_EXCEPTION_MESSAGE);
        }
    }
}
