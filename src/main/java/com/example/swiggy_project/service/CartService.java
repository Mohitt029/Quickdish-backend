package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.Cart;
import com.example.swiggy_project.model.Cart.CartItem;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MenuItemService menuItemService;

    /**
     * Updates the cart for a user by adding or updating an item.
     *
     * @param userId     the ID of the user
     * @param menuItemId the ID of the menu item to add or update
     * @param quantity   the quantity to set or add
     * @return the updated Cart object
     * @throws ResourceNotFoundException if the user or menu item is not found
     */
    public Cart updateCart(String userId, String menuItemId, int quantity) {
        logger.info("Updating cart for user ID: {}, menuItemId: {}, quantity: {}", userId, menuItemId, quantity);

        // Validate quantity
        if (quantity <= 0) {
            logger.error("Quantity must be positive: {}", quantity);
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Fetch the menu item to get its price
        MenuItem menuItem = menuItemService.getMenuItemById(menuItemId);
        double price = menuItem.getPrice();

        // Fetch or create cart for the user
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return newCart;
                });

        // Check if the menu item already exists in the cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(menuItemId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity if item exists
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(price); // Update price in case it has changed
            logger.debug("Updated quantity for menuItemId: {}, new quantity: {}", menuItemId, cartItem.getQuantity());
        } else {
            // Add new item if it doesn't exist
            CartItem newItem = new CartItem();
            newItem.setId(UUID.randomUUID().toString());
            newItem.setMenuItemId(menuItemId);
            newItem.setQuantity(quantity);
            newItem.setPrice(price);
            cart.getItems().add(newItem);
            logger.debug("Added new item to cart: menuItemId: {}, quantity: {}, price: {}", menuItemId, quantity, price);
        }

        // Save the cart
        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart updated successfully for user ID: {}", userId);
        return savedCart;
    }

    /**
     * Retrieves the cart for a user.
     *
     * @param userId the ID of the user
     * @return the Cart object
     * @throws ResourceNotFoundException if the cart is not found
     */
    public Cart getCart(String userId) {
        logger.info("Retrieving cart for user ID: {}", userId);
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Cart not found for user ID: {}", userId);
                    return new ResourceNotFoundException("Cart not found for user: " + userId);
                });
    }

    /**
     * Clears the cart for a user by removing all items.
     *
     * @param userId the ID of the user
     * @throws ResourceNotFoundException if the cart is not found
     */
    public void clearCart(String userId) {
        logger.info("Clearing cart for user ID: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Cart not found for user ID: {}", userId);
                    return new ResourceNotFoundException("Cart not found for user: " + userId);
                });
        cart.getItems().clear();
        cartRepository.save(cart);
        logger.info("Cart cleared successfully for user ID: {}", userId);
    }
}