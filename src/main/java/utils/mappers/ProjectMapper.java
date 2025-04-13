package utils.mappers;

import models.dtos.ProjectDto;
import models.dtos.UserDto;
import models.entities.Project;
import models.enums.ProjectStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectMapper {


    public static ProjectDto toDto(Project project) {
        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(project.getId());
        projectDto.setName(project.getName());
        projectDto.setDescription(project.getDescription());
        projectDto.setCreatedAt(project.getCreatedAt());
        projectDto.setUpdatedAt(project.getUpdatedAt());
        projectDto.setImage(project.getImage());
        projectDto.setAdminId(project.getAdminId());
        projectDto.setProjectStatus(project.getProjectStatus());

        if(project.getProjectUsers() != null) {
            List<UUID> usersIds = project
                    .getProjectUsers()
                    .stream()
                    .map(UserDto::getId)
                    .toList();

            projectDto.setProjectUsersIds(usersIds);
        }
        else projectDto.setProjectUsersIds(List.of());

        return projectDto;
    }

    public static Project mapToEntity(ProjectDto projectDto, List<UserDto> users) {
        if (projectDto != null) {
            Project project = new Project();
            project.setId(projectDto.getId());
            project.setName(projectDto.getName());
            project.setDescription(projectDto.getDescription());
            project.setCreatedAt(projectDto.getCreatedAt());
            project.setUpdatedAt(projectDto.getUpdatedAt());
            project.setImage(projectDto.getImage());
            project.setAdminId(projectDto.getAdminId());
            project.setProjectStatus(projectDto.getProjectStatus());
            project.setProjectUsers(users);

            return project;
        }
        return null;
    }

    public static Project mapResultSetToProject(ResultSet rs) throws SQLException {
        return new Project(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("description"),
                new Date(rs.getTimestamp("created_at").getTime()),
                rs.getTimestamp("updated_at") != null ?
                        new Date(rs.getTimestamp("updated_at").getTime()) : null,
                rs.getBytes("image"),
                UUID.fromString(rs.getString("admin_id")),
                ProjectStatus.values()[Integer.parseInt(rs.getString("project_status"))]
        );
    }
}
