package utils.sqls;

import models.dtos.UserDto;
import models.entities.Project;
import models.entities.User;
import models.enums.ProjectStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class SqlQueryStrings {

    public String findAllQueryString(String tableName){
        return String.format("SELECT * FROM %s", tableName);
    }

    public String createUserString(String tableName, User user) {

        StringBuilder query = new StringBuilder();
        query.append(String.format("INSERT INTO %s", tableName));
        query.append(" (user_name, first_name, last_name, email, password, phone, created_at, updated_at, image, last_login_date, userstatus) ");
        query.append(String.format(" VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', %s);",
                user.getUserName(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                Arrays.toString(user.getUserImage()),
                user.getLastLoginDate(),
                user.getUserRole().ordinal()));

        return query.toString();
    }

    public String createProjectString(String tableName, Project project) {

        StringBuilder query = new StringBuilder();
        query.append(String.format("INSERT INTO %s", tableName));
        query.append(" (name, description, created_at, updated_at, image, admin_id, project_status) ");
        query.append(String.format(" VALUES ('%s', '%s', '%s', '%s', '%s', '%s', %s);",
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                Arrays.toString(project.getImage()),
                project.getAdminId(),
                project.getProjectStatus().ordinal()));

        return query.toString();
    }

    public String deleteById(String tableName, String id){
        return String.format("DELETE FROM %s WHERE id = '%s';", tableName, id);
    }

    public String findById(String tableName, String id){
        return String.format("SELECT * FROM %s WHERE id = '%s';", tableName, id);
    }
}
