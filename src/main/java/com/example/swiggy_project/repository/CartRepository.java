package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<Cart, String> {
    Cart findByUserId(String userId);
}