package repositories;

import models.entities.User;
import models.enums.UserRoles;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersRepositoryTest {

    @Test
    public void findAllTest() {

        try {
            UsersRepository userRepository = new UsersRepository();
            var result = userRepository.findAll();

            assertNotNull(result);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void createTest() {
        try {
            UsersRepository userRepository = new UsersRepository();

            User user = new User("login", "pass", "email@email.com", "Andr33w", "McFly", "0721000000", UserRoles.USER, null, new Date(), new Date(), new Date());


            var result = userRepository.create(user);

            var resultUser = result;//.get();
            assertNotNull(result);
            var id = resultUser.get().getId();

            assertEquals("login", resultUser.get().getUserName());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void deleteByInvalidIdTest() {
        UUID id = UUID.fromString("1802344e-0513-4e57-9099-3f7764412655");

        try {
            UsersRepository userRepository = new UsersRepository();
            var result = userRepository.delete(id);

            assertFalse(result);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    @Test
    public void deleteCorrectTest() {
        UUID id = UUID.fromString("5028cb73-dccb-5cd2-8534-5340b5f53b53");

        try {
            UsersRepository userRepository = new UsersRepository();
            var result = userRepository.delete(id);

            assertTrue(result);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
