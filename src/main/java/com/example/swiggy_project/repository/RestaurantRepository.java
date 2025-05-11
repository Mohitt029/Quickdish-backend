package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
}