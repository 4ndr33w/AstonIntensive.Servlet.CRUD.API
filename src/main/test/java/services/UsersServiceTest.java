package services;

import models.entities.User;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;
import services.interfaces.UserService;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectServiceImplNew projectService;

    @InjectMocks
    private UserService userService;

    @Test
    public void getAllAsyncTest() throws SQLException, ExecutionException, InterruptedException {
        UsersService usersService = new UsersService();

        var users = usersService.getAllAsync().get();

        assertNotNull(users);
    }

   /* @Test
    void testGetByIdAsync_Success() throws Exception {
        // Подготовка
        UUID userId = UUID.randomUUID();
        User user = new User();
        CompletableFuture<User> futureUser = CompletableFuture.completedFuture(user);

        when(userRepository.findByIdAsync(userId)).thenReturn(futureUser);
        when(userService.enrichUserWithProjects(user)).thenReturn(CompletableFuture.completedFuture(user));

        // Выполнение
        CompletableFuture<User> result = userService.getByIdAsync(userId);

        // Проверка
        assertTrue(result.isDone());
        assertEquals(user, result.get());

        verify(userRepository).findByIdAsync(userId);
        verify(projectService).enrichUserWithProjects(user);
    }*/
}
