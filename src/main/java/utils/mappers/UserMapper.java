package utils.mappers;

import models.dtos.UserDto;
import models.entities.User;
import models.enums.UserRoles;
import utils.StaticConstants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Утильный класс для маппинга пользователей
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class UserMapper {

    public static UserDto toDto(User user){

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setUserImage(user.getUserImage());
        userDto.setUserName(user.getUserName());
        userDto.setUserRole(user.getUserRole());
        userDto.setCreatedAt(user.getCreatedAt());

        if(user.getProjects() != null) {
            userDto.setProjects(user.getProjects());
        }
        else {
            userDto.setProjects(new ArrayList<>());
        }
        return userDto;

    }

    public static User mapToEntity(UserDto userDto){
        User user = new User();
        user.setId(userDto.getId());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setUserImage(userDto.getUserImage());
        user.setUserName(userDto.getUserName());
        user.setUserRole(userDto.getUserRole());
        user.setCreatedAt(userDto.getCreatedAt());

        if(userDto.getProjects() != null) {
            user.setProjects(userDto.getProjects());
        }
        else {
            user.setProjects(new ArrayList<>());
        }
        return user;
    }

    public static User mapResultSetToUser(ResultSet rs) throws SQLException {

        if(rs == null){
            throw new NullPointerException(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE);
        }
        try {
            User user = new User(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("user_name"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("phone"),
                    UserRoles.values()[Integer.parseInt(rs.getString("userstatus"))],
                    rs.getBytes("image"),
                    rs.getTimestamp("created_at") != null ?
                            new Date(rs
                                    .getTimestamp("updated_at")
                                    .getTime()) : new Date(),

                    rs.getTimestamp("updated_at") != null ?
                            new Date(rs
                                    .getTimestamp("updated_at")
                                    .getTime()) : new Date(),

                    rs.getTimestamp("last_login_date") != null ?
                            new Date(rs
                                    .getTimestamp("last_login_date")
                                    .getTime()) : new Date()
            );

            return user;
        }
        catch (SQLException ex){
            throw new SQLException(StaticConstants.ERROR_FETCHING_RESULT_SET_METADATA_EXCEPTION_MESSAGE, ex.getMessage());

        }
    }
}
