package repositories;

import configurations.JdbcConnection;
import configurations.ThreadPoolConfiguration;
import models.entities.User;
import models.enums.UserRoles;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import repositories.interfaces.UserRepository;
import testUtils.Utils;
import utils.StaticConstants;
import utils.mappers.UserMapper;
import utils.sqls.SqlQueryStrings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author 4ndr33w
 * @version 1.0
 */
//@ExtendWith(MockitoExtension.class)
public class UsersRepositoryImplementationTest {

    private static Executor dbExecutor;
    private ResultSet mockResultSet;

    @Mock
    private JdbcConnection jdbcConnection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet generatedKeys;

    static {
        dbExecutor = ThreadPoolConfiguration.getDbExecutor();
    }

    @InjectMocks
    private UsersRepositoryImplementation usersRepository;

    @BeforeEach
    void setUp() throws SQLException {
        dbExecutor = Runnable::run; // Для тестов используем синхронное выполнение
        //UsersRepositoryImplementation repository = new UsersRepositoryImplementation();

        // Мок ResultSet для имитации данных из БД
        mockResultSet = mock(ResultSet.class);
        when(mockResultSet.next())
                .thenReturn(true)  // Первая запись
                .thenReturn(true)  // Вторая запись
                .thenReturn(false); // Конец данных
    }


    //@Mock
    private SqlQueryStrings sqlQueryStrings = new SqlQueryStrings();

    /*
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
     */

    @Test
    public void findAllAsync_ShouldReturnUsers_WhenDatabaseHasData() throws Exception {
        // Настройка моков для успешного сценария
        try (MockedStatic<JdbcConnection> mockedJdbc = Mockito.mockStatic(JdbcConnection.class)) {
            JdbcConnection mockConn = mock(JdbcConnection.class);
            mockedJdbc.when(JdbcConnection::new).thenReturn(mockConn);

            when(mockConn.executeQuery(anyString())).thenReturn(mockResultSet);

            // Имитация данных пользователя
            when(mockResultSet.getString("user_name")).thenReturn("user1").thenReturn("user2");
            when(mockResultSet.getString("email")).thenReturn("test1@test.com").thenReturn("test2@test.com");

            // Вызов тестируемого метода
            CompletableFuture<List<User>> future = usersRepository.findAllAsync();
            List<User> users = future.get();

            // Проверки
            assertEquals(2, users.size());
            assertEquals("user1", users.get(0).getId());
            assertEquals("test2@test.com", users.get(1).getEmail());
        }
    }
    @Test
    public void findAllAsync_ShouldThrowCompletionException_WhenDatabaseError() {
        // Для работы с final-методами
        System.setProperty("mockito.mockMakerClass", "org.mockito.internal.creation.bytebuddy.InlineByteBuddyMockMaker");

        try (MockedStatic<JdbcConnection> mockedJdbc = Mockito.mockStatic(JdbcConnection.class)) {
            JdbcConnection mockConn = mock(JdbcConnection.class);
            mockedJdbc.when(JdbcConnection::new).thenReturn(mockConn);

            // Вариант для final-методов
            doThrow(new SQLException("DB error"))
                    .when(mockConn)
                    .executeQuery(sqlQueryStrings.findAllQueryString("servlets.users"));

            CompletableFuture<List<User>> future = usersRepository.findAllAsync();

            CompletionException exception = assertThrows(
                    CompletionException.class,
                    future::join
            );

            assertEquals("Database access error", exception.getMessage());
            assertTrue(exception.getCause() instanceof SQLException);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void findAllAsync_ShouldUseCorrectTableName() throws Exception {
        try (MockedStatic<JdbcConnection> mockedJdbc = Mockito.mockStatic(JdbcConnection.class)) {
            JdbcConnection mockConn = mock(JdbcConnection.class);
            mockedJdbc.when(JdbcConnection::new).thenReturn(mockConn);

            when(mockConn.executeQuery(anyString())).thenReturn(mockResultSet);

            usersRepository.findAllAsync().get();

            verify(mockConn).executeQuery("SELECT * FROM servlets.users"); // Проверяем правильность SQL-запроса
        }
    }

    @Test
    public void findAllAsync_ShouldUseProvidedExecutor() {
        Executor mockExecutor = mock(Executor.class);
        usersRepository = new UsersRepositoryImplementation();

        usersRepository.findAllAsync();

        verify(mockExecutor).execute(any(Runnable.class));
    }


    @Test
    public void getByIdTest() throws ExecutionException, InterruptedException, SQLException {
        usersRepository = new UsersRepositoryImplementation();
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
        UserRepository userRepository = new UsersRepositoryImplementation();

        try {
            var result = userRepository.findAllAsync()
                    .thenApply(userList -> userList
                            .stream()
                            .map(UserMapper::toDto)
                            .toList())
                    .join();

            var test = result;
            assertNotNull(result);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void createTest() {
        try {
            UsersRepositoryImplementation userRepository = new UsersRepositoryImplementation();

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
    }
    */
}
