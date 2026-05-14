package com.jry.base.ui.views;

import com.jry.backend.dto.UserDTO;
import com.jry.backend.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("signup")
@PageTitle("Sign Up")
@AnonymousAllowed
public class Signup extends VerticalLayout {
    private final UserService userService;
    private final BeanValidationBinder<UserDTO> binder = new BeanValidationBinder<>(UserDTO.class);

    private final TextField username = new TextField("Display Name");
    private final EmailField email = new EmailField("Email Address"); 
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm Password");

    public Signup(UserService userService) {
        this.userService = userService;

        // --- 1. NEW HEADERS ---
        H1 title = new H1("Create an Account");
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "8px");
        title.getStyle().set("font-size", "28px");

        Span subtitle = new Span("Join to start managing your tasks.");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-bottom", "24px");

        // Set inputs to trigger validation checks as you type
        username.setValueChangeMode(ValueChangeMode.LAZY);
        email.setValueChangeMode(ValueChangeMode.LAZY);
        password.setValueChangeMode(ValueChangeMode.LAZY);
        confirmPassword.setValueChangeMode(ValueChangeMode.LAZY);

        // Make fields span the full width of the card
        username.setWidthFull();
        email.setWidthFull();
        password.setWidthFull();
        confirmPassword.setWidthFull();

        Button submitBtn = new Button("Sign Up");
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setWidthFull(); // Make button full width

        Button loginLink = new Button("Already have an account? Log in", e ->
                getUI().ifPresent(ui -> ui.navigate("login"))
        );
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginLink.setWidthFull(); // Make link full width

        // --- 2. BINDER LOGIC (Untouched) ---
        binder.bindInstanceFields(this);
        
        binder.forField(email)
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Please enter a valid email address")) // 2. Must look like an email
                .withValidator(e -> !this.userService.emailExists(e), "This email is already registered")
                .bind(UserDTO::getEmail, UserDTO::setEmail);

        binder.forField(confirmPassword)
                .withValidator(confirmPass -> confirmPass.equals(password.getValue()), "Passwords do not match");

        submitBtn.addClickListener(e -> {
            UserDTO dto = new UserDTO();
            if(binder.writeBeanIfValid(dto)) {
                this.userService.createUser(dto.getUsername(), dto.getEmail(), dto.getPassword());
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                binder.validate();
            }
        });

        // --- 3. THE CARD LAYOUT ---
        FormLayout formLayout = new FormLayout();
        formLayout.add(username, email, password, confirmPassword);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Group everything into a styled "Card" container
        VerticalLayout card = new VerticalLayout(title, subtitle, formLayout, submitBtn, loginLink);
        card.setWidth("400px");
        card.setPadding(true);
        card.setAlignItems(Alignment.CENTER); // Center the text and buttons
        
        // The CSS that makes it look like a floating portal box
        card.getStyle().set("background-color", "#ffffff");
        card.getStyle().set("border", "1px solid #e5e7eb");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("box-shadow", "0 10px 15px -3px rgba(0, 0, 0, 0.1)");

        // Center the entire card on the screen
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)"); // Very subtle grey background for the page
        
        add(card);
    }
}