package com.jry.backend.service;

import com.jry.backend.entities.ApplicationUser;
import com.jry.backend.entities.Task;
import com.jry.backend.entities.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(String username, String rawPassword) {
        String hashedPassword = passwordEncoder.encode(rawPassword);
        ApplicationUser newUser = new ApplicationUser(username, hashedPassword);
        userRepo.save(newUser);
    }

    public boolean userExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    @Override
    public @Nonnull UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
