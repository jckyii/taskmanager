package com.jry.backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jry.backend.entities.ApplicationUser;
import com.jry.backend.entities.UserRepository;

import jakarta.annotation.Nonnull;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. NEW: Require email when creating an account ---
    public void createUser(String username, String email, String rawPassword) {
        String hashedPassword = passwordEncoder.encode(rawPassword);
        ApplicationUser newUser = new ApplicationUser(username, hashedPassword);
        newUser.setEmail(email); // Set the email on the user entity
        userRepo.save(newUser);
    }

    // --- 2. NEW: Check if the email is already registered ---
    public boolean emailExists(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    // --- 3. THE MAGIC FIX: Spring Security searches by Email now ---
    @Override
    public @Nonnull UserDetails loadUserByUsername(@Nonnull String email) throws UsernameNotFoundException {
        // Even though the method is called "loadUserByUsername", Vaadin will pass the email 
        // from the login box into this variable. We just tell it to search the database by email!
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account found with email: " + email));
    }
}