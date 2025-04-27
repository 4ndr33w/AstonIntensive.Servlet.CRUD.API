package utils.sqls;

import models.entities.Project;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    public String findProjectsByUserIdString(String tableName){
        return String.format("SELECT * FROM %s WHERE user_id = ?::uuid;", tableName);
    }

    public String findAllByIdsString(String tableName, int idCollectionSize) {

        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT * FROM %s WHERE id IN ( ", tableName));
        for (int i = 0; i < idCollectionSize; i++) {
            if (i == idCollectionSize - 1) {
                query.append("?::uuid);");
            } else {
                query.append("?::uuid, ");
            }
        }
        return query.toString();
    }

    public String findProjectsByAdminsIdsString(String tableName, int idCollectionSize) {

        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT * FROM %s WHERE admin_id IN ( ", tableName));
        for (int i = 0; i < idCollectionSize; i++) {
            if (i == idCollectionSize - 1) {
                query.append("?::uuid);");
            } else {
                query.append("?::uuid, ");
            }
        }
        return query.toString();
    }

    public String findProjectUsersByUserId(String tableName){
        return String.format("SELECT * FROM %s WHERE user_id = ?::uuid;", tableName);
    }
    public String findProjectUsersByProjectId(String tableName){
        return String.format("SELECT * FROM %s WHERE project_id = ?::uuid;", tableName);
    }

    public String removeProjectUser(String tableName) {

        return String.format("DELETE FROM %s WHERE project_id = ?::uuid AND user_id = ?::uuid;", tableName);
    }

    public String addProjectUser(String tableName) {

        return String.format("INSERT INTO %s (project_id, user_id) VALUES (?::uuid, ?::uuid);", tableName);
    }

    public String findProjectUsersByProjectIds(String tableName, int idCollectionSize) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT project_id, user_id FROM %s WHERE project_id IN ( ", tableName));
        for (int i = 0; i < idCollectionSize; i++) {
            if (i == idCollectionSize - 1) {
                query.append("?::uuid);");
            } else {
                query.append("?::uuid, ");
            }
        }
        return query.toString();
    }

    public String findProjectUsersByUserIds(String tableName, int idCollectionSize) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("SELECT project_id, user_id FROM %s WHERE user_id IN ( ", tableName));
        for (int i = 0; i < idCollectionSize; i++) {
            if (i == idCollectionSize - 1) {
                query.append("?::uuid);");
            } else {
                query.append("?::uuid, ");
            }
        }
        return query.toString();
    }

    public String updateProjectByIdString(String tableName) {
        StringBuilder query = new StringBuilder();
        query.append(String.format("UPDATE %s SET ", tableName));
        query.append("name = ?, ");
        query.append("description = ?, ");
        query.append("updated_at = ?, ");
        query.append("image = ?, ");
        query.append("project_status = ? ");
        query.append("WHERE id = ?::uuid;");

        return query.toString();
    }
}
