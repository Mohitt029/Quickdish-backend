package com.example.swiggy_project.controller;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.*;
import com.example.swiggy_project.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private CouponService couponService;

    // Restaurant Endpoints
    @PostMapping("/restaurants")
    public ResponseEntity<Restaurant> addRestaurant(@Valid @RequestBody Restaurant restaurant) {
        logger.info("Received request to add restaurant: {}", restaurant.getName());
        try {
            Restaurant saved = restaurantService.addRestaurant(restaurant);
            logger.info("Restaurant added successfully with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add restaurant: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while adding restaurant: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/restaurants/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @restaurantService.getRestaurantById(#id).ownerId == authentication.principal.id)")
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
    @GetMapping("/menus/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @menuService.getMenuById(#id).restaurantId == authentication.principal.id)")
    public ResponseEntity<FoodMenu> getMenuById(@PathVariable String id) {
        logger.info("Received request to fetch menu with ID: {}", id);
        try {
            FoodMenu menu = menuService.getMenuById(id);
            logger.info("Menu fetched successfully with ID: {}", id);
            return ResponseEntity.ok(menu);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch menu: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching menu: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/menus/{menuId}/items")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @menuService.getMenuById(#menuId).restaurantId == authentication.principal.id)")
    public ResponseEntity<MenuItem> addMenuItem(@PathVariable String menuId, @Valid @RequestBody MenuItem menuItem) {
        logger.info("Received request to add menu item to menu ID: {}", menuId);
        try {
            MenuItem saved = menuItemService.addMenuItem(menuId, menuItem);
            logger.info("Menu item added successfully with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add menu item: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add menu item: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while adding menu item: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Menu Item Endpoints
    @GetMapping("/menu-items/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @menuItemService.getMenuItemById(#id).foodMenu.restaurantId == authentication.principal.id)")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable String id) {
        logger.info("Received request to fetch menu item with ID: {}", id);
        try {
            MenuItem menuItem = menuItemService.getMenuItemById(id);
            logger.info("Menu item fetched successfully with ID: {}", id);
            return ResponseEntity.ok(menuItem);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch menu item: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching menu item: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Order Endpoints
    @PostMapping("/orders")
    public ResponseEntity<Order> addOrder(@Valid @RequestBody Order order) {
        logger.info("Received request to add order for user ID: {}", order.getUserId());
        try {
            Order saved = orderService.addOrder(order);
            logger.info("Order added successfully with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add order: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while adding order: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @orderService.getOrderById(#id).restaurantId == authentication.principal.id)")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        logger.info("Received request to fetch order with ID: {}", id);
        try {
            Order order = orderService.getOrderById(id);
            logger.info("Order fetched successfully with ID: {}", id);
            return ResponseEntity.ok(order);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch order: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching order: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        logger.info("Received request to fetch all orders");
        try {
            List<Order> orders = orderService.getAllOrders();
            logger.info("Fetched {} orders successfully", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching all orders: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Payment Endpoints
    @PostMapping("/payments")
    public ResponseEntity<Payment> addPayment(@Valid @RequestBody Payment payment) {
        logger.info("Received request to add payment for order ID: {}", payment.getOrderId());
        try {
            Payment saved = paymentService.addPayment(payment);
            logger.info("Payment added successfully with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add payment: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while adding payment: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        logger.info("Received request to fetch payment with ID: {}", id);
        try {
            Payment payment = paymentService.getPaymentById(id);
            logger.info("Payment fetched successfully with ID: {}", id);
            return ResponseEntity.ok(payment);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch payment: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching payment: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delivery Endpoints
    @PostMapping("/deliveries")
    public ResponseEntity<Delivery> addDelivery(@Valid @RequestBody Delivery delivery) {
        logger.info("Received request to add delivery for order ID: {}", delivery.getOrderId());
        try {
            Delivery saved = deliveryService.addDelivery(delivery);
            logger.info("Delivery added successfully with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add delivery: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add delivery: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while adding delivery: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/deliveries/{id}")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable String id) {
        logger.info("Received request to fetch delivery with ID: {}", id);
        try {
            Delivery delivery = deliveryService.getDeliveryById(id);
            logger.info("Delivery fetched successfully with ID: {}", id);
            return ResponseEntity.ok(delivery);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to fetch delivery: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching delivery: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/deliveries/delivery-boy/{deliveryBoyId}")
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

    @PutMapping("/orders/{orderId}/assign-delivery")
    public ResponseEntity<Order> assignDelivery(@PathVariable String orderId, @RequestBody Map<String, String> request) {
        logger.info("Received request to assign delivery for order ID: {}", orderId);
        try {
            String deliveryBoyId = request.get("deliveryBoyId");
            if (deliveryBoyId == null || deliveryBoyId.trim().isEmpty()) {
                logger.warn("Delivery boy ID is missing in the request");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Order order = orderService.assignDelivery(orderId, deliveryBoyId);
            logger.info("Delivery assigned successfully to order ID: {} with delivery boy ID: {}", orderId, deliveryBoyId);

            // Create a Delivery record
            Delivery delivery = new Delivery(orderId, deliveryBoyId, "ASSIGNED");
            deliveryService.addDelivery(delivery);
            logger.info("Delivery record created for order ID: {}", orderId);

            return ResponseEntity.ok(order);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to assign delivery: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to assign delivery: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while assigning delivery: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody Coupon coupon) {
        logger.info("Received request to create coupon with code: {}", coupon.getCode());
        try {
            Coupon created = couponService.createCoupon(coupon);
            logger.info("Coupon created successfully with ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create coupon: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error while creating coupon: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}