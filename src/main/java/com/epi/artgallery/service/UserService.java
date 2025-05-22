package com.epi.artgallery.service;

import com.epi.artgallery.model.User;
import com.epi.artgallery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with encoded password
     */
    @Transactional
    public User registerUser(User user) {
        // Encode the password before saving
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Set enabled to true by default if not specified
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }

        // Set default role if not specified
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        } else if (!user.getRole().startsWith("ROLE_")) {
            // Ensure role has ROLE_ prefix
            user.setRole("ROLE_" + user.getRole());
        }

        return userRepository.save(user);
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get all users
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get all users with pagination
     */
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Search users by username, email, or role
     */
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm, pageable);
    }

    /**
     * Find users by filters (search term, role, enabled status)
     */
    public Page<User> findByFilters(String search, String role, Boolean enabled, Pageable pageable) {
        if (StringUtils.hasText(search) && StringUtils.hasText(role) && enabled != null) {
            // All filters provided
            return userRepository.findBySearchTermAndRoleAndEnabled(search, role, enabled, pageable);
        } else if (StringUtils.hasText(search) && StringUtils.hasText(role)) {
            // Search and role filters
            return userRepository.findBySearchTermAndRole(search, role, pageable);
        } else if (StringUtils.hasText(search) && enabled != null) {
            // Search and enabled filters
            return userRepository.findBySearchTermAndEnabled(search, enabled, pageable);
        } else if (StringUtils.hasText(role) && enabled != null) {
            // Role and enabled filters
            return userRepository.findByRoleAndEnabled(role, enabled, pageable);
        } else if (StringUtils.hasText(search)) {
            // Only search filter
            return searchUsers(search, pageable);
        } else if (StringUtils.hasText(role)) {
            // Only role filter
            return userRepository.findByRole(role, pageable);
        } else if (enabled != null) {
            // Only enabled filter
            return userRepository.findByEnabled(enabled, pageable);
        } else {
            // No filters
            return findAll(pageable);
        }
    }

    /**
     * Count total users
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Save or update a user
     * This method handles both new users and updates to existing users
     */
    @Transactional
    public User saveUser(User user) {
        if (user.getId() != null) {
            // This is an update operation
            Optional<User> existingUserOpt = userRepository.findById(user.getId());
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();

                // Update basic information
                existingUser.setUsername(user.getUsername());
                existingUser.setEmail(user.getEmail());
                existingUser.setFirstName(user.getFirstName());
                existingUser.setLastName(user.getLastName());

                // Update bio field
                existingUser.setBio(user.getBio());

                // Handle password update - only if a new password is provided
                if (user.getPassword() != null && !user.getPassword().isEmpty() &&
                        !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                    existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                }

                // Update role if provided
                if (user.getRole() != null && !user.getRole().isEmpty()) {
                    // Ensure role has ROLE_ prefix
                    if (!user.getRole().startsWith("ROLE_")) {
                        existingUser.setRole("ROLE_" + user.getRole());
                    } else {
                        existingUser.setRole(user.getRole());
                    }
                }

                // Update enabled status
                existingUser.setEnabled(user.getEnabled());

                return userRepository.save(existingUser);
            }
        }

        // For new users or if existing user not found
        return registerUser(user);
    }

    /**
     * Update user password
     */
    @Transactional
    public void updatePassword(User user, String newPassword) {
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

    /**
     * Update user profile
     * This method specifically handles profile updates
     */
    @Transactional
    public User updateProfile(User updatedUser) {
        if (updatedUser.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for profile update");
        }

        Optional<User> existingUserOpt = userRepository.findById(updatedUser.getId());
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + updatedUser.getId());
        }

        User existingUser = existingUserOpt.get();

        // Update profile fields
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());

        // Update bio field
        existingUser.setBio(updatedUser.getBio());

        // Handle password update
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty() &&
                !passwordEncoder.matches(updatedUser.getPassword(), existingUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Preserve role and enabled status from the existing user
        // This ensures the user can't change their own role
        existingUser.setRole(existingUser.getRole());
        existingUser.setEnabled(existingUser.getEnabled());

        return userRepository.save(existingUser);
    }

    /**
     * Update user (admin function)
     * This method allows updating all user fields including role and enabled status
     */
    @Transactional
    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for user update");
        }

        Optional<User> existingUserOpt = userRepository.findById(user.getId());
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }

        User existingUser = existingUserOpt.get();

        // Update all fields
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setBio(user.getBio());

        // Update role with proper prefix
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            if (!user.getRole().startsWith("ROLE_")) {
                existingUser.setRole("ROLE_" + user.getRole());
            } else {
                existingUser.setRole(user.getRole());
            }
        }

        existingUser.setEnabled(user.getEnabled());

        // Handle password update
        if (user.getPassword() != null && !user.getPassword().isEmpty() &&
                !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    /**
     * Delete a user by ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Delete a user by ID (alias for deleteUser)
     */
    @Transactional
    public void deleteById(Long userId) {
        deleteUser(userId);
    }

    /**
     * Check if username exists (alias for existsByUsername)
     */
    public boolean usernameExists(String username) {
        return existsByUsername(username);
    }

    /**
     * Check if email exists (alias for existsByEmail)
     */
    public boolean emailExists(String email) {
        return existsByEmail(email);
    }

    /**
     * Find recent users
     * Returns the most recent users based on ID (assuming newer users have higher IDs)
     */
    public List<User> findRecentUsers(int limit) {
        return userRepository.findRecentUsers(PageRequest.of(0, limit));
    }

    /**
     * Encode a password
     */
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Save a user (alias for saveUser)
     */
    public User save(User user) {
        return saveUser(user);
    }
}
