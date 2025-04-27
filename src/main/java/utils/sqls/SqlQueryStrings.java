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


    public String selectUserIdsFromProjectUsersTableByProjectId(String tableName, String projectId) {
        return String.format(
                "SELECT user_id FROM %s WHERE project_id = '%s'",  tableName, projectId);
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
}
