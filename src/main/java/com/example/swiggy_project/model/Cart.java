package com.example.swiggy_project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
public class Cart {
    @Id
    private String id;

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    private List<CartItem> items = new ArrayList<>();

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public static class CartItem {
        @NotBlank(message = "Menu item ID is mandatory")
        private String menuItemId;

        @Positive(message = "Quantity must be positive")
        private int quantity;

        @Positive(message = "Price must be positive")
        private double price;

        // Getters and Setters
        public String getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(String menuItemId) {
            this.menuItemId = menuItemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}