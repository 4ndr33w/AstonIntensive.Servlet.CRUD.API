package models.dtos;

import models.enums.UserRoles;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Класс Dto для объекта User
 * @author 4ndr33w
 * @version 1.0
 */
public class UserDto {

    private UUID id;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRoles userRole;
    private byte[] userImage;
    private Date createdAt;
    List<ProjectDto> projects;

    public UserDto() {
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID userId) {
        this.id = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public UserRoles getUserRole() {
        return userRole;
    }
    public void setUserRole(UserRoles userRole) {
        this.userRole = userRole;
    }
    public byte[] getUserImage() {
        return userImage;
    }
    public void setUserImage(byte[] userImage) {
        this.userImage = userImage;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public List<ProjectDto> getProjects() {
        return projects;
    }
    public void setProjects(List<ProjectDto> projects) {
        this.projects = projects;
    }
}
