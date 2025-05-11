package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CouponRepository extends MongoRepository<Coupon, String> {
    Optional<Coupon> findByCode(String code);
}