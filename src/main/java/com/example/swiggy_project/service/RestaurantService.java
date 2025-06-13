package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.Restaurant;
import com.example.swiggy_project.model.User;
import com.example.swiggy_project.repository.RestaurantRepository;
import com.example.swiggy_project.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.geo.Point; // Import Point
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.util.List;

@Service
public class RestaurantService {
    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    // Earth radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    public Restaurant addRestaurant(@Valid Restaurant restaurant) {
        logger.info("Adding new restaurant: {}", restaurant.getName());
        // Validate owner exists and has ROLE_RESTAURANT
        User owner = userRepository.findById(restaurant.getOwnerId())
                .orElseThrow(() -> {
                    logger.warn("Owner not found with ID: {}", restaurant.getOwnerId());
                    return new ResourceNotFoundException("Owner not found with ID: " + restaurant.getOwnerId());
                });
        if (!"ROLE_RESTAURANT".equals(owner.getRole())) {
            logger.error("User with ID: {} does not have ROLE_RESTAURANT", restaurant.getOwnerId());
            throw new IllegalArgumentException("Owner must have ROLE_RESTAURANT");
        }
        Restaurant saved = restaurantRepository.save(restaurant);
        logger.info("Restaurant added successfully with ID: {}", saved.getId());
        return saved;
    }

    public Restaurant getRestaurantById(String id) {
        logger.info("Retrieving restaurant with ID: {}", id);
        return restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found with ID: {}", id);
                    return new ResourceNotFoundException("Restaurant not found with ID: " + id);
                });
    }

    public List<Restaurant> getAllRestaurants() {
        logger.info("Retrieving all restaurants");
        List<Restaurant> restaurants = restaurantRepository.findAll();
        logger.info("Found {} restaurants", restaurants.size());
        return restaurants;
    }

    public List<Restaurant> getRestaurantsByName(String name) {
        logger.info("Searching restaurants by name: {}", name);
        List<Restaurant> restaurants = restaurantRepository.findByNameContainingIgnoreCase(name);
        logger.info("Found {} restaurants with name containing: {}", restaurants.size(), name);
        return restaurants;
    }

    public List<Restaurant> getRestaurantsByCity(String city) {
        logger.info("Searching restaurants by city: {}", city);
        List<Restaurant> restaurants = restaurantRepository.findByAddressContainingIgnoreCase(city);
        logger.info("Found {} restaurants in city: {}", restaurants.size(), city);
        return restaurants;
    }

    public List<Restaurant> findRestaurantsByRatingAndProximity(String userId, double minRating, double maxDistanceKm) {
        logger.info("Finding restaurants with rating >= {} and within {} km for user ID: {}", minRating, maxDistanceKm, userId);

        // Validate rating
        if (minRating < 0 || minRating > 5) {
            logger.error("Invalid rating value: {}. Must be between 0 and 5", minRating);
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        // Validate distance
        if (maxDistanceKm <= 0) {
            logger.error("Invalid distance value: {}. Must be greater than 0", maxDistanceKm);
            throw new IllegalArgumentException("Distance must be greater than 0");
        }

        // Fetch user to get their location
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
        if (user.getLocation() == null || user.getLocation().length != 2) {
            logger.warn("User location not set for user ID: {}", userId);
            throw new IllegalArgumentException("User location not set");
        }

        // User's location [longitude, latitude]
        double[] userLocation = user.getLocation();
        double userLongitude = userLocation[0];
        double userLatitude = userLocation[1];

        // Convert distance from kilometers to radians (MongoDB uses radians for $nearSphere)
        double maxDistanceRadians = maxDistanceKm / EARTH_RADIUS_KM;

        // Create a geospatial query
        Query query = new Query();

        // Add rating criteria: rating >= minRating
        query.addCriteria(Criteria.where("rating").gte(minRating));

        // Add geospatial criteria: restaurants within maxDistanceKm
        // Use Point for nearSphere
        Point userPoint = new Point(userLongitude, userLatitude); // longitude, latitude
        query.addCriteria(Criteria.where("location")
                .nearSphere(userPoint)
                .maxDistance(maxDistanceRadians));

        List<Restaurant> restaurants = mongoTemplate.find(query, Restaurant.class);
        logger.info("Found {} restaurants with rating >= {} and within {} km for user ID: {}",
                restaurants.size(), minRating, maxDistanceKm, userId);
        return restaurants;
    }
}