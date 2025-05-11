package com.example.swiggy_project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Document(collection = "menu_items")
public class MenuItem {
    @Id
    private String id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @Positive(message = "Price must be positive")
    private double price;

    private String foodMenuId;

    private FoodMenu foodMenu;

    @NotBlank(message = "Cuisine type is mandatory")
    private String cuisineType;

    @NotBlank(message = "Meal type is mandatory")
    private String mealType;

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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getFoodMenuId() {
        return foodMenuId;
    }

    public void setFoodMenuId(String foodMenuId) {
        this.foodMenuId = foodMenuId;
    }

    public FoodMenu getFoodMenu() {
        return foodMenu;
    }

    public void setFoodMenu(FoodMenu foodMenu) {
        this.foodMenu = foodMenu;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
}