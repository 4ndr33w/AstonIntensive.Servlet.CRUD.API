package services.synchronous;

import org.junit.Test;
import repositories.interfaces.synchronous.UserRepositorySynchro;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersServiceTest {

    UserRepositorySynchro usersRepository;

    services.interfaces.synchronous.UserServiceSynchro usersService;

    @Test
    public void GetUsersTest() {
        usersRepository = new repositories.synchronous.UsersRepositorySynchronous();

        List<UUID> list = List.of(UUID.fromString("070fe642-8c78-48df-b352-c9abca157517"), UUID.fromString("234a06d9-7df5-4fa1-8d40-acb1558479f5"));
        var result = usersRepository.findAllByIds(list);

        var test = result.get();

        assertNotNull(test);
    }

    @Test
    public void GetUsers1Test() throws SQLException {

        usersRepository = new repositories.synchronous.UsersRepositorySynchronous();

        //var users = usersRepository.findAll();
        usersService = new services.synchronous.UsersService();

        var result = usersService.getAll();

        var test = result;

        assertNotNull(test);
    }
}
