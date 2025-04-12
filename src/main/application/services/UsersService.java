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

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersService implements BaseService<UserDto>, UserService {

    private final UserRepository userRepository;

    /*public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }*/

    public UsersService() throws SQLException {
        this.userRepository = new UsersRepository();
    }


    @Override
    public UserDto getById(UUID id) {
        return null;
    }

    @Override
    public boolean deleteById(UUID id) {
        return false;
    }

    @Override
    public UserDto updateById(UUID id, UserDto entity) {
        return null;
    }

    @Override
    public List<UserDto> getAll() throws SQLException {

        var users = userRepository.findAll();
        return users.map(userList -> userList
                .stream()
                .map(UserMapper::toDto)
                .toList())
                .orElseGet(List::of);
    }

    @Override
    public UserDto createUser(User user) throws Exception {
        return null;
    }

    @Override
    public UserDto getUserByEmail(String email) {
        return null;
    }

    @Override
    public UserDto getUserByUserName(String username) throws Exception {
        return null;
    }

    @Override
    public UserDto updateEmail(String oldEmail, String newEmail) throws Exception {
        return null;
    }

    @Override
    public UserDto updatePassword(UUID userId, String password) throws Exception {
        return null;
    }
}
