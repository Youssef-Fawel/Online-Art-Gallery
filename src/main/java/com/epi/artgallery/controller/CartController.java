package com.epi.artgallery.controller;

import com.epi.artgallery.model.Artwork;
import com.epi.artgallery.model.CartItem;
import com.epi.artgallery.model.User;
import com.epi.artgallery.service.ArtworkService;
import com.epi.artgallery.service.CartService;
import com.epi.artgallery.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ArtworkService artworkService;

    @Autowired
    public CartController(CartService cartService, UserService userService, ArtworkService artworkService) {
        this.cartService = cartService;
        this.userService = userService;
        this.artworkService = artworkService;
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        // Get current user if authenticated
        User user = getCurrentUser();

        // Get cart items
        List<CartItem> cartItems = cartService.getCartItems(user, session);

        // Calculate total based on CartItem
        // Using the getSubtotal() method from CartItem
        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("cartCount", cartItems.size());

        return "cart/view";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam("artworkId") Long artworkId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user if authenticated
            User user = getCurrentUser();

            // Get the artwork
            Optional<Artwork> artworkOpt = artworkService.findById(artworkId);
            if (artworkOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Artwork not found");
                return ResponseEntity.badRequest().body(response);
            }

            // Add to cart
            cartService.addToCart(user, artworkOpt.get(), quantity, session);

            // Get updated cart count
            int cartCount = cartService.getCartCount(user, session);

            response.put("success", true);
            response.put("message", "Added to cart successfully");
            response.put("cartCount", cartCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding to cart: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@RequestParam("itemId") Long itemId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user if authenticated
            User user = getCurrentUser();

            // Remove from cart - using the correct method name
            cartService.removeCartItem(user, itemId, session);

            // Get updated cart count
            int cartCount = cartService.getCartCount(user, session);

            response.put("success", true);
            response.put("message", "Removed from cart successfully");
            response.put("cartCount", cartCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error removing from cart: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @RequestParam("itemId") Long itemId,
            @RequestParam("quantity") int quantity,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user if authenticated
            User user = getCurrentUser();

            // Update cart item
            cartService.updateCartItem(user, itemId, quantity, session);

            // Get updated cart count
            int cartCount = cartService.getCartCount(user, session);

            // Calculate new total
            BigDecimal total = cartService.calculateCartTotal(user, session);

            response.put("success", true);
            response.put("message", "Cart updated successfully");
            response.put("cartCount", cartCount);
            response.put("total", total);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating cart: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user if authenticated
            User user = getCurrentUser();

            // Clear cart
            cartService.clearCart(user, session);

            response.put("success", true);
            response.put("message", "Cart cleared successfully");
            response.put("cartCount", 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error clearing cart: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if ("anonymousUser".equals(username)) {
            return null;
        }

        return userService.findByUsername(username).orElse(null);
    }
}
