package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.model.User;
import com.example.swiggy_project.repository.MenuItemRepository;
import com.example.swiggy_project.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RedisTemplate<String, List<String>> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private static final String ML_SERVICE_URL = "http://mock-ml-service/recommend";
    private static final long CACHE_EXPIRY_HOURS = 1;

    public List<MenuItem> getRecommendations(String userId) {
        logger.info("Generating recommendations for user ID: {}", userId);

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });

        // Generate cache key based on user ID and preferences (to refresh cache if preferences change)
        String cacheKey = generateCacheKey(userId, user.getFavoriteCuisines());
        List<String> cachedMenuItemIds = redisTemplate.opsForValue().get(cacheKey);

        // Return cached recommendations if available
        if (cachedMenuItemIds != null && !cachedMenuItemIds.isEmpty()) {
            logger.info("Returning cached recommendations for user ID: {}", userId);
            return fetchMenuItems(cachedMenuItemIds);
        }

        // Fetch recommendations
        List<String> recommendedMenuItemIds = getRecommendedMenuItemIds(user);

        // Cache the recommendations
        if (!recommendedMenuItemIds.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, recommendedMenuItemIds, CACHE_EXPIRY_HOURS, TimeUnit.HOURS);
            logger.info("Cached recommendations for user ID: {} with cache key: {}", userId, cacheKey);
        } else {
            logger.warn("No recommendations generated for user ID: {}", userId);
        }

        return fetchMenuItems(recommendedMenuItemIds);
    }

    private List<String> getRecommendedMenuItemIds(User user) {
        // Try ML service first
        List<String> recommendedMenuItemIds = fetchFromMLService(user);
        if (recommendedMenuItemIds != null && !recommendedMenuItemIds.isEmpty()) {
            logger.info("Successfully fetched recommendations from ML service for user ID: {}", user.getId());
            return filterByUserPreferences(user, recommendedMenuItemIds);
        }

        // Fallback to local recommendation logic
        logger.info("Falling back to local recommendation logic for user ID: {}", user.getId());
        return generateLocalRecommendations(user);
    }

    private List<String> fetchFromMLService(User user) {
        try {
            String[] response = restTemplate.postForObject(
                    ML_SERVICE_URL,
                    new RecommendationRequest(user.getFavoriteCuisines(), user.getOrderHistory()),
                    String[].class
            );
            return response != null ? Arrays.asList(response) : Collections.emptyList();
        } catch (RestClientException e) {
            logger.warn("ML service unavailable for user ID: {}. Error: {}", user.getId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> generateLocalRecommendations(User user) {
        List<String> recommendedMenuItemIds = new ArrayList<>();

        // Step 1: Recommend based on liked items
        if (user.getLikedMenuItems() != null && !user.getLikedMenuItems().isEmpty()) {
            recommendedMenuItemIds.addAll(user.getLikedMenuItems());
            logger.debug("Added liked items to recommendations for user ID: {}", user.getId());
        }

        // Step 2: Recommend based on favorite cuisines
        if (user.getFavoriteCuisines() != null && !user.getFavoriteCuisines().isEmpty()) {
            List<MenuItem> cuisineItems = menuItemRepository.findByCuisineTypeIn(user.getFavoriteCuisines());
            recommendedMenuItemIds.addAll(
                    cuisineItems.stream()
                            .map(MenuItem::getId)
                            .filter(id -> !recommendedMenuItemIds.contains(id))
                            .collect(Collectors.toList())
            );
            logger.debug("Added items from favorite cuisines for user ID: {}", user.getId());
        }

        // Step 3: Recommend popular items (based on numberOfTimesOrdered)
        if (recommendedMenuItemIds.size() < 5) { // Ensure at least 5 recommendations
            List<MenuItem> popularItems = menuItemRepository.findTop5ByOrderByNumberOfTimesOrderedDesc();
            recommendedMenuItemIds.addAll(
                    popularItems.stream()
                            .map(MenuItem::getId)
                            .filter(id -> !recommendedMenuItemIds.contains(id))
                            .collect(Collectors.toList())
            );
            logger.debug("Added popular items to recommendations for user ID: {}", user.getId());
        }

        return filterByUserPreferences(user, recommendedMenuItemIds);
    }

    private List<String> filterByUserPreferences(User user, List<String> menuItemIds) {
        if (menuItemIds.isEmpty()) {
            return menuItemIds;
        }

        // Determine user's veg/non-veg preference based on order history
        String dietaryPreference = inferDietaryPreference(user);
        if (dietaryPreference == null) {
            return menuItemIds; // No filtering if preference cannot be determined
        }

        List<MenuItem> menuItems = fetchMenuItems(menuItemIds);
        return menuItems.stream()
                .filter(item -> dietaryPreference.equals(item.getVegOrNonVeg()))
                .map(MenuItem::getId)
                .collect(Collectors.toList());
    }

    private String inferDietaryPreference(User user) {
        if (user.getOrderHistory() == null || user.getOrderHistory().isEmpty()) {
            logger.debug("No order history to infer dietary preference for user ID: {}", user.getId());
            return null;
        }

        // Fetch menu items from order history
        List<MenuItem> orderedItems = user.getOrderHistory().stream()
                .map(orderId -> {
                    try {
                        return menuItemService.getMenuItemById(orderId);
                    } catch (Exception e) {
                        logger.warn("Failed to fetch menu item for order ID: {}", orderId);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Count veg vs non-veg items
        long vegCount = orderedItems.stream()
                .filter(item -> "VEG".equals(item.getVegOrNonVeg()))
                .count();
        long totalCount = orderedItems.size();

        if (totalCount == 0) {
            return null;
        }

        // If more than 70% of ordered items are veg, prefer veg recommendations
        return (vegCount / (double) totalCount) > 0.7 ? "VEG" : "NON_VEG";
    }

    private List<MenuItem> fetchMenuItems(List<String> menuItemIds) {
        if (menuItemIds == null || menuItemIds.isEmpty()) {
            return Collections.emptyList();
        }

        return menuItemIds.stream()
                .map(id -> {
                    try {
                        return menuItemService.getMenuItemById(id);
                    } catch (ResourceNotFoundException e) {
                        logger.warn("Menu item not found with ID: {}", id);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String generateCacheKey(String userId, List<String> favoriteCuisines) {
        // Include favorite cuisines in the cache key to invalidate cache when preferences change
        String preferencesHash = favoriteCuisines != null
                ? String.valueOf(favoriteCuisines.hashCode())
                : "none";
        return "recommendations:" + userId + ":" + preferencesHash;
    }

    public void updatePreferences(String userId, List<String> favoriteCuisines) {
        logger.info("Updating preferences for user ID: {}", userId);

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });

        // Update preferences
        user.setFavoriteCuisines(favoriteCuisines != null ? favoriteCuisines : Collections.emptyList());
        userRepository.save(user);
        logger.info("Preferences updated successfully for user ID: {}", userId);

        // Invalidate cache (handle Redis failure gracefully)
        String oldCacheKey = generateCacheKey(userId, user.getFavoriteCuisines());
        try {
            redisTemplate.delete(oldCacheKey);
            logger.debug("Invalidated cache for user ID: {} with key: {}", userId, oldCacheKey);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Failed to invalidate cache due to Redis connection issue: {}", e.getMessage());
            // Continue without failing the request
        } catch (Exception e) {
            logger.warn("Unexpected error while invalidating cache: {}", e.getMessage());
            // Continue without failing the request
        }
    }

    // DTO for ML service request
    private static class RecommendationRequest {
        private List<String> favoriteCuisines;
        private List<String> orderHistory;

        public RecommendationRequest(List<String> favoriteCuisines, List<String> orderHistory) {
            this.favoriteCuisines = favoriteCuisines != null ? favoriteCuisines : Collections.emptyList();
            this.orderHistory = orderHistory != null ? orderHistory : Collections.emptyList();
        }

        public List<String> getFavoriteCuisines() {
            return favoriteCuisines;
        }

        public void setFavoriteCuisines(List<String> favoriteCuisines) {
            this.favoriteCuisines = favoriteCuisines;
        }

        public List<String> getOrderHistory() {
            return orderHistory;
        }

        public void setOrderHistory(List<String> orderHistory) {
            this.orderHistory = orderHistory;
        }
    }
}