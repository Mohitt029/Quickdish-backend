package com.example.swiggy_project.controller;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.FoodMenu;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.repository.MenuItemRepository;
import com.example.swiggy_project.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller for restaurant-related operations, such as managing menus and menu items.
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    /**
     * Adds a new menu for a specific restaurant.
     *
     * @param restaurantId the ID of the restaurant
     * @param foodMenu     the menu to add
     * @return ResponseEntity containing the saved menu or 404 if the restaurant is not found
     */
    @PostMapping("/{restaurantId}/menus")
    public ResponseEntity<FoodMenu> addMenuByRestaurantId(@PathVariable String restaurantId, @Valid @RequestBody FoodMenu foodMenu) {
        logger.info("Received request to add menu for restaurant ID: {}", restaurantId);
        try {
            FoodMenu saved = menuService.addMenuByRestaurantId(restaurantId, foodMenu);
            logger.info("Menu added successfully with ID: {} for restaurant ID: {}", saved.getId(), saved.getRestaurantId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add menu: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Adds a new menu item for a specific restaurant's menu.
     *
     * @param restaurantId the ID of the restaurant
     * @param menuItem     the menu item to add
     * @return ResponseEntity containing the saved menu item or 404 if the menu is not found
     */
    @PostMapping("/{restaurantId}/menu-items")
    public ResponseEntity<MenuItem> addMenuItemsByRestaurantId(@PathVariable String restaurantId, @Valid @RequestBody MenuItem menuItem) {
        logger.info("Adding menu item for restaurant ID: {}", restaurantId);
        FoodMenu foodMenu = menuService.getMenuByRestaurantId(restaurantId);
        if (foodMenu == null) {
            logger.warn("No menu found for restaurant ID: {}", restaurantId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        menuItem.setFoodMenu(foodMenu);
        menuItem.setFoodMenuId(foodMenu.getId());
        MenuItem saved = menuItemRepository.save(menuItem);
        logger.info("Menu item added successfully with ID: {}", saved.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}