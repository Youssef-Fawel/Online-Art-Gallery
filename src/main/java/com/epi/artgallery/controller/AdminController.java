package com.epi.artgallery.controller;

import com.epi.artgallery.model.Artist;
import com.epi.artgallery.model.Artwork;
import com.epi.artgallery.model.User;
import com.epi.artgallery.service.ArtistService;
import com.epi.artgallery.service.ArtworkService;
import com.epi.artgallery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin")
@Secured("ROLE_ADMIN")
public class AdminController {
    private final ArtistService artistService;
    private final ArtworkService artworkService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    public AdminController(ArtistService artistService, ArtworkService artworkService,
                           UserService userService, PasswordEncoder passwordEncoder) {
        this.artistService = artistService;
        this.artworkService = artworkService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Add counts for dashboard
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("artworkCount", artworkService.countArtworks());
        model.addAttribute("artistCount", artistService.countArtists());

        // Add recent items for dashboard
        model.addAttribute("recentArtists", artistService.findRecentArtists(5));
        model.addAttribute("recentArtworks", artworkService.findRecentArtworks(5));

        // Get recent users safely
        List<User> allUsers = userService.findAllUsers();
        if (allUsers != null && !allUsers.isEmpty()) {
            model.addAttribute("recentUsers", allUsers.subList(0, Math.min(5, allUsers.size())));
        } else {
            model.addAttribute("recentUsers", Collections.emptyList());
        }
        return "dashboard/admin";
    }

    @GetMapping("/artists/add")
    public String showAddArtistForm(Model model) {
        model.addAttribute("artist", new Artist());
        return "artist/form";
    }

    @GetMapping("/artworks/add")
    public String showAddArtworkForm(Model model) {
        model.addAttribute("artwork", new Artwork());
        model.addAttribute("artists", artistService.findAllArtists());
        return "artwork/form";
    }

    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "user/form";
    }

    // Updated User management methods with search and pagination
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        logger.info("User search parameters: search={}, role={}, status={}, page={}, size={}",
                search, role, status, page, size);

        // Convert status string to Boolean if provided
        Boolean enabled = null;
        if (status != null && !status.isEmpty()) {
            enabled = "true".equals(status);
        }

        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Get users with filters
        Page<User> userPage;
        try {
            userPage = userService.findByFilters(search, role, enabled, pageable);
            logger.info("Found {} users matching criteria", userPage.getTotalElements());
        } catch (Exception e) {
            logger.error("Error searching for users: {}", e.getMessage(), e);
            userPage = Page.empty();
            model.addAttribute("errorMessage", "Error searching for users: " + e.getMessage());
        }

        // Add attributes to model
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());

        // Add filter parameters to model for maintaining state
        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("size", size);

        return "user/list";
    }

    @GetMapping("/users/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model) {
        User user = userService.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        return "user/detail";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        return "user/form";
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam(required = false) String password,
                             RedirectAttributes redirectAttributes) {
        try {
            // Check if this is a new user (no ID)
            boolean isNewUser = user.getId() == null;

            // For new users, password is required
            if (isNewUser && (password == null || password.trim().isEmpty())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Password is required for new users");
                return "redirect:/admin/users/add";
            }

            // Set enabled to true for new users
            if (isNewUser) {
                user.setEnabled(true);
            }

            // Ensure role has ROLE_ prefix if needed
            if (user.getRole() != null && !user.getRole().startsWith("ROLE_")) {
                user.setRole("ROLE_" + user.getRole());
            }

            // Set the password directly (don't encode it here)
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password); // Remove passwordEncoder.encode() - let UserService handle it
            } else if (!isNewUser) {
                // If editing existing user and no new password provided, keep the old password
                userService.findById(user.getId()).ifPresent(existingUser ->
                        user.setPassword(existingUser.getPassword()));
            }

            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    isNewUser ? "User created successfully" : "User updated successfully");
        } catch (Exception e) {
            logger.error("Error saving user: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
