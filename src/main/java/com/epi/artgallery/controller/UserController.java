package com.epi.artgallery.controller;

import com.epi.artgallery.model.User;
import com.epi.artgallery.service.CustomUserDetailsService;
import com.epi.artgallery.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController {
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService, CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }


    @PostMapping("/admin/users/add")
    public String addUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // Check if username already exists
            if (userService.usernameExists(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Username already exists. Please choose a different username.");
                return "redirect:/admin/users/add";
            }
            // Check if email already exists
            if (userService.emailExists(user.getEmail())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Email already exists. Please use a different email address.");
                return "redirect:/admin/users/add";
            }
            // Set default values if not provided
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("ROLE_USER");
            }
            if (user.getEnabled() == null) {
                user.setEnabled(true);
            }
            // Save the user
            User savedUser = userService.save(user);
            logger.info("Created new user: {}", savedUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "User created successfully: " + savedUser.getUsername());
            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.error("Error creating user", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to create user: " + e.getMessage());
            return "redirect:/admin/users/add";
        }
    }

    // Remove the conflicting showEditUserForm method
    // Let AdminController handle this endpoint
    @PostMapping("/admin/users/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user,
                             RedirectAttributes redirectAttributes) {
        try {
            Optional<User> existingUserOpt = userService.findById(id);
            if (existingUserOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/admin/users";
            }
            User existingUser = existingUserOpt.get();
            // Check if username is being changed and already exists
            if (!existingUser.getUsername().equals(user.getUsername()) &&
                    userService.usernameExists(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Username already exists. Please choose a different username.");
                return "redirect:/admin/users/" + id + "/edit";
            }
            // Check if email is being changed and already exists
            if (!existingUser.getEmail().equals(user.getEmail()) &&
                    userService.emailExists(user.getEmail())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Email already exists. Please use a different email address.");
                return "redirect:/admin/users/" + id + "/edit";
            }
            // If password is empty, keep the current password
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            } else {
                // Password is being updated, encode it
                user.setPassword(userService.encodePassword(user.getPassword()));
            }
            // Ensure ID is preserved
            user.setId(id);
            // Save the updated user
            User updatedUser = userService.save(user);
            logger.info("Updated user: {}", updatedUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "User updated successfully: " + updatedUser.getUsername());
            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.error("Error updating user", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update user: " + e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    // User Profile endpoints
    @GetMapping("/dashboard/profile")
    public String viewProfile(Model model) {
        // Get the current authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        // Check if the user is authenticated
        if (username.equals("anonymousUser")) {
            logger.warn("Anonymous user tried to access profile page");
            return "redirect:/login";
        }
        // Load user data
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.error("User not found for username: {}", username);
            return "redirect:/login";
        }
        User user = userOpt.get();
        model.addAttribute("user", user);
        logger.info("Profile accessed by: {}", username);
        return "dashboard/profile";
    }

    @PostMapping("/dashboard/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {
        // Get the current authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (currentUsername.equals("anonymousUser")) {
            logger.error("Anonymous user tried to update profile");
            return "redirect:/login";
        }
        // Load the current user from the database
        Optional<User> currentUserOpt = userService.findByUsername(currentUsername);
        if (currentUserOpt.isEmpty()) {
            logger.error("User not found for username: {}", currentUsername);
            return "redirect:/login";
        }
        User currentUser = currentUserOpt.get();
        // Check if username is being changed
        boolean usernameChanged = !currentUsername.equals(updatedUser.getUsername());
        // Check if the new username already exists (if username is being changed)
        if (usernameChanged && userService.usernameExists(updatedUser.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Username already exists. Please choose a different username.");
            return "redirect:/dashboard/profile";
        }
        // Store the new username before updating
        String newUsername = updatedUser.getUsername();
        // Preserve important fields from the current authenticated user
        updatedUser.setId(currentUser.getId());
        updatedUser.setRole(currentUser.getRole());
        updatedUser.setEnabled(currentUser.getEnabled());
        // If password is empty, keep the current password
        if (updatedUser.getPassword() == null || updatedUser.getPassword().isEmpty()) {
            updatedUser.setPassword(currentUser.getPassword());
        } else {
            // Password is being updated, encode it
            updatedUser.setPassword(userService.encodePassword(updatedUser.getPassword()));
        }
        try {
            logger.info("Updating profile for user: {} to new username: {}",
                    currentUsername, usernameChanged ? newUsername : currentUsername);
            // Update the user in the database
            User savedUser = userService.save(updatedUser);
            // If username was changed, update the authentication context
            if (usernameChanged) {
                try {
                    // Get the current authentication details
                    Object details = auth.getDetails();
                    // Load the updated user details with the new username
                    UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(newUsername);
                    // Create a new authentication token with the updated user details
                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            updatedUserDetails,
                            null, // Don't include credentials in the token
                            updatedUserDetails.getAuthorities());
                    // Copy the details from the old authentication
                    newAuth.setDetails(details);
                    // Update the security context with the new authentication
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    // Update the session with the new authentication
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    }
                    logger.info("Successfully updated authentication context for new username: {}", newUsername);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Profile updated successfully! Your username has been changed to: " + newUsername);
                } catch (Exception e) {
                    logger.error("Failed to update authentication context after username change", e);
                    redirectAttributes.addFlashAttribute("warningMessage",
                            "Profile updated but you may need to log out and log back in for all changes to take effect.");
                }
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            }
        } catch (Exception e) {
            logger.error("Error updating profile for user: {}", currentUsername, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/dashboard/profile";
    }
}
