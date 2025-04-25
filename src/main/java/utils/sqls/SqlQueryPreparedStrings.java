package utils.sqls;

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
}
