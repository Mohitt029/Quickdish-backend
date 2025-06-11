package com.example.swiggy_project.repository;

import com.example.swiggy_project.model.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
    @Query("{ 'rating': { $gt: ?0 }, 'location': { $geoWithin: { $centerSphere: [ ?1, ?2 ] } } }")
    List<Restaurant> findByRatingAndLocation(double minRating, double[] center, double radiusInRadians);
}