package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.FoodMenu;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.repository.FoodMenuRepository;
import com.example.swiggy_project.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

import java.util.List;

@Service
public class MenuItemService {
    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private FoodMenuRepository foodMenuRepository;

    public MenuItem addMenuItem(String menuId, @Valid MenuItem menuItem) {
        FoodMenu foodMenu = foodMenuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + menuId));
        menuItem.setFoodMenu(foodMenu);
        menuItem.setFoodMenuId(menuId);
        return menuItemRepository.save(menuItem);
    }

    public MenuItem addMenuItemByRestaurantId(String restaurantId, @Valid MenuItem menuItem) {
        FoodMenu foodMenu = foodMenuRepository.findByRestaurantId(restaurantId);
        if (foodMenu == null) {
            throw new ResourceNotFoundException("No menu found for restaurant with id: " + restaurantId);
        }
        menuItem.setFoodMenu(foodMenu);
        menuItem.setFoodMenuId(foodMenu.getId());
        return menuItemRepository.save(menuItem);
    }

    public MenuItem getMenuItemById(String id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
    }

    public List<MenuItem> getMenuItemsByCuisine(String restaurantId, String cuisineType) {
        FoodMenu foodMenu = foodMenuRepository.findByRestaurantId(restaurantId);
        if (foodMenu == null) {
            throw new ResourceNotFoundException("No menu found for restaurant with id: " + restaurantId);
        }
        return menuItemRepository.findByFoodMenuIdAndCuisineType(foodMenu.getId(), cuisineType);
    }

    public List<MenuItem> getMenuItemsByMealType(String restaurantId, String mealType) {
        FoodMenu foodMenu = foodMenuRepository.findByRestaurantId(restaurantId);
        if (foodMenu == null) {
            throw new ResourceNotFoundException("No menu found for restaurant with id: " + restaurantId);
        }
        return menuItemRepository.findByFoodMenuIdAndMealType(foodMenu.getId(), mealType);
    }
}