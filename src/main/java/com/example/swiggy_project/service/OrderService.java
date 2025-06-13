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

    @Autowired
    private CouponService couponService;

    @Autowired
    private OrderTrackingService orderTrackingService;

    private static final List<String> ASSIGNABLE_STATUSES = Arrays.asList("PLACED", "PREPARING");

    private static final List<String> VALID_STATUSES = Arrays.asList(
            "PLACED", "PREPARING", "COOKING", "PACKED", "DISPATCHED", "DELIVERED", "CANCELLED"
    );

    public Order placeOrder(String userId, String restaurantId, String deliveryAddress) {
        logger.info("Placing order for user ID: {} from restaurant ID: {} with delivery address: {}", userId, restaurantId, deliveryAddress);

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Validate delivery address
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            logger.warn("Delivery address is null or empty for user ID: {}", userId);
            throw new IllegalArgumentException("Delivery address must not be null or empty");
        }

        // Get the user's cart
        Cart cart = cartService.getCart(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            logger.warn("Cart is empty for user ID: {}", userId);
            throw new IllegalStateException("Cart is empty for user ID: " + userId);
        }

        // Create a new order
        Order order = new Order();
        order.setUserId(userId);
        order.setRestaurantId(restaurantId);
        order.setDeliveryAddress(deliveryAddress);
        order.setStatus("PLACED");

        // Populate order items from cart
        List<Order.OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            MenuItem menuItem = menuItemService.getMenuItemById(cartItem.getMenuItemId());
            // Increment the number of times ordered
            menuItem.setNumberOfTimesOrdered(menuItem.getNumberOfTimesOrdered() + cartItem.getQuantity());
            menuItemService.updateMenuItem(menuItem);
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

        // Add order to user's order history
        List<String> orderHistory = user.getOrderHistory();
        orderHistory.add(savedOrder.getId());
        user.setOrderHistory(orderHistory);
        userRepository.save(user);

        // Clear the cart after placing the order
        cartService.clearCart(userId);

        logger.info("Order placed successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    public Order updateOrderStatus(String orderId, String status) {
        if (!VALID_STATUSES.contains(status)) {
            logger.warn("Invalid status: {}. Valid statuses are: {}", status, VALID_STATUSES);
            throw new IllegalArgumentException("Invalid status: " + status + ". Valid statuses are: " + VALID_STATUSES);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        // Send WebSocket update
        orderTrackingService.sendOrderStatusUpdate(orderId, status);
        logger.info("Order status updated to {} for order ID: {}", status, orderId);
        return savedOrder;
    }

    public List<Order> getOrdersByUser(String userId) {
        logger.info("Fetching orders for user ID: {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        logger.info("Found {} orders for user ID: {}", orders.size(), userId);
        return orders;
    }

    public List<Order> getOrdersByRestaurant(String userId, String restaurantId) {
        logger.info("Fetching orders for user ID: {} from restaurant ID: {}", userId, restaurantId);
        List<Order> orders = orderRepository.findByUserIdAndRestaurantId(userId, restaurantId);
        logger.info("Found {} orders for user ID: {} from restaurant ID: {}", orders.size(), userId, restaurantId);
        return orders;
    }

    public String getOrderStatus(String orderId) {
        logger.info("Fetching status for order ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        logger.info("Order status for order ID: {} is: {}", orderId, order.getStatus());
        return order.getStatus();
    }

    public Order assignDelivery(String orderId, String deliveryBoyId) {
        logger.info("Assigning delivery for order ID: {} to delivery boy ID: {}", orderId, deliveryBoyId);

        User deliveryBoy = userRepository.findById(deliveryBoyId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery boy not found: " + deliveryBoyId));
        if (!"ROLE_DELIVERY_BOY".equals(deliveryBoy.getRole())) {
            logger.warn("User with ID: {} is not a delivery boy", deliveryBoyId);
            throw new IllegalArgumentException("User with id " + deliveryBoyId + " is not a delivery boy");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!ASSIGNABLE_STATUSES.contains(order.getStatus())) {
            logger.warn("Order with ID: {} is not in an assignable state. Current status: {}", orderId, order.getStatus());
            throw new IllegalStateException("Order with id " + orderId + " is not in an assignable state. Current status: " + order.getStatus());
        }

        order.setDeliveryBoyId(deliveryBoyId);
        order.setStatus("DISPATCHED");
        Order savedOrder = orderRepository.save(order);
        logger.info("Delivery assigned successfully for order ID: {}", orderId);
        return savedOrder;
    }

    public Bill getBill(String orderId) {
        logger.info("Generating bill for order ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Bill bill = new Bill();
        bill.setOrderId(orderId);

        // Subtotal is the sum of item prices before any discounts
        double subtotal = order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Calculate discount based on the coupon (if applied)
        double discount = 0.0;
        if (order.getCouponCode() != null) {
            Coupon coupon = couponService.getCouponByCode(order.getCouponCode());
            discount = (coupon.getDiscount() / 100) * subtotal; // Percentage-based discount
            logger.info("Applied {}% discount of {} to order ID: {}", coupon.getDiscount(), discount, orderId);
        }

        // Subtotal after discount
        double discountedSubtotal = subtotal - discount;

        // Apply taxes (2.5% CGST + 2.5% SGST = 5% total tax) on the discounted subtotal
        double cgst = discountedSubtotal * 0.025; // 2.5% CGST
        double sgst = discountedSubtotal * 0.025; // 2.5% SGST
        double tax = cgst + sgst;

        // Final total
        double total = discountedSubtotal + tax;

        bill.setSubtotal(subtotal);
        bill.setDiscount(discount);
        bill.setCgst(cgst);
        bill.setSgst(sgst);
        bill.setTax(tax);
        bill.setTotal(total);

        logger.info("Bill generated for order ID: {}. Subtotal: {}, Discount: {}, CGST: {}, SGST: {}, Total: {}",
                orderId, subtotal, discount, cgst, sgst, total);
        return bill;
    }

    public Order updateOrder(String orderId, String status) {
        return updateOrderStatus(orderId, status);
    }

    public Order addOrder(@Valid Order order) {
        logger.info("Adding new order for user ID: {}", order.getUserId());
        Order savedOrder = orderRepository.save(order);
        logger.info("Order added successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    public Order getOrderById(String id) {
        logger.info("Fetching order with ID: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<Order> getAllOrders() {
        logger.info("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        logger.info("Found {} orders", orders.size());
        return orders;
    }

    public List<Order> getDeliveriesByDeliveryBoy(String deliveryBoyId) {
        logger.info("Fetching deliveries for delivery boy ID: {}", deliveryBoyId);
        List<Order> deliveries = orderRepository.findByDeliveryBoyId(deliveryBoyId);
        logger.info("Found {} deliveries for delivery boy ID: {}", deliveries.size(), deliveryBoyId);
        return deliveries;
    }
}