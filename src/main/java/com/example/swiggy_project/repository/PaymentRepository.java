package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Payment findByOrderId(java.lang.String orderId);
}