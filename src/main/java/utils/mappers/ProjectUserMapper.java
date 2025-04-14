package utils.mappers;

import models.dtos.ProjectUsersDto;
import utils.StaticConstants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectUserMapper {

    public static ProjectUsersDto mapResultSetToProjectUser(ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }

        try {
            ProjectUsersDto projectUser = new ProjectUsersDto(
                    UUID.fromString(rs.getString("user_id")),
                    UUID.fromString(rs.getString("project_id"))
            );
            return projectUser;
        }
        catch (SQLException ex){
            throw new SQLException(StaticConstants.ERROR_FETCHING_RESULT_SET_METADATA_EXCEPTION_MESSAGE, ex.getMessage());
        }
    }
}
