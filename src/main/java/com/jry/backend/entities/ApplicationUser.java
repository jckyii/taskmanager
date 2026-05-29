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

    /**
     * Whether this user has verified their email. Brand-new signups default to false
     * (the verification email enables them when they click the link). Existing rows in
     * the DB get NULL when Hibernate adds the column on first startup; isEnabled()
     * treats NULL as true so existing accounts (created before verification existed)
     * are grandfathered. After first startup, run this in Supabase once to clean up:
     *   UPDATE app_users SET enabled = true WHERE enabled IS NULL;
     */
    @Column(name = "enabled")
    private Boolean enabled = false;

    /**
     * How many hours before a task's due date it starts counting as "urgent".
     * Defaults to 48 hours (the old hardcoded 2-day window). Existing rows in the DB
     * may be NULL after the schema migration — getUrgentThresholdHours() handles that
     * by returning the default, so no manual backfill is needed.
     */
    @Column(name = "urgent_threshold_hours")
    private Integer urgentThresholdHours = 48;


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

    /**
     * Spring Security checks this at login time. We override it so:
     *  - new signups (enabled=false) are blocked until they verify their email,
     *  - existing/grandfathered users (enabled=null, before the SQL backfill runs) can
     *    still log in (we treat NULL as true so first-time deploys don't lock everyone out).
     */
    @Override
    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    /** Marks the user as verified. Called by the verification service when they click the link. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    /** Updates the display name (the entity's `username` field is the display name; the
     *  authentication identity is the email, exposed via getUsername()). */
    public void setDisplayName(String displayName) {
        this.username = displayName;
    }

    /** Sets the (already-hashed) password. UserService is responsible for hashing. */
    public void setPassword(String hashedPassword) {
        this.password = hashedPassword;
    }

    /**
     * Returns the configured urgency threshold in hours. Null-safe: if the column is
     * NULL for an old row (or unset on a brand-new entity), returns the default of 48.
     */
    public int getUrgentThresholdHours() {
        return urgentThresholdHours == null ? 48 : urgentThresholdHours;
    }

    public void setUrgentThresholdHours(int urgentThresholdHours) {
        this.urgentThresholdHours = urgentThresholdHours;
    }
}