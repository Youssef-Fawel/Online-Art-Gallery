package com.epi.artgallery.service;

import com.epi.artgallery.model.Artwork;
import com.epi.artgallery.model.CartItem;
import com.epi.artgallery.model.User;
import com.epi.artgallery.repository.CartItemRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Get cart items - from database if user is logged in, otherwise from session
     */
    public List<CartItem> getCartItems(User user, HttpSession session) {
        if (user != null) {
            return cartItemRepository.findByUser(user);
        } else {
            return getCartFromSession(session);
        }
    }

    /**
     * Get cart count - from database if user is logged in, otherwise from session
     */
    public int getCartCount(User user, HttpSession session) {
        if (user != null) {
            return cartItemRepository.countByUser(user);
        } else {
            return getCartFromSession(session).size();
        }
    }

    /**
     * Calculate cart total - from database if user is logged in, otherwise from session
     */
    public BigDecimal calculateCartTotal(User user, HttpSession session) {
        List<CartItem> cartItems = getCartItems(user, session);
        return cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Add to cart - to database if user is logged in, otherwise to session
     * For art gallery, we only allow one of each artwork (quantity parameter is ignored)
     */
    @Transactional
    public boolean addToCart(User user, Artwork artwork, int quantity, HttpSession session) {
        // Always use quantity=1 for art gallery items
        quantity = 1;

        if (user != null) {
            // Add to database cart
            Optional<CartItem> existingItem = cartItemRepository.findByUserAndArtworkId(user, artwork.getId());
            if (existingItem.isPresent()) {
                // Item already exists in cart, return false to indicate it wasn't added
                return false;
            } else {
                // Create new cart item
                CartItem newItem = new CartItem(artwork, user, quantity);
                // Set artist name
                if (artwork.getArtist() != null) {
                    newItem.setArtistName(artwork.getArtist().getName());
                } else {
                    newItem.setArtistName("Unknown Artist");
                }
                cartItemRepository.save(newItem);
                return true;
            }
        } else {
            // Add to session cart
            List<CartItem> cart = getCartFromSession(session);

            // Check if item already exists
            for (CartItem item : cart) {
                if (item.getArtworkId().equals(artwork.getId())) {
                    // Item already exists in cart, return false to indicate it wasn't added
                    return false;
                }
            }

            // Item doesn't exist, add it
            CartItem newItem = new CartItem(artwork, quantity);
            // Set artist name
            if (artwork.getArtist() != null) {
                newItem.setArtistName(artwork.getArtist().getName());
            } else {
                newItem.setArtistName("Unknown Artist");
            }
            cart.add(newItem);

            // Update session
            session.setAttribute("cart", cart);
            session.setAttribute("cartCount", cart.size());
            return true;
        }
    }

    /**
     * Update cart item - in database if user is logged in, otherwise in session
     */
    @Transactional
    public void updateCartItem(User user, Long itemId, int quantity, HttpSession session) {
        if (user != null) {
            // Update database cart
            Optional<CartItem> itemOpt = cartItemRepository.findById(itemId);
            if (itemOpt.isPresent()) {
                CartItem item = itemOpt.get();
                if (quantity <= 0) {
                    cartItemRepository.delete(item);
                } else {
                    item.setQuantity(quantity);
                    cartItemRepository.save(item);
                }
            }
        } else {
            // Update session cart
            List<CartItem> cart = getCartFromSession(session);
            for (CartItem item : cart) {
                if (item.getId().equals(itemId)) {
                    if (quantity <= 0) {
                        cart.remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                    break;
                }
            }
            // Update session
            session.setAttribute("cart", cart);
            session.setAttribute("cartCount", cart.size());
        }
    }

    /**
     * Remove cart item - from database if user is logged in, otherwise from session
     */
    @Transactional
    public void removeCartItem(User user, Long itemId, HttpSession session) {
        if (user != null) {
            // Remove from database cart
            cartItemRepository.deleteById(itemId);
        } else {
            // Remove from session cart
            List<CartItem> cart = getCartFromSession(session);
            cart.removeIf(item -> item.getId().equals(itemId));

            // Update session
            session.setAttribute("cart", cart);
            session.setAttribute("cartCount", cart.size());
        }
    }

    /**
     * Clear cart - from database if user is logged in, otherwise from session
     */
    @Transactional
    public void clearCart(User user, HttpSession session) {
        if (user != null) {
            // Clear database cart
            cartItemRepository.deleteByUser(user);
        } else {
            // Clear session cart
            session.removeAttribute("cart");
            session.setAttribute("cartCount", 0);
        }
    }

    /**
     * Transfer items from session cart to database cart when user logs in
     */
    @Transactional
    public void transferCartFromSessionToDatabase(User user, HttpSession session) {
        List<CartItem> sessionCart = getCartFromSession(session);

        for (CartItem sessionItem : sessionCart) {
            Optional<CartItem> existingItem = cartItemRepository.findByUserAndArtworkId(user, sessionItem.getArtworkId());

            if (existingItem.isPresent()) {
                // Skip this item as it already exists in the user's cart
                // For art gallery, we don't add duplicates
                logger.info("Artwork {} already exists in user's cart, skipping", sessionItem.getArtworkId());
            } else {
                // Create new item
                CartItem newItem = new CartItem();
                newItem.setArtworkId(sessionItem.getArtworkId());
                newItem.setTitle(sessionItem.getTitle());
                newItem.setImageUrl(sessionItem.getImageUrl());
                newItem.setPrice(sessionItem.getPrice());
                newItem.setQuantity(1); // Always 1 for art gallery
                newItem.setArtistName(sessionItem.getArtistName()); // Copy artist name
                newItem.setUser(user);
                cartItemRepository.save(newItem);
            }
        }

        // Clear session cart after transfer
        session.removeAttribute("cart");
        session.setAttribute("cartCount", 0);
    }

    /**
     * Get cart from session
     */
    @SuppressWarnings("unchecked")
    private List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
            session.setAttribute("cartCount", 0);
        }
        return cart;
    }
}
