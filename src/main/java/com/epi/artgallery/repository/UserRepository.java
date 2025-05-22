package com.epi.artgallery.repository;

import com.epi.artgallery.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find recent users ordered by ID descending
     */
    @Query("SELECT u FROM User u ORDER BY u.id DESC")
    List<User> findRecentUsers(Pageable pageable);

    /**
     * Search users by username, email, or role with pagination
     */
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCase(
            String username, String email, String role, Pageable pageable);

    /**
     * Alternative search method using a single JPQL query
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.role) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find users by role
     */
    Page<User> findByRoleContainingIgnoreCase(String role, Pageable pageable);

    /**
     * Find users by exact role
     */
    Page<User> findByRole(String role, Pageable pageable);

    /**
     * Find users by enabled status
     */
    Page<User> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * Find users by role and enabled status
     */
    Page<User> findByRoleAndEnabled(String role, Boolean enabled, Pageable pageable);

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role LIKE %:role%")
    long countByRole(@Param("role") String role);

    /**
     * Search users by search term, role, and enabled status
     */
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND u.role = :role AND u.enabled = :enabled")
    Page<User> findBySearchTermAndRoleAndEnabled(
            @Param("search") String search,
            @Param("role") String role,
            @Param("enabled") Boolean enabled,
            Pageable pageable);

    /**
     * Search users by search term and role
     */
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND u.role = :role")
    Page<User> findBySearchTermAndRole(
            @Param("search") String search,
            @Param("role") String role,
            Pageable pageable);

    /**
     * Search users by search term and enabled status
     */
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND u.enabled = :enabled")
    Page<User> findBySearchTermAndEnabled(
            @Param("search") String search,
            @Param("enabled") Boolean enabled,
            Pageable pageable);
}
