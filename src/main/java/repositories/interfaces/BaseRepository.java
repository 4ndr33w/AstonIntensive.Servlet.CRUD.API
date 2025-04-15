package repositories.interfaces;

import models.entities.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    CompletableFuture<List<T>> findAllAsync();
    CompletableFuture<T> createAsync(T item);
    CompletableFuture<T> updateAsync(T item);
    CompletableFuture<Boolean> deleteAsync(UUID id);
}
