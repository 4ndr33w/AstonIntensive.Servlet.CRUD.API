package utils.mappers;

import models.entities.Project;
import models.entities.User;

import java.sql.SQLException;

import static utils.mappers.ProjectMapper.mapResultSetToProject;
import static utils.mappers.UserMapper.mapResultSetToUser;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ResultSetHandler<T> {

    public static <T> T mapResultSetToEntity(java.sql.ResultSet rs, Class<T> clazz) throws SQLException {
        if (clazz.equals(Project.class)) {
            return (T) mapResultSetToProject(rs);
        }
        if (clazz.equals(User.class)) {
            return (T) mapResultSetToUser(rs);
        }
        return null;
    }
}
