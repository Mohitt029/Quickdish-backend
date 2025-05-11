package com.example.swiggy_project.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the system.
 */
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @NotBlank(message = "Username is mandatory")
    private String username;

    @NotBlank(message = "Address is mandatory")
    private String address;

    @NotBlank(message = "Phone number is mandatory")
    private String phoneNo;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @NotBlank(message = "Role is mandatory")
    private String role; // Supports CUSTOMER, ADMIN, DELIVERY_BOY

    private String firstName;
    private String lastName;
    private String gender;
    private String bio;

    private List<String> likedMenuItems = new ArrayList<>(); // List of MenuItem IDs the user has liked

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getLikedMenuItems() {
        return likedMenuItems;
    }

    public void setLikedMenuItems(List<String> likedMenuItems) {
        this.likedMenuItems = likedMenuItems;
    }
}