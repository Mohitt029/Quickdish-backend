package com.example.swiggy_project.service;

import com.example.swiggy_project.exception.ResourceNotFoundException;
import com.example.swiggy_project.model.Cart;
import com.example.swiggy_project.model.MenuItem;
import com.example.swiggy_project.repository.CartRepository;
import com.example.swiggy_project.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

import java.util.ArrayList;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    public Cart updateCart(@Valid String userId, String menuItemId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
        }

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));

        Cart.CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(menuItemId))
                .findFirst()
                .orElse(null);

        if (cartItem == null && quantity > 0) {
            cartItem = new Cart.CartItem();
            cartItem.setMenuItemId(menuItemId);
            cartItem.setPrice(menuItem.getPrice());
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
        } else if (cartItem != null) {
            if (quantity <= 0) {
                cart.getItems().remove(cartItem);
            } else {
                cartItem.setQuantity(quantity);
            }
        }

        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            cartRepository.delete(cart);
        }
    }

    public Cart getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
        }
        return cart;
    }
}