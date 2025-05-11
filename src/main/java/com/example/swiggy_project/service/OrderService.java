package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.*;
import com.example.swiggy_project.repository.OrderRepository;
import com.example.swiggy_project.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private MenuItemService menuItemService;

    private static final List<String> ASSIGNABLE_STATUSES = Arrays.asList("PLACED", "PREPARING");

    public Order placeOrder(String userId, String restaurantId) {
        logger.info("Placing order for user ID: {} from restaurant ID: {}", userId, restaurantId);

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Get the user's cart
        Cart cart = cartService.getCart(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty for user ID: " + userId);
        }

        // Create a new order
        Order order = new Order();
        order.setUserId(userId);
        order.setRestaurantId(restaurantId);
        order.setStatus("PLACED");

        // Populate order items from cart
        List<Order.OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            MenuItem menuItem = menuItemService.getMenuItemById(cartItem.getMenuItemId());
            return new Order.OrderItem(menuItem.getName(), menuItem.getPrice(), cartItem.getQuantity());
        }).collect(Collectors.toList());
        order.setItems(orderItems);

        // Calculate total amount
        double totalAmount = orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        order.setTotalAmount(totalAmount);

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Clear the cart after placing the order
        cartService.clearCart(userId);

        return savedOrder;
    }

    public Order updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByRestaurant(String userId, String restaurantId) {
        return orderRepository.findByUserIdAndRestaurantId(userId, restaurantId);
    }

    public String getOrderStatus(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return order.getStatus();
    }

    public Order assignDelivery(String orderId, String deliveryBoyId) {
        logger.info("Assigning delivery for order ID: {} to delivery boy ID: {}", orderId, deliveryBoyId);

        User deliveryBoy = userRepository.findById(deliveryBoyId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery boy not found: " + deliveryBoyId));
        if (!"DELIVERY_BOY".equals(deliveryBoy.getRole())) {
            throw new IllegalArgumentException("User with id " + deliveryBoyId + " is not a delivery boy");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!ASSIGNABLE_STATUSES.contains(order.getStatus())) {
            throw new IllegalStateException("Order with id " + orderId + " is not in an assignable state. Current status: " + order.getStatus());
        }

        order.setDeliveryBoyId(deliveryBoyId);
        Order savedOrder = orderRepository.save(order);
        logger.info("Delivery assigned successfully for order ID: {}", orderId);
        return savedOrder;
    }

    public Bill getBill(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        Bill bill = new Bill();
        bill.setOrderId(orderId);
        double subtotal = order.getTotalAmount();
        double tax = subtotal * 0.05; // 5% tax
        double discount = order.getCouponCode() != null ? order.getTotalAmount() * 0.1 : 0.0; // 10% if coupon
        double total = subtotal + tax - discount;
        bill.setSubtotal(subtotal);
        bill.setTax(tax);
        bill.setDiscount(discount);
        bill.setTotal(total);
        return bill;
    }

    public Order updateOrder(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order addOrder(@Valid Order order) {
        return orderRepository.save(order);
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getDeliveriesByDeliveryBoy(String deliveryBoyId) {
        return List.of();
    }
}