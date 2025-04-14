package services;

import models.entities.Project;
import repositories.ProjectsRepositoryImplementation;
import repositories.UsersRepositoryImplementation;
import repositories.interfaces.ProjectRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.ProjectService;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectsService implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectsService() throws SQLException {
        this.projectRepository = new ProjectsRepositoryImplementation();
        this.userRepository = new UsersRepositoryImplementation();
    }

    @Override
    public CompletableFuture<List<Project>> getByUserIdAsync() throws SQLException {
        return null;
    }

    @Override
    public CompletableFuture<List<Project>> getByAdminIdAsync() throws SQLException {
        return null;
    }

    @Override
    public CompletableFuture<Project> addUserToProjectAsync() {
        return null;
    }

    @Override
    public CompletableFuture<Project> removeUserFromProjectAsync() {


        return null;
    }

    @Override
    public CompletableFuture<Project> createAsync(Project entity) throws Exception {
        return null;
    }

    @Override
    public CompletableFuture<Project> getByIdAsync(UUID id) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID id) throws SQLException {
        return null;
    }

    @Override
    public CompletableFuture<Project> updateByIdAsync(UUID id, Project entity) {
        return null;
    }
}
