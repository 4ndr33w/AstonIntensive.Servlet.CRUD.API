package models.dtos;

import java.util.Date;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class ProjectUsersDto {

    private UUID userId;
    private UUID projectId;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();

    public ProjectUsersDto() {}

        public ProjectUsersDto(UUID userId, UUID projectId, Date createdAt, Date updatedAt) {
        this.userId = userId;
        this.projectId = projectId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ProjectUsersDto(UUID userId, UUID projectId) {
        this.userId = userId;
        this.projectId = projectId;
    }

    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    public UUID getProjectId() {
        return projectId;
    }
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
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
}
