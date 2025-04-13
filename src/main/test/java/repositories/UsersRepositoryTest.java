package repositories;

import configurations.JdbcConnection;
import jdk.jshell.execution.Util;
import models.entities.User;
import models.enums.UserRoles;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import repositories.interfaces.UserRepository;
import testUtils.Utils;
import utils.mappers.ProjectMapper;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.mockito.Mockito.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author 4ndr33w
 * @version 1.0
 */
//@ExtendWith(MockitoExtension.class)
public class UsersRepositoryTest {

    @Mock
    private JdbcConnection jdbcConnection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet generatedKeys;

    //@Mock
    private SqlQueryStrings sqlQueryStrings = new SqlQueryStrings();

    @InjectMocks
    private UsersRepository usersRepository;


    @Test
    public void getByIdTest() throws ExecutionException, InterruptedException, SQLException {
        usersRepository = new UsersRepository ();
        UUID id = UUID.fromString("7f1111e0-8020-4de6-b15a-601d6903b9eb");
        var result =  usersRepository.findByIdAsync(id)
                .thenApplyAsync(UserMapper::toDto)
                .exceptionally(ex -> {
                    //System.err.println("Error fetching users: " + ex.getMessage());
                    return null; // Fallback
                }).get();

        var project = result;
        assertEquals("login", result.getUserName());

    }

/*
    @Test
    public void createAsync_ShouldReturnUserWithId_WhenCreationSuccessful() throws Exception {

        User testUser = Utils.testUser1;
        UUID expectedId = UUID.randomUUID();
        String expectedQuery = "INSERT INTO...";

        //when(sqlQueryStrings.createUserString(anyString(), any(User.class)))
          //      .thenReturn(expectedQuery);
        when(jdbcConnection.statement()).thenReturn(statement);
        when(statement.executeUpdate(sqlQueryStrings.createUserString(Utils.usersSchema + "." + Utils.usersTable, testUser), Statement.RETURN_GENERATED_KEYS))
                .thenReturn(1);
        when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getObject(1)).thenReturn(expectedId);

        // Act
        CompletableFuture<User> future = usersRepository.createAsync(testUser);
        User result = future.get(); // Блокируем для проверки

        // Assert
        assertNotNull(result);
        assertEquals(expectedId, result.getId());
        assertEquals("testUser1", result.getUserName());
        verify(statement).close();
        verify(generatedKeys).close();
    }*/
/*
    @Test
    void createAsync_ShouldThrow_WhenUserIsNull() {
        // Act & Assert
        CompletableFuture<User> future = usersRepository.createAsync(null);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("User item cannot be null", exception.getCause().getMessage());
    }

    @Test
    void createAsync_ShouldThrow_WhenNoRowsAffected() throws Exception {
        // Arrange
        User testUser = new User("testUser", "test@example.com");

        when(sqlQueryStrings.createUserString(anyString(), any(User.class)))
                .thenReturn("INSERT...");
        when(jdbcConnection.statement()).thenReturn(statement);
        when(statement.executeUpdate(anyString(), anyInt()))
                .thenReturn(0);

        // Act & Assert
        CompletableFuture<User> future = usersRepository.createAsync(testUser);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Failed to create user, no rows affected", exception.getCause().getMessage());
    }

    @Test
    void createAsync_ShouldThrow_WhenDatabaseError() throws Exception {
        // Arrange
        User testUser = new User("testUser", "test@example.com");
        SQLException expectedException = new SQLException("DB error");

        when(sqlQueryStrings.createUserString(anyString(), any(User.class)))
                .thenReturn("INSERT...");
        when(jdbcConnection.statement()).thenReturn(statement);
        when(statement.executeUpdate(anyString(), anyInt()))
                .thenThrow(expectedException);

        // Act & Assert
        CompletableFuture<User> future = usersRepository.createAsync(testUser);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertSame(expectedException, exception.getCause().getCause());
    }
*/

    @Test
    public void findAllAsyncTest() throws SQLException {
        UserRepository userRepository = new UsersRepository();

        try {
            var result = userRepository.findAllAsync()
                    .thenApply(userList -> userList
                            .stream()
                            .map(UserMapper::toDto)
                            .toList())
                    .join();

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


            var result = userRepository.createAsync(Utils.testUser1);

            var resultUser = result.get();//.get();
            assertNotNull(result);
            var id = resultUser.getId();

            assertEquals("login", resultUser.getUserName());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
/*
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
    }*/
}
