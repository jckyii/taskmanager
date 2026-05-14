package com.jry.backend.entities;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class ApplicationUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)//doesnt need to be true if emails are unique and required
    private String username;

    @Column(nullable = false)
    private String password;


    public ApplicationUser() {}

    public ApplicationUser(String username, String password) {
        this.username = username;
        this.password = password;
        this.email = null; // Set email to null by default, it will be set later when creating the user
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
        return email; // Return email as the username for authentication
    }

    public String getDisplayName() {
        return this.username;
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
}
