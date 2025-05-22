package com.epi.artgallery.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String redirectUrl = "/login?error=true";

            if (exception instanceof DisabledException) {
                redirectUrl = "/login?disabled=true";
            } else if (exception instanceof LockedException) {
                redirectUrl = "/login?locked=true";
            } else if (exception instanceof BadCredentialsException) {
                redirectUrl = "/login?badCredentials=true";
            }

            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/register", "/css/**", "/js/**", "/images/**", "/uploads/**",
                                "/static/**", "/artists", "/artworks", "/login").permitAll() // Public access paths
                        .requestMatchers("/dashboard/admin/**").hasRole("ADMIN") // Admin access
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Also protect /admin/** paths
                        .requestMatchers("/dashboard/profile", "/dashboard/profile/**", "/dashboard/update",
                                "/dashboard/reset-password").authenticated() // Explicitly allow profile operations
                        .requestMatchers("/dashboard/**").authenticated() // Require authentication for user dashboard
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .formLogin(form -> form
                        .loginPage("/login") // Custom login page
                        .loginProcessingUrl("/login") // URL to process login
                        .usernameParameter("username") // Keep this as "username" to match the form field name
                        .defaultSuccessUrl("/dashboard", true) // Default success URL with redirect parameter
                        .successHandler(authenticationSuccessHandler()) // Redirect based on role after login
                        .failureHandler(authenticationFailureHandler()) // Use custom failure handler for disabled accounts
                        .permitAll() // Allow everyone to see the login page
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // Logout URL
                        .logoutSuccessUrl("/") // Redirect after logout
                        .invalidateHttpSession(true) // Invalidate session
                        .clearAuthentication(true) // Clear authentication
                        .deleteCookies("JSESSIONID", "remember-me") // Delete cookies including remember-me
                        .permitAll() // Allow everyone to log out
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied") // Custom access denied page
                )
                // Add session management configuration
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession() // Migrate session on authentication change
                        .maximumSessions(1) // Prevent multiple concurrent sessions
                        .expiredUrl("/login?expired=true") // Redirect when session expires
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                response.sendRedirect("/dashboard/admin"); // Keep original path
            } else {
                response.sendRedirect("/dashboard"); // Redirect to user dashboard
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // Disable user caching to ensure fresh user details are loaded each time
        provider.setUserCache(new NullUserCache());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
