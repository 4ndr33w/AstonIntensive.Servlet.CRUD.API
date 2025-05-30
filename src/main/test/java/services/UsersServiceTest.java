package services;

import jdk.jfr.Description;
import models.dtos.UserDto;
import models.entities.User;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.junit.jupiter.MockitoSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import testUtils.Utils;
import utils.StaticConstants;
import utils.exceptions.ProjectNotFoundException;
import utils.exceptions.UserNotFoundException;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.Assert.*;


/**
 * Тестовый класс
 * Для тестирования функциональности класса UsersService
 *
 * @author 4ndr33w
 * @version 1.0
 */
/*
@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UsersServiceTest extends Utils{

    @Mock private UserRepository userRepository;
    @InjectMocks private UsersService userService;

    //---------------------------------------------------------------
    // CreateTest
    //---------------------------------------------------------------
    @Test
    @Description("Успешное создание пользователя")
    public void testCreateAsync_Success() throws SQLException {
        User testUser = Utils.testUser1;
        UserDto userDto = UserMapper.toDto(testUser);

        var anyResult = userRepository.createAsync(testUser);
        when(anyResult)
                .thenReturn(CompletableFuture.completedFuture(testUser));

        CompletableFuture<UserDto> resultFuture =  userService.createAsync(testUser);

        assertNotNull(resultFuture);
        UserDto result = resultFuture.join();
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getUserName(), result.getUserName());
        verify(userRepository).createAsync(testUser);
    }

    @Test(expected = ExecutionException.class)
    @Description("Создание пользователя возвращает ошибку SQLException")
    public void createAsync_ShouldThrow_WhenRepositoryFails() throws Exception {
        User testUser = Utils.testUser1;
        lenient().when(userRepository.createAsync(testUser))
                .thenReturn(CompletableFuture.failedFuture(new SQLException()));

        userService.createAsync(testUser).get();
    }

    @Test
    @Description("NullPointerException при передаче в качестве параметра null")
    public void CreateAsync_NullUser() {

        assertThrows(NullPointerException.class, () -> {
            userService.createAsync(null).get();
        });
        verifyNoInteractions(userRepository);
    }
    //---------------------------------------------------------------
    // CreateTest
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // DeleteTest
    //---------------------------------------------------------------

    @Test
    @Description("Успешное удаление пользователя")
    public void deleteByIdAsync_ShouldReturnTrue_WhenUserDeleted() throws SQLException {
        UUID id = UUID.randomUUID();

        when(userRepository.deleteAsync(id)).thenReturn(CompletableFuture.completedFuture(true));

        assertTrue(userService.deleteByIdAsync(id).join());
    }

    @Test
    @Description("Пользователя удалить не удалось")
    public void deleteByIdAsync_ShouldReturnFalse_WhenUserNotExists() throws SQLException {

        UUID id = UUID.randomUUID();
        when(userRepository.deleteAsync(id))
                .thenReturn(CompletableFuture.completedFuture(false));

        CompletableFuture<Boolean> future = userService.deleteByIdAsync(id);

        assertFalse(future.join());
    }

    @Test
    @Description("NullPointerException при передаче в качестве параметра null")
    public void deleteByIdAsync_ShouldThrow_WhenIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                userService.deleteByIdAsync(null).get());
    }

    @Test(expected = ExecutionException.class)
    @Description("Удаление пользователя возвращает ошибку ExecutionException")
    public void deleteByIdAsync_ShouldThrowExecutionException_WhenDatabaseError() throws ExecutionException, InterruptedException, SQLException {
        UUID id = UUID.randomUUID();

        lenient().when(userRepository.deleteAsync(eq(id)))
                .thenReturn(CompletableFuture.failedFuture(new SQLException("DB error")));

        userService.deleteByIdAsync(id).get();
        verify(userRepository).deleteAsync(id);
    }

    //---------------------------------------------------------------
    // DeleteTest
    //---------------------------------------------------------------


    //---------------------------------------------------------------
    // GetAllTest
    //---------------------------------------------------------------

    @Test
    @Description("Успешное получение списка всех пользователей")
    public void getAll_ShouldPreserveUserOrder() throws SQLException {

        List<User> users = List.of(
                testUser1,
                testUser2,
                testUser3
        );

        List<UserDto> userDtos = users.stream().map(UserMapper::toDto).toList();

        when(userRepository.findAllAsync()).thenReturn(CompletableFuture.completedFuture(users));

        List<UserDto> result = userService.getAllAsync().join();

        assertEquals(testUser1.getUserName(), result.get(0).getUserName());
        assertEquals(testUser2.getUserName(), result.get(1).getUserName());
        assertEquals(testUser3.getUserName(), result.get(2).getUserName());
        verify(userRepository).findAllAsync();
    }

    @Test
    @Description("Ошибка при получении списка пользователей")
    public void getAll_ShouldThrow_WhenRepositoryFails() throws SQLException {

        RuntimeException expectedException = new RuntimeException("DB error");
        when(userRepository.findAllAsync())
                .thenReturn(CompletableFuture.failedFuture(expectedException));

        CompletableFuture<List<UserDto>> resultFuture = userService.getAllAsync();
        CompletionException exception = assertThrows(CompletionException.class, resultFuture::join);

        assertTrue(resultFuture.isCompletedExceptionally());
        assertTrue(exception.getCause() instanceof RuntimeException);
        verify(userRepository).findAllAsync();
    }

    @Test
    @Description("Получение пустого списка пользователей")
    public void getAll_ShouldReturnEmptyList_WhenNoUsersExist() throws SQLException {

        when(userRepository.findAllAsync())
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        CompletableFuture<List<UserDto>> resultFuture = userService.getAllAsync();
        List<UserDto> result = resultFuture.join();

        assertTrue(result.isEmpty());
        verify(userRepository).findAllAsync();
    }
    //---------------------------------------------------------------
    // GetAllTest
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // GetByIdTest
    //---------------------------------------------------------------

    @Test
    @Description("Попытка получения пользователя по несуществующему ID")
    public void getByIdAsync_ShouldReturnNull_WhenUserNotFound() throws SQLException {

        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findByIdAsync(nonExistentId))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<UserDto> resultFuture = userService.getByIdAsync(nonExistentId);
        UserDto result = resultFuture.join();

        assertNull(result);
        verify(userRepository).findByIdAsync(nonExistentId);
    }

    @Test
    @Description("Ошибка при получении пользователя по ID")
    public void getByIdAsync_ShouldThrowError() throws SQLException {
        UUID userId = UUID.randomUUID();
        RuntimeException error = new RuntimeException("Test error");

        when(userRepository.findByIdAsync(userId))
                .thenReturn(CompletableFuture.failedFuture(error));

        var resultFuture = userService.getByIdAsync(userId);
        CompletionException exception = assertThrows(CompletionException.class, resultFuture::join);

        assertTrue(resultFuture.isCompletedExceptionally());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    @Description("NullPointerException при передаче в качестве параметра null")
    public void getByIdAsync_ShouldThrowIllegalArgumentException_WhenIdIsNull() {

        assertThrows(NullPointerException.class,
                () -> userService.getByIdAsync(null));

        verifyNoInteractions(userRepository);
    }

    @Test
    @Description("Успешное получение пользователя по ID")
    public void getByIdAsync_SuccessGetUser() throws SQLException {

        User user = testUser1;
        UUID id = UUID.randomUUID();

        when(userRepository.findByIdAsync(eq(id)))
                .thenReturn(CompletableFuture.completedFuture(user));

        var result = userService.getByIdAsync(id).join();

        assertEquals(testUser1.getUserName(), result.getUserName());
        assertEquals(testUser1.getEmail(), result.getEmail());
        verify(userRepository).findByIdAsync(id);
    }

    //---------------------------------------------------------------
    // GetByIdTest
    //---------------------------------------------------------------

    //---------------------------------------------------------------
    // UpdateTest
    //---------------------------------------------------------------
    @Test
    @Description("Успешное обновление пользователя")
    public void updateByIdAsync_ShouldReturnUpdatedUser_WhenUserExists() throws SQLException {
        User updatedUser = testUser1;

        when(userRepository.updateAsync(updatedUser))
                .thenReturn(CompletableFuture.completedFuture(updatedUser));

        CompletableFuture<UserDto> resultFuture = userService.updateByIdAsync(UserMapper.toDto(updatedUser));
        UserDto result = resultFuture.join();
       assertNotNull(result);
        assertEquals(testUser1.getUserName(), result.getUserName());
        verify(userRepository).updateAsync(updatedUser);
    }

    @Test
    @Description("Попытка обновления пользователя, которого не существует")
    public void updateByIdAsync_ShouldThrow_WhenUserNotFound() throws SQLException {

        UUID userId = UUID.randomUUID();
        User user = testUser1;
        user.setId(userId);

        when(userRepository.updateAsync(user))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<UserDto> future = userService.updateByIdAsync(UserMapper.toDto(user));
        CompletionException exception = assertThrows(CompletionException.class, future::join);

        assertTrue(exception.getCause() instanceof UserNotFoundException);
        assertEquals(
                "User with id " + userId + " not found",
                exception.getCause().getMessage()
        );
    }

    @Test
    @Description("Ошибка при обновлении пользователя")
    public void updateByIdAsync_ShouldThrow_WhenRepositoryFails() throws SQLException {

        UUID userId = UUID.randomUUID();
        User user = testUser1;
        user.setId(userId);
        RuntimeException dbError = new RuntimeException("Database error");

        UserRepository repo = Mockito.mock(UserRepository.class);
        userService = new UsersService(repo);


        when(userRepository.updateAsync(user))
                .thenReturn(CompletableFuture.failedFuture(dbError));

        CompletableFuture<UserDto> future = userService.updateByIdAsync(UserMapper.toDto(user));
        CompletionException exception = assertThrows(CompletionException.class, future::join);

        assertEquals("Failed to update user", exception.getMessage());
        assertSame(dbError, exception.getCause());
    }

    @Test
    @Description("NullPointerException при передаче в качестве параметра null")
    public void updateByIdAsync_ShouldThrow_WhenUserIsNull() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> userService.updateByIdAsync(null)
        );

        assertEquals(StaticConstants.PARAMETER_IS_NULL_EXCEPTION_MESSAGE, exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    //---------------------------------------------------------------
    // UpdateTest
    //---------------------------------------------------------------

}*/
