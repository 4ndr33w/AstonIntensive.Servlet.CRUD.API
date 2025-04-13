package utils.sqls;

import models.entities.Project;
import models.entities.User;

import java.util.Arrays;

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

    public String deleteByIdString(String tableName, String id){
        return String.format("DELETE FROM %s WHERE id = '%s';", tableName, id);
    }

    public String findByIdString(String tableName, String id){
        return String.format("SELECT * FROM %s WHERE id = '%s';", tableName, id);
    }

    public String findProjectsByAdminIdString(String tableName, String adminId){
        if (adminId != null) {
            return String.format("SELECT * FROM %s WHERE admin_id = '%s';", tableName, adminId);
        } else {
            return "";
        }
    }

    public String addUserIntoProjectString(String tableName, String projectId, String userId) {
        if (projectId != null && userId != null) {
            return String.format("INSERT INTO %s (project_id, user_id) VALUES ('%s', '%s');", tableName, projectId, userId);
        } else {
            return "";
        }
    }

    public String selectUserIdsFromProjectUsersTableByProjectId(String tableName, String projectId) {
        return String.format(
                "SELECT user_id FROM %s WHERE project_id = '%s'",  tableName, projectId);
    }

    public String findAllProjectsByUserId(String projectsTable, String projectUsersTable, String userId) {
        String query = String.format(
                "SELECT proj.* FROM %s proj JOIN %s proj_users ON proj.id = proj_users.project_id WHERE proj_users.user_id = '%s'",
                projectsTable, projectUsersTable, userId);
        return query;
    }
    public String removeUserFromProjectString(String tableName, String projectId, String userId) {
        if (projectId != null && userId != null) {
            return String.format("DELETE FROM %s WHERE project_id = '%s' AND user_id = '%s';", tableName, projectId, userId);
        } else {
            return "";
        }
    }
}
