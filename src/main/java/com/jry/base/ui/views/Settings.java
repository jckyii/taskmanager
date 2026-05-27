package com.jry.base.ui.views;

import org.springframework.security.core.userdetails.UserDetails;

import com.jry.backend.entities.ApplicationUser;
import com.jry.backend.entities.UserRepository;
import com.jry.backend.service.UserService;
import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.PermitAll;

/**
 * Per-user settings: display name, urgency threshold (hours), change password.
 * Email editing is intentionally omitted until email-verification is built — changing
 * the login identity without re-verification would be a real security gap.
 */
@Route("settings")
@PageTitle("Settings")
@Menu(order = 2, title = "Settings", icon = "vaadin:cog")
@PermitAll
public class Settings extends VerticalLayout {

    private final transient UserService userService;
    private final transient UserRepository userRepo;
    private final transient ApplicationUser currentUser;

    // Profile fields
    private final TextField displayNameField = new TextField("Display name");

    // Urgency threshold
    private final IntegerField urgentThresholdField = new IntegerField("Urgency threshold (hours)");

    // Password change (all three optional — only validated/applied if any are filled)
    private final PasswordField currentPasswordField = new PasswordField("Current password");
    private final PasswordField newPasswordField = new PasswordField("New password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm new password");

    public Settings(UserService userService, UserRepository userRepo, AuthenticationContext authContext) {
        this.userService = userService;
        this.userRepo = userRepo;

        String userEmail = authContext.getAuthenticatedUser(UserDetails.class)
                .orElseThrow(() -> new IllegalStateException("No authenticated user in session"))
                .getUsername();
        this.currentUser = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("No account found for email: " + userEmail));

        setSizeFull();
        setPadding(true);
        add(new ViewToolbar("Settings"));

        add(buildProfileSection());
        add(new Hr());
        add(buildUrgencySection());
        add(new Hr());
        add(buildPasswordSection());

        // Single Save button at the bottom commits all changes.
        Button saveBtn = new Button("Save Changes", e -> saveAll());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout actions = new HorizontalLayout(saveBtn);
        actions.getStyle().set("margin-top", "24px");
        add(actions);

