package com.epi.artgallery.controller;

import com.epi.artgallery.model.Artist;
import com.epi.artgallery.model.Artwork;
import com.epi.artgallery.model.User;
import com.epi.artgallery.service.ArtistService;
import com.epi.artgallery.service.ArtworkService;
import com.epi.artgallery.service.CartService;
import com.epi.artgallery.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final ArtistService artistService;
    private final ArtworkService artworkService;
    private final UserService userService;
    private final CartService cartService;

    @Autowired
    public DashboardController(ArtistService artistService,
                               ArtworkService artworkService,
                               UserService userService,
                               CartService cartService) {
        this.artistService = artistService;
        this.artworkService = artworkService;
        this.userService = userService;
        this.cartService = cartService;
    }

    @GetMapping
    public String showDashboard(Model model, HttpSession session) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            model.addAttribute("username", username);

            // Get current user if authenticated
            User user = null;
            if (!username.equals("anonymousUser")) { // Fixed: removed extra space
                Optional<User> userOpt = userService.findByUsername(username);
                user = userOpt.orElse(null);
            }

            // Get cart count using the cart service
            int cartCount = cartService.getCartCount(user, session);
            model.addAttribute("cartCount", cartCount);

            // Add counts for regular dashboard
            model.addAttribute("artistCount", artistService.countArtists());
            model.addAttribute("artworkCount", artworkService.countArtworks());

            // Add featured artworks and artists for user dashboard
            List<Artwork> featuredArtworks = artworkService.findRecentArtworks(3); // Get 3 recent artworks
            model.addAttribute("featuredArtworks", featuredArtworks != null ? featuredArtworks : Collections.emptyList());

            List<Artist> featuredArtists = artistService.findRecentArtists(3); // Get 3 recent artists
            model.addAttribute("featuredArtists", featuredArtists != null ? featuredArtists : Collections.emptyList());

            // Log role-based user information for debugging
            logger.info("User {} with roles: {}", username, auth.getAuthorities());

            return "dashboard/index"; // User dashboard
        } catch (Exception e) {
            logger.error("Error displaying dashboard: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while loading the dashboard");
            return "error/general";
        }
    }

    @GetMapping("/admin")
    @Secured("ROLE_ADMIN")
    public String showAdminDashboard(Model model, HttpSession session) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            model.addAttribute("username", username);

            // Get current user
            Optional<User> userOpt = userService.findByUsername(username);
            User user = userOpt.orElse(null);

            // Get cart count for admin too
            int cartCount = cartService.getCartCount(user, session);
            model.addAttribute("cartCount", cartCount);

            // Add counts for admin dashboard
            long artistCount = artistService.countArtists();
            long artworkCount = artworkService.countArtworks();
            long userCount = userService.countUsers();
            model.addAttribute("artistCount", artistCount);
            model.addAttribute("artworkCount", artworkCount);
            model.addAttribute("userCount", userCount);

            // Get recent artists (limit to 5)
            List<Artist> recentArtists = artistService.findRecentArtists(5);
            model.addAttribute("recentArtists", recentArtists != null ? recentArtists : Collections.emptyList());

            // Get recent artworks (limit to 5)
            List<Artwork> recentArtworks = artworkService.findRecentArtworks(5);
            model.addAttribute("recentArtworks", recentArtworks != null ? recentArtworks : Collections.emptyList());

            // Get recent users (limit to 5)
            List<User> recentUsers = userService.findRecentUsers(5);
            model.addAttribute("recentUsers", recentUsers != null ? recentUsers : Collections.emptyList());

            // Log admin dashboard information for debugging
            logger.info("Admin Dashboard accessed by: {}", username);
            logger.info("Recent users count: {}", recentUsers != null ? recentUsers.size() : "null");

            return "dashboard/admin"; // Admin dashboard
        } catch (Exception e) {
            logger.error("Error displaying admin dashboard: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while loading the admin dashboard");
            return "error/general";
        }
    }
}
