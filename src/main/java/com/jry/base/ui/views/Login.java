package com.jry.base.ui.views;

import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;


@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class    Login extends VerticalLayout implements BeforeEnterObserver {

    private LoginForm login = new LoginForm();

    public Login() {
        addClassName("login-view");
        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Log in");

        TextField username = new TextField("Username");
        username.setWidthFull();

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();

        Checkbox rememberMe = new Checkbox("Remember me");

        Button loginBtn = new Button("Log in");
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.setWidthFull();


        Button signupBtn = new Button("Don't have an account? Sign up");
        signupBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        signupBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("signup")));

        VerticalLayout formLayout = new VerticalLayout(title, username, password, rememberMe, loginBtn);
        formLayout.setPadding(true);
        formLayout.setWidth("350px");
        formLayout.getStyle().set("border", "1px solid #e5e7eb");
        formLayout.getStyle().set("border-radius", "12px");
        formLayout.getStyle().set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");

        //hidden HTML form to communicate with Spring Security
        String hiddenFormHtml = "<form id='login-form' method='post' action='login' style='display:none;'>" +
                "<input type='text' id='hidden-user' name='username'>" +
                "<input type='password' id='hidden-pass' name='password'>" +
                "<input type='checkbox' id='hidden-remember' name='remember-me'>" +
                "</form>";
        Html hiddenForm = new Html(hiddenFormHtml);

        // wire the button to copy the Vaadin values to the hidden form and submit it!
        loginBtn.addClickListener(e -> {
            getElement().executeJs(
                    "document.getElementById('hidden-user').value = $0;" +
                            "document.getElementById('hidden-pass').value = $1;" +
                            "document.getElementById('hidden-remember').checked = $2;" +
                            "document.getElementById('login-form').submit();",
                    username.getValue(), password.getValue(), rememberMe.getValue()
            );
        });

        add(formLayout, signupBtn, hiddenForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}