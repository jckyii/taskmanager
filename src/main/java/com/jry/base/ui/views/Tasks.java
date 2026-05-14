package com.jry.base.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.jry.backend.entities.ApplicationUser;
import com.jry.backend.entities.Task;
import com.jry.backend.entities.TaskRepository;
import com.jry.backend.entities.UserRepository;
import com.jry.base.ui.components.TaskCardList;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.PermitAll;

@PermitAll
@Route("")
@PageTitle("My Tasks")
public class Tasks extends VerticalLayout {

    public Tasks(TaskRepository taskRepo, UserRepository userRepo, AuthenticationContext authContext) {

        // --- 1. CHECK FOR RELOAD BANNERS ---
        if (Boolean.TRUE.equals(VaadinSession.getCurrent().getAttribute("showCompleteBanner"))) {
            Notification completeBanner = Notification.show("Task marked as completed! 🎉");
            completeBanner.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            completeBanner.setPosition(Notification.Position.TOP_CENTER);
            completeBanner.setDuration(3000);
            VaadinSession.getCurrent().setAttribute("showCompleteBanner", null);
        }

        if (Boolean.TRUE.equals(VaadinSession.getCurrent().getAttribute("showDeleteBanner"))) {
            Notification deletedBanner = Notification.show("Task permanently deleted.");
            deletedBanner.addThemeVariants(NotificationVariant.LUMO_ERROR);
            deletedBanner.setPosition(Notification.Position.TOP_CENTER);
            deletedBanner.setDuration(3000);
            VaadinSession.getCurrent().setAttribute("showDeleteBanner", null);
        }

        // --- 2. GET THE LOGGED IN USER ---
        String userEmail = authContext.getAuthenticatedUser(UserDetails.class).get().getUsername();
        ApplicationUser currentUser = userRepo.findByEmail(userEmail).get();

        // --- 3. HEADER LAYOUT (WITH FIXED LOGOUT BUTTON) ---
        H2 pageTitle = new H2("Welcome, " + currentUser.getDisplayName());
        pageTitle.getStyle().set("margin-top", "0");

        Button newTaskBtn = new Button("New Task", e -> {
            getUI().ifPresent(ui -> ui.navigate("tasks/new"));
        });
        newTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // FIX: Removed "Tertiary" so it actually looks like a framed button.
        // Added LUMO_ERROR so it has a nice, clean red text/border to warn users it's an exit action.
        Button logoutBtn = new Button("Sign Out", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        logoutBtn.getStyle().set("cursor", "pointer"); // Shows the clicky-finger on hover

        logoutBtn.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Sign Out");
            dialog.setText("Are you sure you want to log out of your account?");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Sign Out");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event -> {
                authContext.logout();
            });
            dialog.open();
        });

        HorizontalLayout headerButtons = new HorizontalLayout(newTaskBtn, logoutBtn);
        headerButtons.setAlignItems(Alignment.CENTER);
        headerButtons.getStyle().set("gap", "12px"); // Adds a little breathing room between the buttons

        HorizontalLayout header = new HorizontalLayout(pageTitle, headerButtons);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // --- 4. FETCH DATA ---
        List<Task> allTasksInDatabase = taskRepo.findByUser(currentUser);

        // --- 5. BUILD THE GRID ---
        TaskCardList grid = new TaskCardList(allTasksInDatabase,
                taskToComplete -> {
                    taskToComplete.setCompleted(true);
                    taskRepo.save(taskToComplete);

                    VaadinSession.getCurrent().setAttribute("showCompleteBanner", true);
                    getUI().ifPresent(ui -> ui.getPage().reload());
                },
                taskToDelete -> {
                    taskRepo.delete(taskToDelete);

                    VaadinSession.getCurrent().setAttribute("showDeleteBanner", true);
                    getUI().ifPresent(ui -> ui.getPage().reload());
                }
        );

        // --- 6. BUILD THE TOOLBAR ---
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search tasks...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        Select<String> categoryFilter = new Select<>();
        categoryFilter.setItems("All Categories", "Work", "School", "Home", "Casual", "Uncategorized");
        categoryFilter.setValue("All Categories");

        Select<String> subjectFilter = new Select<>();
        List<String> subjects = new ArrayList<>();
        subjects.add("All Subjects");
        allTasksInDatabase.stream()
                .map(Task::getSubject)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .forEach(subjects::add);
        subjectFilter.setItems(subjects);
        subjectFilter.setValue("All Subjects");

        Select<String> groupByFilter = new Select<>();
        groupByFilter.setItems("Group by Status", "Group by Date", "Group by Subject");

        String savedGroupBy = (String) VaadinSession.getCurrent().getAttribute("savedGroupBy");
        if (savedGroupBy != null) {
            groupByFilter.setValue(savedGroupBy);
        } else {
            groupByFilter.setValue("Group by Status");
        }

        HorizontalLayout toolbar = new HorizontalLayout(searchField, groupByFilter, categoryFilter, subjectFilter);
        toolbar.setWidthFull();
        toolbar.getStyle().set("margin-top", "16px");
        toolbar.getStyle().set("margin-bottom", "16px");

        // --- 7. WIRE TOOLBAR TO GRID ---
        searchField.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        categoryFilter.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        subjectFilter.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));

        groupByFilter.addValueChangeListener(e -> {
            VaadinSession.getCurrent().setAttribute("savedGroupBy", e.getValue());
            grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue());
        });

        grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue());

        // --- 8. FINAL ASSEMBLY ---
        setSizeFull();
        setPadding(true);
        add(header, toolbar, grid);
    }
}