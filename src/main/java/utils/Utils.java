package utils;

import java.util.UUID;

/**
 * Утильный класс
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class Utils {

    /**
     * Валидация {@code id}
     * <p>
     *     Метод проверяет, является ли переданный id валидным UUID
     * </p>
     *
     * @see UUID
     * @param id
     * @return {@code true} если id валидный, иначе {@code false}
     * @throws IllegalArgumentException
     */
    public boolean validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
