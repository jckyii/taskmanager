package com.jry.base.ui.views;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    // Filter selects are now fields so reloadTasks() can repopulate them when the
    // underlying tasks change (e.g. a new task introduces a brand-new category).
    private final Select<String> categoryFilter = new Select<>();
    private final Select<String> subjectFilter = new Select<>();

    public Tasks(TaskRepository taskRepo, UserRepository userRepo, AuthenticationContext authContext) {
        this.taskRepo = taskRepo;

        // --- 1. GET THE LOGGED IN USER ---
        String userEmail = authContext.getAuthenticatedUser(UserDetails.class)
                .orElseThrow(() -> new IllegalStateException("No authenticated user in session"))
                .getUsername();
        this.currentUser = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("No account found for email: " + userEmail));

        // --- 2. HEADER (via ViewToolbar, so it has the drawer toggle + consistent title
        //         font matching the other views, and the action button on the right).
        //         Sign Out lives in the drawer footer now, so it's no longer here. ---

        Button newTaskBtn = new Button("New Task", e ->
                TaskDialog.openForNew(taskRepo, currentUser, this::reloadTasks));
        newTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        ViewToolbar header = new ViewToolbar("Welcome, " + currentUser.getDisplayName(), newTaskBtn);

        // --- 3. FETCH DATA ---
        List<Task> allTasksInDatabase = taskRepo.findByUser(currentUser);

        // --- 4. BUILD THE GRID ---
        grid = new TaskCardList(allTasksInDatabase, currentUser.getUrgentThresholdHours());
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
        grid.setOnCardClick(task ->
                TaskDialog.openForEdit(taskRepo, currentUser, task, this::reloadTasks));

        // --- 5. BUILD THE TOOLBAR ---
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search tasks...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        // Filter selects start with "All ..." selected; their items are populated by the
        // shared helper below (which also drives reloadTasks() after edits).
        categoryFilter.setValue("All Categories");
        subjectFilter.setValue("All Subjects");
        repopulateFilters(allTasksInDatabase);

        Select<String> groupByFilter = new Select<>();
        groupByFilter.setItems("Group by Status", "Group by Date", "Group by Subject", "Group by Category");

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
        setWidthFull();
        setHeightFull();
        getStyle().set("box-sizing", "border-box");
        setPadding(true);
        getStyle().set("padding-bottom", "48px");
        add(header, toolbar, grid);
    }

    /**
     * Rebuilds the category and subject filter dropdowns from the supplied task list.
     * "All Categories"/"All Subjects" are always first; "Uncategorized" is always present
     * in the category list as a stable landmark; remaining options are derived from the
     * actual tasks and sorted. Preserves the user's current selection if it still exists.
     */
    private void repopulateFilters(List<Task> tasks) {
        // --- Categories: always include "Uncategorized" as a stable option ---
        Set<String> categorySet = new LinkedHashSet<>();
        categorySet.add("Uncategorized");
        tasks.stream()
                .map(Task::getCategory)
                .map(c -> (c == null || c.isEmpty()) ? "Uncategorized" : c)
                .forEach(categorySet::add);
        List<String> sortedCategories = new ArrayList<>(categorySet);
        sortedCategories.sort(String::compareToIgnoreCase);

        List<String> categoryItems = new ArrayList<>();
        categoryItems.add("All Categories");
        categoryItems.addAll(sortedCategories);

        String currentCategory = categoryFilter.getValue();
        categoryFilter.setItems(categoryItems);
        // Preserve the user's selection if it's still valid; otherwise fall back to "All".
        categoryFilter.setValue(categoryItems.contains(currentCategory) ? currentCategory : "All Categories");

        // --- Subjects: data-driven only (no special always-present entry) ---
        List<String> subjectItems = new ArrayList<>();
        subjectItems.add("All Subjects");
        tasks.stream()
                .map(Task::getSubject)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .forEach(subjectItems::add);

        String currentSubject = subjectFilter.getValue();
        subjectFilter.setItems(subjectItems);
        subjectFilter.setValue(subjectItems.contains(currentSubject) ? currentSubject : "All Subjects");
    }

    /** Re-reads this user's tasks from the DB and refreshes the card list AND the filter
     *  dropdowns, so newly created or edited categories/subjects show up immediately. */
    private void reloadTasks() {
        List<Task> latest = taskRepo.findByUser(currentUser);
        grid.refresh(latest);
        repopulateFilters(latest);
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