package utils;

import models.entities.User;
import models.enums.UserRoles;

import java.util.Date;
import java.util.UUID;

/**
 * Утильный класс
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class Utils {

    public static User testUser1 = new User(
            UUID.fromString("762bdaf1-d6ea-4b8d-ac7d-2c0ff5d0ecc9"),
            "johndoe",
            "password",
            "john.doe@example.com",
            "John",
            "Doe",
            "phoneNumber",
            UserRoles.USER,
            null,
            new Date(),
            new Date(),
            new Date());

    public static User testUser2 = new User(
            UUID.fromString("7f1111e0-8020-4de6-b15a-601d6903b9eb"),
            "login",
            "password",
            "email@email.com",
            "firstName",
            "lastName",
            "phoneNumber",
            UserRoles.ADMIN,
            null,
            new Date(),
            new Date(),
            new Date());

    public static User testUser3 = new User(
            UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"),
            "login2",
            "password",
            "email2@email.com",
            "firstName",
            "lastName",
            "phoneNumber",
            UserRoles.ADMIN,
            null,
            new Date(),
            new Date(),
            new Date());

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
        } catch (Exception e) {
            return false;
        }
    }
}
