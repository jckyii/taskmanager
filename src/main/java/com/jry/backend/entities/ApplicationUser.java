package com.jry.backend.entities;

import jakarta.persistence.*;
import jakarta.annotation.Nonnull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "app_users")
public class ApplicationUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;


    public ApplicationUser() {}

    public ApplicationUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getId() { return id; }


    // UserDetails methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nonnull String getPassword() {
        return password;
    }

    @Override
    public @Nonnull String getUsername() {
        return username;
    }
}
