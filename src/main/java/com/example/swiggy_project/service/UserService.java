package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.model.User;
import com.example.swiggy_project.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        return user;
    }

    public User addUser(User user) {
        List<String> validRoles = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_RESTAURANT");
        if (!validRoles.contains(user.getRole())) {
            logger.error("Invalid role provided: {}", user.getRole());
            throw new IllegalArgumentException("Invalid role: " + user.getRole() + ". Valid roles are: " + validRoles);
        }
        // Validate email uniqueness
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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

    public User findByEmail(String email) {
        logger.info("Retrieving user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElse(null); // Return null if not found, handled in AuthController
    }

    public User updateUser(String userId, User user) {
        logger.info("Updating user with ID: {}", userId);
        User existing = getUser(userId);
        List<String> validRoles = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_RESTAURANT");
        if (!validRoles.contains(user.getRole())) {
            logger.error("Invalid role provided: {}", user.getRole());
            throw new IllegalArgumentException("Invalid role: " + user.getRole() + ". Valid roles are: " + validRoles);
        }
        // Validate email uniqueness if changed
        if (!existing.getEmail().equals(user.getEmail()) && userRepository.findByEmail(user.getEmail()).isPresent()) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        existing.setUsername(user.getUsername());
        existing.setAddress(user.getAddress());
        existing.setPhoneNo(user.getPhoneNo());
        existing.setEmail(user.getEmail());
        // Only update password if provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        existing.setRole(user.getRole());
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setGender(user.getGender());
        existing.setBio(user.getBio());
        // Preserve the likedMenuItems and location fields
        if (user.getLikedMenuItems() != null) {
            existing.setLikedMenuItems(user.getLikedMenuItems());
        }
        if (user.getLocation() != null) {
            existing.setLocation(user.getLocation());
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