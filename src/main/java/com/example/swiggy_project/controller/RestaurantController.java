package com.example.swiggy_project.controller;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.FoodMenu;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.service.MenuItemService;
import com.example.swiggy_project.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/restaurants")
@PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuItemService menuItemService;

    @PostMapping("/{restaurantId}/menus")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and #restaurantId == authentication.principal.id)")
    public ResponseEntity<FoodMenu> addMenuByRestaurantId(@PathVariable String restaurantId, @Valid @RequestBody FoodMenu foodMenu) {
        logger.info("Received request to add menu for restaurant ID: {}", restaurantId);
        try {
            FoodMenu saved = menuService.addMenuByRestaurantId(restaurantId, foodMenu);
            logger.info("Menu added successfully with ID: {} for restaurant ID: {}", saved.getId(), saved.getRestaurantId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add menu: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while adding menu: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{restaurantId}/menu-items")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and #restaurantId == authentication.principal.id)")
    public ResponseEntity<MenuItem> addMenuItemsByRestaurantId(@PathVariable String restaurantId, @Valid @RequestBody MenuItem menuItem) {
        logger.info("Adding menu item for restaurant ID: {}", restaurantId);
        try {
            MenuItem saved = menuItemService.addMenuItemByRestaurantId(restaurantId, menuItem);
            logger.info("Menu item added successfully with ID: {}", saved.getId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add menu item: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error while adding menu item: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}