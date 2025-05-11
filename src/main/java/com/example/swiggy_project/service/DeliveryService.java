package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.Delivery;
import com.example.swiggy_project.model.Order;
import com.example.swiggy_project.repository.DeliveryRepository;
import com.example.swiggy_project.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {
    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Delivery addDelivery(@Valid Delivery delivery) {
        if (delivery.getOrderId() == null || delivery.getOrderId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID must not be null or empty");
        }
        return deliveryRepository.save(delivery);
    }

    public Delivery getDeliveryById(String id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
    }

    public List<Delivery> getDeliveriesByDeliveryBoy(String deliveryBoyId) {
        if (deliveryBoyId == null || deliveryBoyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery boy ID must not be null or empty");
        }
        List<Delivery> deliveries = deliveryRepository.findByDeliveryBoyId(deliveryBoyId);
        // Fetch full Order objects for each Delivery
        return deliveries.stream().map(delivery -> {
            if (delivery.getOrderId() != null) {
                Order order = orderRepository.findById(delivery.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + delivery.getOrderId()));
                delivery.setOrder(order);
            }
            return delivery;
        }).collect(Collectors.toList());
    }
}