        prefillFields();
    }

    // ---- Section builders ---------------------------------------------------

    private VerticalLayout buildProfileSection() {
        H3 heading = new H3("Profile");
        heading.getStyle().set("margin-bottom", "4px");

        Span emailReadonly = new Span("Email: " + currentUser.getEmail());
        emailReadonly.getStyle().set("color", "var(--lumo-secondary-text-color)");
        emailReadonly.getStyle().set("font-size", "14px");

        Paragraph emailNote = new Paragraph(
                "Email changes are coming soon — they require re-verification, which we'll "
                        + "add together with email-verification on signup.");
        emailNote.getStyle().set("color", "var(--lumo-secondary-text-color)");
        emailNote.getStyle().set("font-size", "13px");
        emailNote.getStyle().set("margin-top", "0");

        displayNameField.setWidth("320px");

        VerticalLayout section = new VerticalLayout(heading, emailReadonly, emailNote, displayNameField);
        section.setPadding(false);
        section.setSpacing(false);
        return section;
    }

    private VerticalLayout buildUrgencySection() {
        H3 heading = new H3("Urgency");
        heading.getStyle().set("margin-bottom", "4px");

        Paragraph blurb = new Paragraph(
                "How many hours before a task's due date should it count as urgent? "
                        + "Default is 48 hours (2 days).");
        blurb.getStyle().set("color", "var(--lumo-secondary-text-color)");
        blurb.getStyle().set("font-size", "13px");
        blurb.getStyle().set("margin-top", "0");

        urgentThresholdField.setMin(1);
        urgentThresholdField.setMax(720); // a month
        urgentThresholdField.setStepButtonsVisible(true);
        urgentThresholdField.setWidth("220px");
        urgentThresholdField.setHelperText("Between 1 and 720 hours");

        VerticalLayout section = new VerticalLayout(heading, blurb, urgentThresholdField);
        section.setPadding(false);
        section.setSpacing(false);
        return section;
    }

    private VerticalLayout buildPasswordSection() {
        H3 heading = new H3("Change password");
        heading.getStyle().set("margin-bottom", "4px");

        Paragraph blurb = new Paragraph(
                "Leave these fields blank if you don't want to change your password.");
        blurb.getStyle().set("color", "var(--lumo-secondary-text-color)");
        blurb.getStyle().set("font-size", "13px");
        blurb.getStyle().set("margin-top", "0");

        currentPasswordField.setWidthFull();
        newPasswordField.setWidthFull();
        confirmPasswordField.setWidthFull();

        FormLayout fl = new FormLayout(currentPasswordField, newPasswordField, confirmPasswordField);
        fl.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        fl.setMaxWidth("420px");

        VerticalLayout section = new VerticalLayout(heading, blurb, fl);
        section.setPadding(false);
        section.setSpacing(false);
        return section;
    }

    // ---- Behavior -----------------------------------------------------------

    private void prefillFields() {
        displayNameField.setValue(currentUser.getDisplayName() == null ? "" : currentUser.getDisplayName());
        urgentThresholdField.setValue(currentUser.getUrgentThresholdHours());
    }

    private void saveAll() {
        boolean anyChange = false;
        boolean anyError = false;

        // --- Display name ---
        String newName = displayNameField.getValue() == null ? "" : displayNameField.getValue().trim();
        if (newName.isEmpty()) {
            displayNameField.setInvalid(true);
            displayNameField.setErrorMessage("Display name cannot be empty");
            anyError = true;
        } else if (!newName.equals(currentUser.getDisplayName())) {
            userService.updateDisplayName(currentUser, newName);
            anyChange = true;
        }

        // --- Urgency threshold ---
        Integer hours = urgentThresholdField.getValue();
        if (hours == null) {
            urgentThresholdField.setInvalid(true);
            urgentThresholdField.setErrorMessage("Required");
            anyError = true;
        } else if (hours < 1 || hours > 720) {
            urgentThresholdField.setInvalid(true);
            urgentThresholdField.setErrorMessage("Must be between 1 and 720");
            anyError = true;
        } else if (hours != currentUser.getUrgentThresholdHours()) {
            userService.updateUrgentThresholdHours(currentUser, hours);
            anyChange = true;
        }

        // --- Password (only validate if user touched any of the three fields) ---
        String current = nullToEmpty(currentPasswordField.getValue());
        String fresh = nullToEmpty(newPasswordField.getValue());
        String confirm = nullToEmpty(confirmPasswordField.getValue());
        boolean passwordTouched = !current.isEmpty() || !fresh.isEmpty() || !confirm.isEmpty();

        if (passwordTouched) {
            if (current.isEmpty() || fresh.isEmpty() || confirm.isEmpty()) {
                Notification.show("To change your password, fill in all three password fields.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                anyError = true;
            } else if (!fresh.equals(confirm)) {
                confirmPasswordField.setInvalid(true);
                confirmPasswordField.setErrorMessage("New passwords don't match");
                anyError = true;
            } else if (fresh.length() < 8) {
                newPasswordField.setInvalid(true);
                newPasswordField.setErrorMessage("New password must be at least 8 characters");
                anyError = true;
            } else {
                boolean ok = userService.changePassword(currentUser, current, fresh);
                if (!ok) {
                    currentPasswordField.setInvalid(true);
                    currentPasswordField.setErrorMessage("Current password is incorrect");
                    anyError = true;
                } else {
                    anyChange = true;
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                }
            }
        }

        // --- Banner ---
        if (anyError) {
            // Field-level errors are already shown; don't pile on with a top banner unless
            // there's no other change to report.
            if (!anyChange) {
                return;
            }
        }
        if (anyChange) {
            Notification n = Notification.show("Settings saved.");
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            n.setPosition(Notification.Position.TOP_CENTER);
            n.setDuration(2500);
        } else if (!anyError) {
            Notification.show("No changes to save.");
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}