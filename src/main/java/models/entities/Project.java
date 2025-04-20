package models.entities;

import models.dtos.UserDto;
import models.enums.ProjectStatus;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Класс, представляющий модель проекта
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class Project {

    private UUID id;
    private String name;
    private String description;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
    private byte[] image;
    private UUID adminId;
    private ProjectStatus projectStatus = ProjectStatus.ACTIVE;
    private List<UserDto> projectUsers;

    public Project() {}
    public Project(
            UUID id,
            String name,
            String description,
            Date createdAt,
            Date updatedAt,
            byte[] image,
            UUID adminId,
            ProjectStatus projectStatus,
            List<UserDto> projectUsers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.image = image;
        this.adminId = adminId;
        this.projectStatus = projectStatus;
        this.projectUsers = projectUsers;
    }

    public Project(
            String name,
            String description,
            Date createdAt,
            Date updatedAt,
            byte[] image,
            UUID adminId,
            ProjectStatus projectStatus,
            List<UserDto> projectUsers) {
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.image = image;
        this.adminId = adminId;
        this.projectStatus = projectStatus;
        this.projectUsers = projectUsers;
    }

    public Project(Project project, List<UserDto> projectUsers) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
        this.image = project.getImage();
        this.adminId = project.getAdminId();
        this.projectStatus = project.getProjectStatus();
        this.projectUsers = projectUsers;
    }

    public Project(UUID id,
                   String name,
                   String description,
                   Date createdAt,
                   Date updatedAt,
                   byte[] image,
                   UUID adminId,
                   ProjectStatus projectStatus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.image = image;
        this.adminId = adminId;
        this.projectStatus = projectStatus;
        this.projectUsers = null;
    }

    public Project(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
        this.image = project.getImage();
        this.adminId = project.getAdminId();
        this.projectStatus = project.getProjectStatus();
        this.projectUsers = project.getProjectUsers();
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
    public UUID getAdminId() {
        return adminId;
    }
    public void setAdminId(UUID adminId) {
        this.adminId = adminId;
    }
    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }
    public void setProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public List<UserDto> getProjectUsers() {
        return projectUsers;
    }
    public void setProjectUsers(List<UserDto> projectUsers) {
        this.projectUsers = projectUsers;
    }
}
