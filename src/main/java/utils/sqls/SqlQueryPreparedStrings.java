package utils.sqls;

import models.entities.Project;

import java.util.Arrays;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class SqlQueryPreparedStrings {

    public String findAllQueryString(String tableName){
        return "SELECT * FROM %s".formatted(tableName);
    }

    public String createUserPreparedQueryString(String tableName) {

        StringBuilder query = new StringBuilder();

        String queryString = "INSERT INTO %s (user_name, first_name, last_name, email, password, phone, updated_at, image, last_login_date)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)".formatted(tableName).replace(";", " ");

        query.append("INSERT INTO %s ".formatted(tableName));
        query.append("(user_name, first_name, last_name, email, password, phone, updated_at, image, last_login_date) ");
        query.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");

        return queryString;
    }

    public String deleteByIdString(String tableName){
        return String.format("DELETE FROM %s WHERE id = ?::uuid;", tableName);
    }

    public String findByIdString(String tableName){
        return String.format("SELECT * FROM %s WHERE id = ?::uuid", tableName);
    }

    public String updateUsertByIdString(String tableName) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("UPDATE %s SET ", tableName));
        query.append("first_name = ?, ");
        query.append("last_name = ?, ");
        query.append("phone = ?, ");
        query.append("updated_at = ?, ");
        query.append("image = ?, ");
        query.append("last_login_date = ? ");
        query.append("WHERE id = ?::uuid;");

        return query.toString();
    }

    public String createProjectString(String tableName) {

        StringBuilder query = new StringBuilder();
        query.append(String.format("INSERT INTO %s", tableName));
        query.append(" (name, description, created_at, updated_at, image, admin_id, project_status) ");
        query.append(" VALUES (?, ?, ?, ?, ?, ?::uuid, ?);");

        return query.toString();
    }

    public String findProjectsByAdminIdString(String tableName){

        return String.format("SELECT * FROM %s WHERE admin_id = ?::uuid;", tableName);
    }
}
