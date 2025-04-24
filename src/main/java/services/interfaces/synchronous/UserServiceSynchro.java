package services.interfaces.synchronous;

import models.entities.User;

import java.sql.SQLException;
import java.util.List;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public interface UserServiceSynchro extends BaseServiceSynchro<User> {


    List<User> getAll() throws SQLException;
}
