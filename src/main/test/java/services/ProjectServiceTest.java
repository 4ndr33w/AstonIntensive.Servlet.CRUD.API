package services;

import models.dtos.ProjectUsersDto;
import models.entities.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import repositories.ProjectUsersRepositoryImpl;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.ProjectUserRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;
import testUtils.Utils;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;

/**
 * @author 4ndr33w
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectUserRepository projectUserRepository;
    @InjectMocks private UsersService userService;





}
