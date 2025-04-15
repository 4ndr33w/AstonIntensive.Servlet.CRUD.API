package services;

import models.entities.User;
import org.junit.Test;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.UserRepository;
import services.interfaces.UserService;
import testUtils.Utils;
import utils.StaticConstants;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class UsersServiceTest {

    @Mock private UsersRepositoryImplementation userRepository;

    @Mock private ProjectServiceImplNew projectService;

    @InjectMocks private UsersService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UsersRepositoryImplementation.class);
        userService = new UsersService();
    }

    //---------------------------------------------------------------
    // CreateTest
    //---------------------------------------------------------------
    @Test//(timeout = 1000)
    public void testCreateAsync_Success() {
        User testUser = Utils.testUser1;

        var anyResult = userRepository.createAsync(testUser);
        when(anyResult)
                .thenReturn(CompletableFuture.completedFuture(testUser));

        CompletableFuture<User> resultFuture =  userRepository.createAsync(testUser);

        assertNotNull(resultFuture);
        User result = resultFuture.join();
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getUserName(), result.getUserName());
        verify(userRepository).createAsync(testUser);
    }

    @Test(expected = ExecutionException.class)
    public void createAsync_ShouldThrow_WhenRepositoryFails() throws Exception {
        User testUser = Utils.testUser1;
        lenient().when(userRepository.createAsync(testUser))
                .thenReturn(CompletableFuture.failedFuture(new SQLException()));

        userService.createAsync(testUser).get();
    }

    @Test
    public void CreateAsync_NullUser() {

        assertThrows(NullPointerException.class, () -> {
            userService.createAsync(null).get();
        });
        verifyNoInteractions(userRepository);
    }
    //---------------------------------------------------------------
    // CreateTest
    //---------------------------------------------------------------

    @Test
    public void getAllAsyncTest() {
        UsersService usersService = new UsersService();

        var users = usersService.getAllAsync().join();

        assertNotNull(users);
    }


    //---------------------------------------------------------------
    // DeleteTest
    //---------------------------------------------------------------
    @Test
    public void deleteByIdAsync_ShouldReturnTrue_WhenUserDeleted() {
        UUID id = UUID.randomUUID();
        when(userRepository.deleteAsync(id)).thenReturn(CompletableFuture.completedFuture(true));

        assertTrue(userService.deleteByIdAsync(id).join());
        assertDoesNotThrow(() -> userService.deleteByIdAsync(id).get());
    }

    @Test
    public void deleteByIdAsync_ShouldThrow_WhenIdIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                userService.deleteByIdAsync(null));
    }

    @Test
    public void deleteByIdAsync_ShouldWrapGenericException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(userRepository.deleteAsync(eq(id)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Generic error")));

        // Act & Assert
        CompletionException exception = assertThrows(CompletionException.class, () ->
                userService.deleteByIdAsync(id).join());

        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    public void deleteByIdAsync_ShouldThrowCompletionException_WhenDatabaseError() {

        UUID id = UUID.randomUUID();
        //UUID id = UUID.fromString("ada6fe61-f4d4-46ba-9739-e59304705f20");
        when(userRepository.deleteAsync(eq(id)))
                .thenReturn(CompletableFuture.failedFuture(new SQLException("DB error")));

        assertThrows(CompletionException.class, () ->userService.deleteByIdAsync(id) );
                //.thenThrow(new SQLException("DB error"));

        // Act & Assert
        //CompletionException exception = assertThrows(CompletionException.class, () ->
                //userService.deleteByIdAsync(id).get());

        //assertTrue(exception.getCause() instanceof SQLException);

        //assertThrows(CompletionException.class, () -> userService.deleteByIdAsync(id).join());
        //assertTrue(userService.deleteByIdAsync(id).isCompletedExceptionally());
        //assertEquals(StaticConstants.DATABASE_ACCESS_EXCEPTION_MESSAGE, exception.getMessage());
    }
    //---------------------------------------------------------------
    // DeleteTest
    //---------------------------------------------------------------
}
