package com.example.swiggy_project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a delivery associated with an order in the system.
 */
@Document(collection = "deliveries")
public class Delivery {
    @Id
    private String id;
    private String orderId; // Store only the order ID
    private String deliveryBoyId;
    private String status;
    private transient Order order; // Transient to avoid serialization in MongoDB

    // Constructors
    public Delivery() {}

    public Delivery(String orderId, String deliveryBoyId, String status) {
        this.orderId = orderId;
        this.deliveryBoyId = deliveryBoyId;
        this.status = status;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDeliveryBoyId() {
        return deliveryBoyId;
    }

    public void setDeliveryBoyId(String deliveryBoyId) {
        this.deliveryBoyId = deliveryBoyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            this.orderId = order.getId();
        }
    }
}