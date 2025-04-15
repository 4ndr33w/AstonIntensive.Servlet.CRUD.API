package testUtils;

import configurations.PropertiesConfiguration;
import models.dtos.ProjectUsersDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import models.enums.ProjectStatus;
import models.enums.UserRoles;
import utils.mappers.UserMapper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public abstract class Utils {
    public static final String usersSchema = PropertiesConfiguration.getProperties().getProperty("jdbc.default-schema");
    public static final String usersTable = PropertiesConfiguration.getProperties().getProperty("jdbc.users-table");

    public UUID userId = UUID.randomUUID();
    public UUID projectId1 = UUID.randomUUID();
    public UUID projectId2 = UUID.randomUUID();
    public UUID user1Id = UUID.randomUUID();
    public UUID user2Id = UUID.randomUUID();

    public ProjectUsersDto dto1 = new ProjectUsersDto(projectId1, user1Id);
    public ProjectUsersDto dto2 = new ProjectUsersDto(projectId1, user2Id);
    public ProjectUsersDto dto3 = new ProjectUsersDto(projectId2, user1Id);

    public UserDto userDto1 = UserMapper.toDto(testUser1);
    public UserDto userDto2 = UserMapper.toDto(testUser2);

    public List<Project> projects = Arrays.asList(testProject1, testProject1);


    public static User testUser1 = new User(UUID.fromString("762bdaf1-d6ea-4b8d-ac7d-2c0ff5d0ecc9"),
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

    public static User testUser2 = new User(UUID.fromString("7f1111e0-8020-4de6-b15a-601d6903b9eb"),
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

    public static User testUser3 = new User(UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"),
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

    public static Project testProject1 = new Project(
            UUID.fromString("ada6fe61-f4d4-46ba-9739-e59304705f20"),
            "PostmanProject1",
            "description1",
            new Date(),
            new Date(),
            null,
            UUID.fromString("d6df284c-b302-48c2-84d0-fd6d61c75f38"),
            ProjectStatus.ACTIVE,
            List.of());

    public static Project testProject2 = new Project(
            UUID.fromString("9658455a-348b-4d4d-ad08-cb562da4f8c4"),
            "testProject2",
            "description3",
            new Date(),
            new Date(),
            null,
            UUID.fromString("7f1111e0-8020-4de6-b15a-601d6903b9eb"),
            ProjectStatus.ACTIVE,
            List.of());

    public static Project testProject3 = new Project(
            UUID.fromString("e79aaeeb-acfe-4156-8865-d5cbafdc0a69"),
            "testProject3",
            "description3",
            new Date(),
            new Date(),
            null,
            UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"),
            ProjectStatus.ACTIVE,
            List.of());

    /*public ProjectUsersDto testProjectUserDto1 = new ProjectUsersDto(testUser1.getId(), testProject1.getId());
    public ProjectUsersDto testProjectUserDto2 = new ProjectUsersDto(testUser2.getId(), testProject2.getId());
    public ProjectUsersDto testProjectUserDto3 = new ProjectUsersDto(testUser3.getId(), testProject3.getId());*/

    public ProjectUsersDto testProjectUserDto1 = new ProjectUsersDto(UUID.fromString("41096054-cbd7-4308-8411-905ae6f03aa6"), UUID.fromString("9658455a-348b-4d4d-ad08-cb562da4f8c4"));
    public ProjectUsersDto testProjectUserDto2 = new ProjectUsersDto(UUID.fromString("d6df284c-b302-48c2-84d0-fd6d61c75f38"), UUID.fromString("9658455a-348b-4d4d-ad08-cb562da4f8c4"));
    public ProjectUsersDto testProjectUserDto3 = new ProjectUsersDto(UUID.fromString("762bdaf1-d6ea-4b8d-ac7d-2c0ff5d0ecc9"), UUID.fromString("ada6fe61-f4d4-46ba-9739-e59304705f20"));
}
