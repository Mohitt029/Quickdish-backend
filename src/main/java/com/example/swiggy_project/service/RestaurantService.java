package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.Restaurant;
import com.example.swiggy_project.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

@Service
public class RestaurantService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    public Restaurant addRestaurant(@Valid Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public Restaurant getRestaurantById(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }
}