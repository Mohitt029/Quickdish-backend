package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
    List<Restaurant> findByRatingAndLocation(double minRating, double[] location, double maxDistance);

    List<Restaurant> findByNameContainingIgnoreCase(String name);

    List<Restaurant> findByAddressContainingIgnoreCase(String city);
}