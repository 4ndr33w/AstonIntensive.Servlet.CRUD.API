package utils.sqls;

import models.entities.Project;
import models.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servlets.ProjectsServlet;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Утильный класс для получения SQL-запросов при указанных параметрах.
 *
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

    public String updateProjectByIdString(String tableName, String id, Project project) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("UPDATE %s SET ", tableName));
        query.append(String.format("name = '%s', ", project.getName()));
        query.append(String.format("description = '%s', ", project.getDescription()));
        query.append(String.format("updated_at = '%s', ", project.getUpdatedAt()));
        query.append(String.format("image = '%s', ", Arrays.toString(project.getImage())));
        query.append(String.format("project_status = '%s' ", project.getProjectStatus().ordinal()));
        query.append(String.format("WHERE id = '%s';", id));

        return query.toString();
    }

    public String updateUsertByIdString(String tableName, String id, User user) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("UPDATE %s SET ", tableName));
        query.append(String.format("first_name = '%s', ", user.getFirstName()));
        query.append(String.format("last_name = '%s', ", user.getLastName()));
        query.append(String.format("phone = '%s', ", user.getPhoneNumber()));
        query.append(String.format("updated_at = '%s', ", new Date()));
        query.append(String.format("image = '%s', ", Arrays.toString(user.getUserImage())));
        query.append(String.format("last_login_date = '%s' ", new Date()));
        query.append(String.format("WHERE id = '%s';", id));

        Logger logger = LoggerFactory.getLogger(SqlQueryStrings.class);

        logger.info("Query for updating user: " + query.toString());
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

    public String findProjectUserByUserIdString(String tableName, String id){
        return String.format("SELECT * FROM %s WHERE user_id = '%s';", tableName, id);
    }
    public String findProjectUserByProjectIdString(String tableName, String id){
        return String.format("SELECT * FROM %s WHERE project_id = '%s';", tableName, id);
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

    /**
     * SQL-запрос на удаление из таблицы {@code tableName}
     * с заданными идентификаторами {@code projectId} и {@code userId}.
     * <p>
     *     Для операции удаления пользователя из участников проекта
     * </p>
     *
     * @param tableName
     * @param projectId
     * @param userId
     * @return {@code String}
     */
    public String removeUserFromProjectString(String tableName, String projectId, String userId) {
        if (projectId != null && userId != null) {
            return String.format("DELETE FROM %s WHERE project_id = '%s' AND user_id = '%s';", tableName, projectId, userId);
        } else {
            return "";
        }
    }

    /**
     * SQL-запрос на поиск в таблице {@code tableName} всех записей
     * с заданными идентификаторами {@code List<String> ids}.
     *
     * @param tableName
     * @param ids
     * @return {@code String}
     */
    public String findAllByIdsString(String tableName, List<String> ids) {

        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT * FROM %s WHERE id IN ( ", tableName));
        for (int i = 0; i < ids.size(); i++) {
            if (i == ids.size() - 1) {
                query.append(String.format("'%s');", ids.get(i)));
            } else {
                query.append(String.format("'%s', ", ids.get(i)));
            }
        }
            String sql = query.toString();

        return sql;
    }

    public String findAllProjectsByAdminIds(String tableName, List<String> ids) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT * FROM %s WHERE admin_id IN ( ", tableName));
        for (int i = 0; i < ids.size(); i++) {
            if (i == ids.size() - 1) {
                query.append(String.format("'%s');", ids.get(i)));
            } else {
                query.append(String.format("'%s', ", ids.get(i)));
            }
        }
        String sql = query.toString();

        return sql;
    }

    public String findProjectUsersByUserIds(String tableName, List<String> ids) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT * FROM %s WHERE user_id IN ( ", tableName));
        for (int i = 0; i < ids.size(); i++) {
            if (i == ids.size() - 1) {
                query.append(String.format("'%s');", ids.get(i)));
            } else {
                query.append(String.format("'%s', ", ids.get(i)));
            }
        }
        String sql = query.toString();

        return sql;
    }

    public String findProjectUsersByProjectIds(String tableName, List<String> ids) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT * FROM %s WHERE project_id IN ( ", tableName));
        for (int i = 0; i < ids.size(); i++) {
            if (i == ids.size() - 1) {
                query.append(String.format("'%s');", ids.get(i)));
            } else {
                query.append(String.format("'%s', ", ids.get(i)));
            }
        }
        String sql = query.toString();

        return sql;
    }
}
