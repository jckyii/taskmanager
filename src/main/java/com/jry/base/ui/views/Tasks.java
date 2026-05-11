package com.jry.base.ui.views;

import com.jry.backend.entities.ApplicationUser;
import com.jry.backend.entities.Task;
import com.jry.backend.entities.TaskRepository;
import com.jry.backend.entities.UserRepository;
import com.jry.base.ui.components.TaskCardList;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

@PermitAll
@Route("") // Keep your existing route here
@PageTitle("My Tasks")
public class Tasks extends VerticalLayout {

    public Tasks(TaskRepository taskRepo, UserRepository userRepo, AuthenticationContext authContext) {

        // --- 1. GET THE LOGGED IN USER ---
        String username = authContext.getAuthenticatedUser(UserDetails.class).get().getUsername();
        ApplicationUser currentUser = userRepo.findByUsername(username).get();

        // --- 2. HEADER LAYOUT ---
        H2 pageTitle = new H2("My Tasks");
        pageTitle.getStyle().set("margin-top", "0");

        Button newTaskBtn = new Button("New Task", e -> {
            getUI().ifPresent(ui -> ui.navigate("tasks/new"));
        });
        newTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout header = new HorizontalLayout(pageTitle, newTaskBtn);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // --- 3. FETCH DATA ---
        List<Task> allTasksInDatabase = taskRepo.findByUser(currentUser);

        // --- 4. BUILD THE GRID (With the new buttons!) ---
        TaskCardList grid = new TaskCardList(allTasksInDatabase,
                taskToComplete -> {
                    taskToComplete.setCompleted(true);
                    taskRepo.save(taskToComplete);

                    Notification completeBanner = Notification.show("Task marked as completed! 🎉");
                    completeBanner.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    completeBanner.setPosition(Notification.Position.TOP_CENTER);
                    completeBanner.setDuration(3000);

                    getUI().ifPresent(ui -> ui.getPage().reload());
                },
                taskToDelete -> {
                    taskRepo.delete(taskToDelete);

                    Notification deletedBanner = Notification.show("Task permanently deleted.");
                    deletedBanner.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    deletedBanner.setPosition(Notification.Position.TOP_CENTER);
                    deletedBanner.setDuration(3000);

                    getUI().ifPresent(ui -> ui.getPage().reload());
                }
        );

        // --- 5. REBUILD THE MISSING TOOLBAR ---

        // A. Search Bar
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search tasks...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER); // Filters as you type!

        // B. Category Dropdown
        Select<String> categoryFilter = new Select<>();
        categoryFilter.setItems("All Categories", "Work", "School", "Home", "Casual", "Uncategorized");
        categoryFilter.setValue("All Categories");

        // C. Subject Dropdown (Dynamically grabs existing subjects)
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

        // D. Group By Dropdown
        Select<String> groupByFilter = new Select<>();
        groupByFilter.setItems("Group by Status", "Group by Date", "Group by Subject");
        groupByFilter.setValue("Group by Status");

        // Put them all in a row
        HorizontalLayout toolbar = new HorizontalLayout(searchField, categoryFilter, subjectFilter, groupByFilter);
        toolbar.setWidthFull();
        toolbar.getStyle().set("margin-top", "16px");
        toolbar.getStyle().set("margin-bottom", "16px");

        // --- 6. WIRE THE TOOLBAR TO THE GRID ---
        // Whenever any dropdown or search bar changes, it tells the grid to update!
        searchField.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        categoryFilter.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        subjectFilter.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        groupByFilter.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));


        // --- 7. FINAL ASSEMBLY ---
        setSizeFull();
        setPadding(true);
        // Notice we add the header, THEN the toolbar, THEN the grid!
        add(header, toolbar, grid);
    }
}