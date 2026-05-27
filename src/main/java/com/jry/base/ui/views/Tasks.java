package com.jry.base.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.jry.backend.entities.ApplicationUser;
import com.jry.backend.entities.Task;
import com.jry.backend.entities.TaskRepository;
import com.jry.backend.entities.UserRepository;
import com.jry.base.ui.components.TaskCardList;
import com.jry.base.ui.components.TaskDialog;
import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.PermitAll;

@PermitAll
@Route("")
@PageTitle("My Tasks")
@Menu(order = 0, title = "My Tasks", icon = "vaadin:tasks")
public class Tasks extends VerticalLayout {

    private final TaskRepository taskRepo;
    private final ApplicationUser currentUser;
    private TaskCardList grid;

    public Tasks(TaskRepository taskRepo, UserRepository userRepo, AuthenticationContext authContext) {
        this.taskRepo = taskRepo;

        // --- 1. GET THE LOGGED IN USER ---
        String userEmail = authContext.getAuthenticatedUser(UserDetails.class)
                .orElseThrow(() -> new IllegalStateException("No authenticated user in session"))
                .getUsername();
        this.currentUser = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("No account found for email: " + userEmail));

        // --- 2. HEADER (via ViewToolbar, so it has the drawer toggle + consistent title
        //         font matching the other views, and the action buttons on the right). ---

        // New Task now opens a dialog HUD instead of navigating to a page.
        Button newTaskBtn = new Button("New Task", e ->
                TaskDialog.openForNew(taskRepo, currentUser, this::reloadTasks));
        newTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button logoutBtn = new Button("Sign Out", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        logoutBtn.getStyle().set("cursor", "pointer");

        logoutBtn.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Sign Out");
            dialog.setText("Are you sure you want to log out of your account?");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Sign Out");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event -> authContext.logout());
            dialog.open();
        });

        ViewToolbar header = new ViewToolbar("Welcome, " + currentUser.getDisplayName(),
                ViewToolbar.group(newTaskBtn, logoutBtn));

        // --- 3. FETCH DATA ---
        List<Task> allTasksInDatabase = taskRepo.findByUser(currentUser);

        // --- 4. BUILD THE GRID ---
        grid = new TaskCardList(allTasksInDatabase);
        grid.setOnComplete(taskToComplete -> {
            taskToComplete.setCompleted(true);
            taskRepo.save(taskToComplete);
            reloadTasks();
            showTaskCompletedBanner();
        });
        grid.setOnDelete(taskToDelete -> {
            taskRepo.delete(taskToDelete);
            reloadTasks();
            showTaskDeletedBanner();
        });
        // Clicking a card now opens an edit dialog HUD instead of navigating to the detail page.
        grid.setOnCardClick(task ->
                TaskDialog.openForEdit(taskRepo, currentUser, task, this::reloadTasks));

        // --- 5. BUILD THE TOOLBAR ---
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

        // --- 6. WIRE TOOLBAR TO GRID ---
        searchField.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        categoryFilter.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        subjectFilter.addValueChangeListener(e -> grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));

        groupByFilter.addValueChangeListener(e -> {
            VaadinSession.getCurrent().setAttribute("savedGroupBy", e.getValue());
            grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue());
        });

        grid.filter(searchField.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue());

        // --- 7. FINAL ASSEMBLY ---
        // Use full WIDTH but let the view's HEIGHT grow with its content. Using setSizeFull()
        // here clamps the view to the viewport height, so when the cards overflowed, the
        // view's own background stopped short of them (the "page ends before the cards" bug).
        // Letting height be content-driven means the background always extends past the last card.
        setWidthFull();
        setHeightFull();
        getStyle().set("box-sizing", "border-box");
        setPadding(true);
        // breathing room so the last card clears the bottom edge of the scroll area
        getStyle().set("padding-bottom", "48px");
        add(header, toolbar, grid);
    }

    /** Re-reads this user's tasks from the DB and refreshes the card list. */
    private void reloadTasks() {
        grid.refresh(taskRepo.findByUser(currentUser));
    }

    private void showTaskCompletedBanner() {
        Notification completeBanner = Notification.show("Task marked as completed! 🎉");
        completeBanner.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        completeBanner.setPosition(Notification.Position.TOP_CENTER);
        completeBanner.setDuration(3000);
    }

    private void showTaskDeletedBanner() {
        Notification deletedBanner = Notification.show("Task permanently deleted.");
        deletedBanner.addThemeVariants(NotificationVariant.LUMO_ERROR);
        deletedBanner.setPosition(Notification.Position.TOP_CENTER);
        deletedBanner.setDuration(3000);
    }
}