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
        return couponRepository.save(coupon);
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
        logger.info("Coupon updated successfully with ID: {}", id);
        return couponRepository.save(existing);
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
     * @throws ResourceNotFoundException if the order is not found
     * @throws IllegalArgumentException if the coupon is invalid or inactive
     */
    public Order applyCoupon(String orderId, String couponCode) {
        logger.info("Applying coupon with code: {} to order ID: {}", couponCode, orderId);
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> {
                    logger.warn("Invalid coupon code: {}", couponCode);
                    return new IllegalArgumentException("Invalid coupon code: " + couponCode);
                });
        if (!coupon.isActive()) {
            logger.warn("Coupon is not active: {}", couponCode);
            throw new IllegalArgumentException("Coupon is not active: " + couponCode);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found: {}", orderId);
                    return new ResourceNotFoundException("Order not found: " + orderId);
                });
        double discount = coupon.getDiscount();
        double newTotal = order.getTotalAmount() - discount; // Changed to flat discount (as per Coupon model)
        if (newTotal < 0) {
            newTotal = 0; // Prevent negative total
        }
        order.setTotalAmount(newTotal);
        order.setCouponCode(couponCode);
        logger.info("Coupon applied successfully to order ID: {}", orderId);
        return orderRepository.save(order);
    }
}