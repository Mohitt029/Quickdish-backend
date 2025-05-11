package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.FoodMenu;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FoodMenuRepository extends MongoRepository<FoodMenu, String> {
    FoodMenu findByRestaurantId(String restaurantId);
}