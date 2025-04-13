package models.entities;

import models.dtos.ProjectDto;
import models.enums.UserRoles;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author 4ndr33w
 * @version 1.0
 */
public class User {

    private UUID id;
    private String userName;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRoles userRole;
    private byte[] userImage;

    private Date createdAt;
    private Date updatedAt;
    private Date lastLoginDate;

    List<ProjectDto> projects;

    public User() {}

    public User(UUID id,
                String userName,
                String password,
                String email,
                String firstName,
                String lastName,
                String phoneNumber,
                UserRoles userRole,
                byte[] userImage,
                Date createdAt,
                Date updatedAt,
                Date lastLoginDate) {

        this.id = id;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
        this.userImage = userImage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginDate = lastLoginDate;
        this.projects = new java.util.ArrayList<>();
    }

    public User(String userName,
                String password,
                String email,
                String firstName,
                String lastName,
                String phoneNumber,
                UserRoles userRole,
                byte[] userImage,
                Date createdAt,
                Date updatedAt,
                Date lastLoginDate) {

        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
        this.userImage = userImage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginDate = lastLoginDate;
        this.projects = new java.util.ArrayList<>();
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
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
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
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
    public Date getLastLoginDate() {
        return lastLoginDate;
    }
    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    public List<ProjectDto> getProjects() {
        return projects;
    }
    public void setProjects(List<ProjectDto> projects) {
        this.projects = projects;
    }
}
