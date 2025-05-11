package com.example.swiggy_project.service;

import jakarta.validation.Valid;
import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.Bill;
import com.example.swiggy_project.model.Order;
import com.example.swiggy_project.model.Payment;
import com.example.swiggy_project.repository.OrderRepository;
import com.example.swiggy_project.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    public Payment recordPayment(String orderId, double amount, String paymentMethod) {
        logger.info("Recording payment for order ID: {} with amount: {} and method: {}", orderId, amount, paymentMethod);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        Bill bill = orderService.getBill(orderId);
        if (Math.abs(amount - bill.getTotal()) > 0.01) {
            logger.warn("Payment amount {} does not match bill total {}", amount, bill.getTotal());
            throw new IllegalArgumentException("Payment amount " + amount + " does not match bill total " + bill.getTotal());
        }
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("SUCCESS");
        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment recorded successfully for order ID: {}", orderId);
        return savedPayment;
    }

    public Payment getPaymentById(String paymentId) {
        logger.info("Fetching payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        logger.info("Payment fetched successfully with ID: {}", paymentId);
        return payment;
    }

    public Payment addPayment(@Valid Payment payment) {
        logger.info("Adding payment for order ID: {}", payment.getOrderId());
        if (payment.getOrderId() == null || payment.getOrderId().trim().isEmpty()) {
            logger.warn("Order ID is null or empty in payment");
            throw new IllegalArgumentException("Order ID must not be null or empty");
        }
        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment added successfully with ID: {}", savedPayment.getId());
        return savedPayment;
    }

    public boolean validatePayment(@Valid String orderId, double paymentAmount) {
        logger.info("Validating payment for order ID: {} with amount: {}", orderId, paymentAmount);
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.warn("Order ID is null or empty");
            throw new IllegalArgumentException("Order ID must not be null or empty");
        }
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            logger.warn("No payment found for order ID: {}", orderId);
            throw new ResourceNotFoundException("Payment not found for order ID: " + orderId);
        }
        boolean isValid = Math.abs(payment.getAmount() - paymentAmount) < 0.01 && "SUCCESS".equals(payment.getStatus());
        logger.info("Payment validation result for order ID: {} is: {}", orderId, isValid);
        return isValid;
    }

    public Payment getPaymentByOrderId(String orderId) {
        logger.info("Fetching payment for order ID: {}", orderId);
        if (orderId == null || orderId.trim().isEmpty()) {
            logger.warn("Order ID is null or empty");
            throw new IllegalArgumentException("Order ID must not be null or empty");
        }
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            logger.warn("No payment found for order ID: {}", orderId);
            throw new ResourceNotFoundException("Payment not found for order ID: " + orderId);
        }
        logger.info("Payment fetched successfully for order ID: {}", orderId);
        return payment;
    }
}