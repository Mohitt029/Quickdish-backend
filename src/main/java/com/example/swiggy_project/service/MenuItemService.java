package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.FoodMenu;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.repository.FoodMenuRepository;
import com.example.swiggy_project.repository.MenuItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

import java.util.List;

@Service
public class MenuItemService {
    private static final Logger logger = LoggerFactory.getLogger(MenuItemService.class);

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private FoodMenuRepository foodMenuRepository;

    public MenuItem addMenuItem(String menuId, @Valid MenuItem menuItem) {
        logger.info("Adding menu item to menu ID: {}", menuId);
        FoodMenu foodMenu = foodMenuRepository.findById(menuId)
                .orElseThrow(() -> {
                    logger.warn("Menu not found with id: {}", menuId);
                    return new ResourceNotFoundException("Menu not found with id: " + menuId);
                });
        menuItem.setFoodMenu(foodMenu);
        menuItem.setFoodMenuId(menuId);
        MenuItem saved = menuItemRepository.save(menuItem);
        logger.info("Menu item added successfully with ID: {}", saved.getId());
        return saved;
    }

    public MenuItem addMenuItemByRestaurantId(String restaurantId, @Valid MenuItem menuItem) {
        logger.info("Adding menu item for restaurant ID: {}", restaurantId);
        FoodMenu foodMenu = foodMenuRepository.findByRestaurantId(restaurantId);
        if (foodMenu == null) {
            logger.warn("No menu found for restaurant with id: {}", restaurantId);
            throw new ResourceNotFoundException("No menu found for restaurant with id: " + restaurantId);
        }
        menuItem.setFoodMenu(foodMenu);
        menuItem.setFoodMenuId(foodMenu.getId());
        MenuItem saved = menuItemRepository.save(menuItem);
        logger.info("Menu item added successfully with ID: {}", saved.getId());
        return saved;
    }

    public MenuItem getMenuItemById(String id) {
        logger.info("Retrieving menu item with ID: {}", id);
        return menuItemRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Menu item not found with id: {}", id);
                    return new ResourceNotFoundException("Menu item not found with id: " + id);
                });
    }

    public List<MenuItem> getMenuItemsByCuisine(String restaurantId, String cuisineType) {
        logger.info("Retrieving menu items for restaurant ID: {} with cuisine: {}", restaurantId, cuisineType);
        FoodMenu foodMenu = foodMenuRepository.findByRestaurantId(restaurantId);
        if (foodMenu == null) {
            logger.warn("No menu found for restaurant with id: {}", restaurantId);
            throw new ResourceNotFoundException("No menu found for restaurant with id: " + restaurantId);
        }
        List<MenuItem> items = menuItemRepository.findByFoodMenuIdAndCuisineType(foodMenu.getId(), cuisineType);
        logger.info("Found {} menu items for restaurant ID: {} with cuisine: {}", items.size(), restaurantId, cuisineType);
        return items;
    }

    public List<MenuItem> getMenuItemsByMealType(String restaurantId, String mealType) {
        logger.info("Retrieving menu items for restaurant ID: {} with meal type: {}", restaurantId, mealType);
        FoodMenu foodMenu = foodMenuRepository.findByRestaurantId(restaurantId);
        if (foodMenu == null) {
            logger.warn("No menu found for restaurant with id: {}", restaurantId);
            throw new ResourceNotFoundException("No menu found for restaurant with id: " + restaurantId);
        }
        List<MenuItem> items = menuItemRepository.findByFoodMenuIdAndMealType(foodMenu.getId(), mealType);
        logger.info("Found {} menu items for restaurant ID: {} with meal type: {}", items.size(), restaurantId, mealType);
        return items;
    }

    public MenuItem updateMenuItem(MenuItem menuItem) {
        logger.info("Updating menu item with ID: {}", menuItem.getId());
        MenuItem updated = menuItemRepository.save(menuItem);
        logger.info("Menu item updated successfully with ID: {}", updated.getId());
        return updated;
    }
}