package com.example.swiggy_project.controller;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.*;
import com.example.swiggy_project.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.List;

/**
 * Controller for user-related operations, including user management, cart, orders, payments, menu item preferences, and coupon management.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private DeliveryService deliveryService; // Added to fetch deliveries

    // User Endpoints
    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        logger.info("Received request to add user: {}", user.getUsername());
        try {
            User savedUser = userService.addUser(user);
            logger.info("User added successfully with ID: {}", savedUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add user: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while adding user: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        logger.info("Received request to delete user with ID: {}", userId);
        try {
            userService.deleteUser(userId);
            logger.info("User deleted successfully with ID: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to delete user: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while deleting user: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @Valid @RequestBody User user) {
        logger.info("Received request to update user with ID: {}", userId);
        try {
            User updatedUser = userService.updateUser(userId, user);
            logger.info("User updated successfully with ID: {}", userId);
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update user: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update user: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while updating user: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        logger.info("Received request to fetch user with ID: {}", userId);
        try {
            User user = userService.getUser(userId);
            logger.info("User fetched successfully with ID: {}", userId);
            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch user: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching user: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Restaurant Endpoints
    @GetMapping("/restaurants/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable String id) {
        logger.info("Received request to fetch restaurant with ID: {}", id);
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(id);
            logger.info("Restaurant fetched successfully with ID: {}", id);
            return ResponseEntity.ok(restaurant);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch restaurant: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching restaurant: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Menu Endpoints
    @GetMapping("/menus/{restaurantId}")
    public ResponseEntity<FoodMenu> getMenuByRestaurantId(@PathVariable String restaurantId) {
        logger.info("Received request to fetch menu for restaurant ID: {}", restaurantId);
        try {
            FoodMenu menu = menuService.getMenuByRestaurantId(restaurantId);
            logger.info("Menu fetched successfully for restaurant ID: {}", restaurantId);
            return ResponseEntity.ok(menu);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch menu: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching menu: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/menu-items/{restaurantId}/cuisine/{cuisineType}")
    public ResponseEntity<List<MenuItem>> getMenuItemsByCuisine(@PathVariable String restaurantId, @PathVariable String cuisineType) {
        logger.info("Received request to fetch menu items for restaurant ID: {} with cuisine: {}", restaurantId, cuisineType);
        try {
            List<MenuItem> items = menuItemService.getMenuItemsByCuisine(restaurantId, cuisineType);
            logger.info("Menu items fetched successfully for restaurant ID: {} with cuisine: {}", restaurantId, cuisineType);
            return ResponseEntity.ok(items);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch menu items: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching menu items: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/menu-items/{restaurantId}/meal/{mealType}")
    public ResponseEntity<List<MenuItem>> getMenuItemsByMealType(@PathVariable String restaurantId, @PathVariable String mealType) {
        logger.info("Received request to fetch menu items for restaurant ID: {} with meal type: {}", restaurantId, mealType);
        try {
            List<MenuItem> items = menuItemService.getMenuItemsByMealType(restaurantId, mealType);
            logger.info("Menu items fetched successfully for restaurant ID: {} with meal type: {}", restaurantId, mealType);
            return ResponseEntity.ok(items);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch menu items: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching menu items: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Cart Endpoints
    @PutMapping("/cart")
    public ResponseEntity<Cart> updateCart(@RequestParam @Valid String userId, @RequestParam String menuItemId, @RequestParam int quantity) {
        logger.info("Received request to update cart for user ID: {} with menu item ID: {} and quantity: {}", userId, menuItemId, quantity);
        if (quantity < 0) {
            logger.warn("Invalid quantity provided: {}", quantity);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Cart cart = cartService.updateCart(userId, menuItemId, quantity);
            logger.info("Cart updated successfully for user ID: {}", userId);
            return ResponseEntity.ok(cart);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update cart: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while updating cart: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Void> clearCart(@RequestParam @Valid String userId) {
        logger.info("Received request to clear cart for user ID: {}", userId);
        try {
            cartService.clearCart(userId);
            logger.info("Cart cleared successfully for user ID: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to clear cart: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while clearing cart: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cart")
    public ResponseEntity<Cart> getCart(@RequestParam @Valid String userId) {
        logger.info("Received request to fetch cart for user ID: {}", userId);
        try {
            Cart cart = cartService.getCart(userId);
            logger.info("Cart fetched successfully for user ID: {}", userId);
            return ResponseEntity.ok(cart);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch cart: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching cart: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Order Endpoints
    @PutMapping("/orders")
    public ResponseEntity<Order> placeOrder(@RequestParam @Valid String userId, @RequestParam String restaurantId) {
        logger.info("Received request to place order for user ID: {} from restaurant ID: {}", userId, restaurantId);
        try {
            Order order = orderService.placeOrder(userId, restaurantId);
            logger.info("Order placed successfully with ID: {}", order.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to place order: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            logger.warn("Failed to place order: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while placing order: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        logger.info("Received request to update status of order ID: {} to {}", orderId, status);
        List<String> validStatuses = Arrays.asList("PLACED", "PREPARING", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(status)) {
            logger.warn("Invalid order status provided: {}", status);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Order order = orderService.updateOrder(orderId, status);
            logger.info("Order status updated successfully for order ID: {}", orderId);
            return ResponseEntity.ok(order);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update order status: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while updating order status: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOrdersByUser(@RequestParam @Valid String userId) {
        logger.info("Received request to fetch orders for user ID: {}", userId);
        try {
            List<Order> orders = orderService.getOrdersByUser(userId);
            logger.info("Orders fetched successfully for user ID: {}", userId);
            return ResponseEntity.ok(orders);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch orders: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching orders: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orders/restaurant/{restaurantId}")
    public ResponseEntity<List<Order>> getOrdersByRestaurant(@RequestParam @Valid String userId, @PathVariable String restaurantId) {
        logger.info("Received request to fetch orders for user ID: {} from restaurant ID: {}", userId, restaurantId);
        try {
            List<Order> orders = orderService.getOrdersByRestaurant(userId, restaurantId);
            logger.info("Orders fetched successfully for user ID: {} from restaurant ID: {}", userId, restaurantId);
            return ResponseEntity.ok(orders);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch orders: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching orders: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<String> getOrderStatus(@PathVariable String orderId) {
        logger.info("Received request to fetch status for order ID: {}", orderId);
        try {
            String status = orderService.getOrderStatus(orderId);
            logger.info("Order status fetched successfully for order ID: {}", orderId);
            return ResponseEntity.ok(status);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch order status: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching order status: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/deliveries/{deliveryBoyId}")
    public ResponseEntity<List<Delivery>> getDeliveriesByDeliveryBoy(@PathVariable String deliveryBoyId) {
        logger.info("Received request to fetch deliveries for delivery boy ID: {}", deliveryBoyId);
        try {
            List<Delivery> deliveries = deliveryService.getDeliveriesByDeliveryBoy(deliveryBoyId);
            logger.info("Fetched {} deliveries for delivery boy ID: {}", deliveries.size(), deliveryBoyId);
            return ResponseEntity.ok(deliveries);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to fetch deliveries: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching deliveries: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Coupon Endpoints
    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody Coupon coupon) {
        logger.info("Received request to create coupon with code: {}", coupon.getCode());
        try {
            Coupon createdCoupon = couponService.createCoupon(coupon);
            logger.info("Coupon created successfully with ID: {}", createdCoupon.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCoupon);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create coupon: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while creating coupon: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/coupons/{code}")
    public ResponseEntity<Coupon> getCouponByCode(@PathVariable String code) {
        logger.info("Received request to fetch coupon with code: {}", code);
        try {
            Coupon coupon = couponService.getCouponByCode(code);
            logger.info("Coupon fetched successfully with code: {}", code);
            return ResponseEntity.ok(coupon);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch coupon: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching coupon: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable String id, @Valid @RequestBody Coupon coupon) {
        logger.info("Received request to update coupon with ID: {}", id);
        try {
            Coupon updatedCoupon = couponService.updateCoupon(id, coupon);
            logger.info("Coupon updated successfully with ID: {}", id);
            return ResponseEntity.ok(updatedCoupon);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to update coupon: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update coupon: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while updating coupon: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable String id) {
        logger.info("Received request to delete coupon with ID: {}", id);
        try {
            couponService.deleteCoupon(id);
            logger.info("Coupon deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to delete coupon: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while deleting coupon: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/orders/{orderId}/coupon")
    public ResponseEntity<Order> applyCoupon(@PathVariable String orderId, @RequestParam String couponCode) {
        logger.info("Received request to apply coupon {} to order ID: {}", couponCode, orderId);
        try {
            Order order = couponService.applyCoupon(orderId, couponCode);
            logger.info("Coupon applied successfully to order ID: {}", orderId);
            return ResponseEntity.ok(order);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to apply coupon: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to apply coupon: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while applying coupon: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Bill Endpoint
    @GetMapping("/orders/{orderId}/bill")
    public ResponseEntity<Bill> getBill(@PathVariable String orderId) {
        logger.info("Received request to fetch bill for order ID: {}", orderId);
        try {
            Bill bill = orderService.getBill(orderId);
            logger.info("Bill fetched successfully for order ID: {}", orderId);
            return ResponseEntity.ok(bill);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch bill: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching bill: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Payment Endpoints
    @PostMapping("/payments")
    public ResponseEntity<Payment> recordPayment(
            @RequestParam String orderId,
            @RequestParam double amount,
            @RequestParam String paymentMethod) {
        logger.info("Received request to record payment for order ID: {} with amount: {} and method: {}", orderId, amount, paymentMethod);
        if (amount <= 0) {
            logger.warn("Invalid payment amount provided: {}", amount);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Payment payment = paymentService.recordPayment(orderId, amount, paymentMethod);
            logger.info("Payment recorded successfully for order ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to record payment: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to record payment: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while recording payment: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String paymentId) {
        logger.info("Received request to fetch payment with ID: {}", paymentId);
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            logger.info("Payment fetched successfully with ID: {}", paymentId);
            return ResponseEntity.ok(payment);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch payment: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching payment: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payments/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId) {
        logger.info("Received request to fetch payment for order ID: {}", orderId);
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            logger.info("Payment fetched successfully for order ID: {}", orderId);
            return ResponseEntity.ok(payment);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch payment: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching payment: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/payments/validate")
    public ResponseEntity<Boolean> validatePayment(@RequestParam @Valid String orderId, @RequestParam double paymentAmount) {
        logger.info("Received request to validate payment for order ID: {} with amount: {}", orderId, paymentAmount);
        if (paymentAmount <= 0) {
            logger.warn("Invalid payment amount provided: {}", paymentAmount);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            boolean isValid = paymentService.validatePayment(orderId, paymentAmount);
            logger.info("Payment validation result for order ID: {} is: {}", orderId, isValid);
            return ResponseEntity.ok(isValid);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to validate payment: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while validating payment: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Menu Item Like Endpoints
    @PostMapping("/{userId}/like-menu-item")
    public ResponseEntity<User> likeMenuItem(@PathVariable String userId, @RequestParam String menuItemId) {
        logger.info("Received request for user ID: {} to like menu item ID: {}", userId, menuItemId);
        try {
            User user = userService.likeMenuItem(userId, menuItemId);
            logger.info("User ID: {} successfully liked menu item ID: {}", userId, menuItemId);
            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to like menu item: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while liking menu item: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}/liked-menu-items")
    public ResponseEntity<List<MenuItem>> getLikedMenuItems(@PathVariable String userId) {
        logger.info("Received request to fetch liked menu items for user ID: {}", userId);
        try {
            List<MenuItem> likedItems = userService.getLikedMenuItems(userId);
            logger.info("Liked menu items fetched successfully for user ID: {}", userId);
            return ResponseEntity.ok(likedItems);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch liked menu items: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching liked menu items: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}