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

    public static User testUser1 = new User(UUID.fromString("080e9856-777e-4947-90ca-5dccdef2e7a8"),
            "testUser10",
            "password",
            "testUser10@gmail.com",
            "firstName",
            "lastName",
            "phoneNumber",
            UserRoles.ADMIN,
            null,
            new Date(),
            new Date(),
            new Date());

    public static User testUser2 = new User(UUID.fromString("7f1111e0-8020-4de6-b15a-601d6903b9eb"),
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
            UUID.fromString("d2d4b92a-f9db-4001-9c04-26b150c75310"),
            "testProject1",
            "description1",
            new Date(),
            new Date(),
            null,
            UUID.fromString("d2d4b92a-f9db-4001-9c04-26b150c75310"),
            ProjectStatus.ACTIVE,
            List.of());

    public static Project testProject2 = new Project(
            UUID.fromString("9658455a-348b-4d4d-ad08-cb562da4f8c4"),
            "testProject3",
            "description3",
            new Date(),
            new Date(),
            null,
            UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"),
            ProjectStatus.ACTIVE,
            List.of());
}
