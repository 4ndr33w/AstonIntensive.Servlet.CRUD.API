package models.dtos;

import models.enums.ProjectStatus;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Класс Dto для объекта Project
 *
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectDto {

    protected UUID id;
    protected String name;
    protected String description;
    protected Date createdAt = new Date();
    protected Date updatedAt = new Date();
    protected byte[] image;
    private UUID adminId;
    private ProjectStatus projectStatus;
    private List<UUID> projectUsersIds;

    public ProjectDto() {}
    public ProjectDto(
            UUID id,
            String name,
            String description,
            Date createdAt,
            Date updatedAt,
            byte[] image,
            UUID adminId,
            ProjectStatus projectStatus,
            List<UUID> projectUsersIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.image = image;
        this.adminId = adminId;
        this.projectStatus = projectStatus;
        this.projectUsersIds = projectUsersIds;
    }
    public ProjectDto(
            String name,
            String description,
            Date createdAt,
            Date updatedAt,
            byte[] image,
            UUID adminId,
            ProjectStatus projectStatus,
            List<UUID> projectUsersIds) {
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.image = image;
        this.adminId = adminId;
        this.projectStatus = projectStatus;
        this.projectUsersIds = projectUsersIds;
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
    public List<UUID> getProjectUsersIds() {
        return projectUsersIds;
    }
    public void setProjectUsersIds(List<UUID> projectUsersIds) {
        this.projectUsersIds = projectUsersIds;
    }
}
