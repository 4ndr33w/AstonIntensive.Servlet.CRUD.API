package testUtils;

import configurations.PropertiesConfiguration;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import models.enums.ProjectStatus;
import models.enums.UserRoles;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class Utils {
    public static final String usersSchema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    public static final String usersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");

    public static User testUser1 = new User(UUID.randomUUID(),
            "testUser4",
            "password",
            "testUser4@gmail.com",
            "firstName",
            "lastName",
            "phoneNumber",
            UserRoles.ADMIN,
            null,
            new Date(),
            new Date(),
            new Date());

    public static Project testProject1 = new Project(
            UUID.randomUUID(),
            "testProject1",
            "description1",
            new Date(),
            new Date(),
            null,
            UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"),
            ProjectStatus.ACTIVE,
            List.of());
}
