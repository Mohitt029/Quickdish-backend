package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.Coupon;
import com.example.swiggy_project.model.Order;
import com.example.swiggy_project.repository.CouponRepository;
import com.example.swiggy_project.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CouponService {
    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Creates a new coupon.
     *
     * @param coupon the coupon to create
     * @return the created coupon
     * @throws IllegalArgumentException if the coupon code already exists
     */
    public Coupon createCoupon(Coupon coupon) {
        logger.info("Creating coupon with code: {}", coupon.getCode());
        if (couponRepository.findByCode(coupon.getCode()).isPresent()) {
            logger.warn("Coupon code already exists: {}", coupon.getCode());
            throw new IllegalArgumentException("Coupon code already exists: " + coupon.getCode());
        }
        Coupon savedCoupon = couponRepository.save(coupon);
        logger.info("Coupon created successfully with ID: {}", savedCoupon.getId());
        return savedCoupon;
    }

    /**
     * Retrieves a coupon by its code.
     *
     * @param code the code of the coupon
     * @return the coupon
     * @throws ResourceNotFoundException if the coupon is not found
     */
    public Coupon getCouponByCode(String code) {
        logger.info("Retrieving coupon with code: {}", code);
        return couponRepository.findByCode(code)
                .orElseThrow(() -> {
                    logger.warn("Coupon not found with code: {}", code);
                    return new ResourceNotFoundException("Coupon not found with code: " + code);
                });
    }

    /**
     * Updates an existing coupon.
     *
     * @param id the ID of the coupon to update
     * @param coupon the updated coupon details
     * @return the updated coupon
     * @throws ResourceNotFoundException if the coupon is not found
     */
    public Coupon updateCoupon(String id, Coupon coupon) {
        logger.info("Updating coupon with ID: {}", id);
        Coupon existing = couponRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Coupon not found with ID: {}", id);
                    return new ResourceNotFoundException("Coupon not found with ID: " + id);
                });
        // Check if the new code is already taken by another coupon
        couponRepository.findByCode(coupon.getCode())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        logger.warn("Coupon code already exists: {}", coupon.getCode());
                        throw new IllegalArgumentException("Coupon code already exists: " + coupon.getCode());
                    }
                });
        existing.setCode(coupon.getCode());
        existing.setDiscount(coupon.getDiscount());
        existing.setActive(coupon.isActive());
        Coupon updatedCoupon = couponRepository.save(existing);
        logger.info("Coupon updated successfully with ID: {}", updatedCoupon.getId());
        return updatedCoupon;
    }

    /**
     * Deletes a coupon by its ID.
     *
     * @param id the ID of the coupon to delete
     * @throws ResourceNotFoundException if the coupon is not found
     */
    public void deleteCoupon(String id) {
        logger.info("Deleting coupon with ID: {}", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Coupon not found with ID: {}", id);
                    return new ResourceNotFoundException("Coupon not found with ID: " + id);
                });
        couponRepository.delete(coupon);
        logger.info("Coupon deleted successfully with ID: {}", id);
    }

    /**
     * Applies a coupon to an order.
     *
     * @param orderId the ID of the order
     * @param couponCode the code of the coupon to apply
     * @return the updated order
     * @throws ResourceNotFoundException if the order or coupon is not found
     * @throws IllegalArgumentException if the coupon is invalid, inactive, or the order status is not PLACED
     */
    public Order applyCoupon(String orderId, String couponCode) {
        logger.info("Applying coupon with code: {} to order ID: {}", couponCode, orderId);

        // Fetch and validate the coupon
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> {
                    logger.warn("Invalid coupon code: {}", couponCode);
                    return new ResourceNotFoundException("Invalid coupon code: " + couponCode);
                });

        if (!coupon.isActive()) {
            logger.warn("Coupon is not active: {}", couponCode);
            throw new IllegalArgumentException("Coupon is not active: " + couponCode);
        }

        // Fetch and validate the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Order not found with ID: " + orderId);
                });

        // Check if the order is in a state where a coupon can be applied
        if (!"PLACED".equals(order.getStatus())) {
            logger.warn("Cannot apply coupon to order with status: {}", order.getStatus());
            throw new IllegalStateException("Cannot apply coupon to an order with status: " + order.getStatus());
        }

        // Check if a coupon has already been applied
        if (order.getCouponCode() != null) {
            logger.warn("A coupon has already been applied to order ID: {}", orderId);
            throw new IllegalStateException("A coupon has already been applied to this order");
        }

        // Apply the discount as a percentage
        double originalTotal = order.getTotalAmount();
        double discountPercentage = coupon.getDiscount(); // e.g., 10 for 10%
        double discountAmount = (discountPercentage / 100) * originalTotal;
        double newTotal = originalTotal - discountAmount;

        // Ensure the total doesn't go negative
        if (newTotal < 0) {
            logger.warn("Discount would result in a negative total. Setting total to 0 for order ID: {}", orderId);
            newTotal = 0;
        }

        // Log the discount details
        logger.info("Applying {}% discount to order ID: {}. Original total: {}, Discount amount: {}, New total: {}",
                discountPercentage, orderId, originalTotal, discountAmount, newTotal);

        // Update the order
        order.setTotalAmount(newTotal);
        order.setCouponCode(couponCode);
        Order updatedOrder = orderRepository.save(order);

        logger.info("Coupon applied successfully to order ID: {}", orderId);
        return updatedOrder;
    }
}