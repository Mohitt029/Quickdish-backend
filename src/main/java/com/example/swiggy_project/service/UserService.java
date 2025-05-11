package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.model.User;
import com.example.swiggy_project.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemService menuItemService;

    public User addUser(User user) {
        List<String> validRoles = Arrays.asList("CUSTOMER", "ADMIN", "DELIVERY_BOY");
        if (!validRoles.contains(user.getRole())) {
            logger.error("Invalid role provided: {}", user.getRole());
            throw new IllegalArgumentException("Invalid role: " + user.getRole());
        }
        // Assume password hashing is handled elsewhere
        logger.info("Adding new user with username: {}", user.getUsername());
        return userRepository.save(user);
    }

    public User getUser(String userId) {
        logger.info("Retrieving user with ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });
    }

    public User updateUser(String userId, User user) {
        logger.info("Updating user with ID: {}", userId);
        User existing = getUser(userId);
        List<String> validRoles = Arrays.asList("CUSTOMER", "ADMIN", "DELIVERY_BOY");
        if (!validRoles.contains(user.getRole())) {
            logger.error("Invalid role provided: {}", user.getRole());
            throw new IllegalArgumentException("Invalid role: " + user.getRole());
        }
        existing.setUsername(user.getUsername());
        existing.setAddress(user.getAddress());
        existing.setPhoneNo(user.getPhoneNo());
        existing.setPassword(user.getPassword());
        existing.setRole(user.getRole());
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setGender(user.getGender());
        existing.setBio(user.getBio());
        // Preserve the likedMenuItems field
        if (user.getLikedMenuItems() != null) {
            existing.setLikedMenuItems(user.getLikedMenuItems());
        }
        logger.info("User updated successfully with ID: {}", userId);
        return userRepository.save(existing);
    }

    public void deleteUser(String userId) {
        logger.info("Deleting user with ID: {}", userId);
        User user = getUser(userId);
        userRepository.delete(user);
        logger.info("User deleted successfully with ID: {}", userId);
    }

    /**
     * Allows a user to like a menu item.
     *
     * @param userId the ID of the user
     * @param menuItemId the ID of the menu item to like
     * @return the updated User object
     * @throws ResourceNotFoundException if the user or menu item is not found
     */
    public User likeMenuItem(String userId, String menuItemId) {
        logger.info("User ID: {} liking menu item ID: {}", userId, menuItemId);
        User user = getUser(userId);
        MenuItem menuItem = menuItemService.getMenuItemById(menuItemId); // Validates the menu item exists
        List<String> likedItems = user.getLikedMenuItems();
        if (!likedItems.contains(menuItemId)) {
            likedItems.add(menuItemId);
            user.setLikedMenuItems(likedItems);
            logger.info("Menu item ID: {} added to liked items for user ID: {}", menuItemId, userId);
            return userRepository.save(user);
        }
        logger.info("Menu item ID: {} already liked by user ID: {}", menuItemId, userId);
        return user;
    }

    /**
     * Retrieves the list of menu items liked by a user.
     *
     * @param userId the ID of the user
     * @return the list of liked MenuItem objects
     * @throws ResourceNotFoundException if the user is not found
     */
    public List<MenuItem> getLikedMenuItems(String userId) {
        logger.info("Retrieving liked menu items for user ID: {}", userId);
        User user = getUser(userId);
        List<String> likedItemIds = user.getLikedMenuItems();
        return likedItemIds.stream()
                .map(menuItemService::getMenuItemById)
                .collect(Collectors.toList());
    }
}