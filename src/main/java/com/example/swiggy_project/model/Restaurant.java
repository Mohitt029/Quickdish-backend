package com.example.swiggy_project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

@Document(collection = "restaurants")
public class Restaurant {
    @Id
    private String id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Address is mandatory")
    private String address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}