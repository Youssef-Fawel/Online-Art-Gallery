package com.epi.artgallery.controller;

import com.epi.artgallery.model.User;
import com.epi.artgallery.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        @RequestParam(value = "expired", required = false) String expired,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("success", "Registration successful! Please login with your credentials.");
        }
        if (expired != null) {
            model.addAttribute("warning", "Your session has expired. Please login again.");
        }

        logger.info("Accessing login page. Error: {}, Logout: {}, Registered: {}, Expired: {}",
                error, logout, registered, expired);
        return "auth/login"; // Return the login template
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Create a new User object for the registration form if not already in model
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }

        // Initialize error attributes to prevent Thymeleaf errors
        if (!model.containsAttribute("usernameError")) {
            model.addAttribute("usernameError", null);
        }
        if (!model.containsAttribute("emailError")) {
            model.addAttribute("emailError", null);
        }
        if (!model.containsAttribute("passwordError")) {
            model.addAttribute("passwordError", null);
        }

        logger.info("Displaying registration form");
        return "auth/register"; // Return the registration template
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               @RequestParam(required = false) String password,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        logger.info("Processing registration for user: {}", user.getUsername());

        // Initialize error attributes
        model.addAttribute("usernameError", null);
        model.addAttribute("emailError", null);
        model.addAttribute("passwordError", null);

        // Validate password
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("passwordError", "Password is required");
            logger.warn("Registration failed: Password is empty");
            return "auth/register";
        }

        // Check if username already exists
        if (userService.usernameExists(user.getUsername())) {
            logger.warn("Registration failed: Username '{}' already exists", user.getUsername());
            model.addAttribute("usernameError", "Username already exists");
            return "auth/register";
        }

        // Check if email already exists
        if (userService.emailExists(user.getEmail())) {
            logger.warn("Registration failed: Email '{}' already exists", user.getEmail());
            model.addAttribute("emailError", "Email already exists");
            return "auth/register";
        }

        // Check for validation errors
        if (result.hasErrors()) {
            logger.warn("Registration failed: Validation errors: {}", result.getAllErrors());
            return "auth/register";
        }

        try {
            // Set default role if not specified
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("ROLE_USER");
            }
            // Ensure role has ROLE_ prefix
            else if (!user.getRole().startsWith("ROLE_")) {
                user.setRole("ROLE_" + user.getRole());
            }

            // Enable the user by default
            user.setEnabled(true);

            // Save the user
            User savedUser = userService.saveUser(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());

            // Use a URL parameter instead of flash attribute
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            logger.error("Error during user registration", e);
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "auth/register";
        }
    }
}
