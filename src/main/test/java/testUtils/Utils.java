package testUtils;

import configurations.PropertiesConfiguration;
import models.entities.User;
import models.enums.UserRoles;

import java.util.Date;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class Utils {
    public static final String usersSchema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    public static final String usersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");

    public static User testUser1 = new User(UUID.randomUUID(),
            "testUser1",
            "password",
            "testUser1@gmail.com",
            "firstName",
            "lastName",
            "phoneNumber",
            UserRoles.ADMIN,
            null,
            new Date(),
            new Date(),
            new Date());

}
