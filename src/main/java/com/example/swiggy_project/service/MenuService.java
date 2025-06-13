package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.FoodMenu;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.model.Restaurant;
import com.example.swiggy_project.repository.FoodMenuRepository;
import com.example.swiggy_project.repository.MenuItemRepository;
import com.example.swiggy_project.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Service class for managing menu-related operations.
 */
@Service
public class MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    @Autowired
    private FoodMenuRepository foodMenuRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository; // Add this dependency

    /**
     * Adds a new menu for a specific restaurant.
     *
     * @param restaurantId the ID of the restaurant
     * @param foodMenu     the menu to add
     * @return the saved FoodMenu object
     * @throws ResourceNotFoundException if the restaurant is not found
     * @throws IllegalArgumentException  if the restaurantId is null or empty
     */
    public FoodMenu addMenuByRestaurantId(String restaurantId, @Valid FoodMenu foodMenu) {
        if (!StringUtils.hasText(restaurantId)) {
            logger.error("Restaurant ID is null or empty");
            throw new IllegalArgumentException("Restaurant ID cannot be null or empty");
        }
        logger.info("Adding menu for restaurant ID: {}", restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found with id: {}", restaurantId);
                    return new ResourceNotFoundException("Restaurant not found with id: " + restaurantId);
                });
        foodMenu.setRestaurant(restaurant);
        foodMenu.setRestaurantId(restaurantId);
        FoodMenu saved = foodMenuRepository.save(foodMenu);
        logger.info("Menu added successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Retrieves a menu by its ID.
     *
     * @param id the ID of the menu
     * @return the FoodMenu object
     * @throws ResourceNotFoundException if the menu is not found
     * @throws IllegalArgumentException  if the ID is null or empty
     */
    public FoodMenu getMenuById(String id) {
        if (!StringUtils.hasText(id)) {
            logger.error("Menu ID is null or empty");
            throw new IllegalArgumentException("Menu ID cannot be null or empty");
        }
        logger.info("Retrieving menu with ID: {}", id);
        return foodMenuRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Menu not found with id: {}", id);
                    return new ResourceNotFoundException("Menu not found with id: " + id);
                });
    }

    /**
     * Retrieves the list of menu items for a restaurant by its ID.
     *
     * @param restaurantId the ID of the restaurant
     * @return the list of MenuItem objects
     * @throws ResourceNotFoundException if the menu is not found
     * @throws IllegalArgumentException  if the restaurantId is null or empty
     */
    public List<MenuItem> getMenuItemsByRestaurantId(String restaurantId) {
        if (!StringUtils.hasText(restaurantId)) {
            logger.error("Restaurant ID is null or empty");
            throw new IllegalArgumentException("Restaurant ID cannot be null or empty");
        }
        logger.info("Retrieving menu items for restaurant ID: {}", restaurantId);
        FoodMenu foodMenu = foodMenuRepository.findByRestaurantId(restaurantId);
        if (foodMenu == null) {
            logger.warn("Menu not found for restaurant ID: {}", restaurantId);
            throw new ResourceNotFoundException("Menu not found for restaurant ID: " + restaurantId);
        }
        // Fetch menu items directly from the menu_items collection
        List<MenuItem> menuItems = menuItemRepository.findByFoodMenuId(foodMenu.getId());
        logger.info("Found {} menu items for restaurant ID: {}", menuItems.size(), restaurantId);
        return menuItems;
    }
}