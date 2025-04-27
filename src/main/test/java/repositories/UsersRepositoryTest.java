package repositories;

import org.junit.jupiter.api.Test;
import repositories.interfaces.UserRepository;
import testUtils.Utils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * @author 4ndr33w
 * @version 1.0
 */
public class UsersRepositoryTest extends Utils {

    UserRepository userRepository;

    public UsersRepositoryTest() {
        userRepository = new UsersRepository();
    }


    @Test
    public void findAllByIdsTestSuccess() throws ExecutionException, InterruptedException {

        UUID id1 = UUID.fromString("b16ea0d1-920a-4898-a660-6f21ff2582c5");
        UUID id2 = UUID.fromString("24cf4e9d-0b56-4c4c-aa45-2cdd93774a3b");
        UUID id3 = UUID.fromString("234a06d9-7df5-4fa1-8d40-acb1558479f5");
        UUID id4 = UUID.fromString("c0e0932c-c2ec-4835-b8ab-b95cf44f7965");
        UUID id5 = UUID.fromString("defea91a-e782-4a40-8dfd-258d9cc7407e");

        var resustTest = userRepository.findAllByIdsAsync(List.of(id1, id2, id3, id4, id5));

        var result = userRepository.findAllByIdsAsync(List.of(id1, id2, id3, id4, id5));

        var users = result.get();

        assertNotNull(users);
    }

}
/*
package repositories.synchronous;

import org.junit.jupiter.api.Test;
import repositories.interfaces.synchronous.ProjectRepoSynchro;

//import repositories.synchronous.ProjectsRepository;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;

/**
 * @author 4ndr33w
 * @version 1.0
 */
/*
public class ProjectRepositoryTest {


}

 */