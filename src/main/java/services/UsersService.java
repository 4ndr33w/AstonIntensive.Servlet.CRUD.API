package services;

import models.dtos.UserDto;
import models.entities.User;
import repositories.UsersRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.BaseService;
import services.interfaces.UserService;
import utils.mappers.UserMapper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersService implements UserService {

    private final UserRepository userRepository;

    public UsersService() throws SQLException {
        this.userRepository = new UsersRepository();
    }


    @Override
    public CompletableFuture<UserDto> getByIdAsync(UUID id) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<UserDto> updateByIdAsync(UUID id, UserDto entity) {
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<List<UserDto>> getAllAsync() throws SQLException {
        return userRepository.findAllAsync()
                .thenApplyAsync(users -> users.stream()
                                .map(UserMapper::toDto)
                                .collect(Collectors.toList()))
                .exceptionally(ex -> {
                    System.err.println("Error fetching users: " + ex.getMessage());
                    return Collections.emptyList(); // Fallback
                });
    }

    @Override
    public CompletableFuture<UserDto> createUserAsync(User user) throws Exception {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDto> getUserByEmailAsync(String email) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDto> getUserByUserNameAsync(String username) throws Exception {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDto> updateEmailAsync(String oldEmail, String newEmail) throws Exception {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<UserDto> updatePasswordAsync(UUID userId, String password) throws Exception {
        return CompletableFuture.completedFuture(null);
    }
}
