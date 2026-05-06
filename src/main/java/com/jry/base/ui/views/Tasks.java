package com.jry.base.ui.views;

import com.jry.backend.Task;
import com.jry.backend.TaskRepository;
import com.jry.base.ui.components.TaskCardList;
import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.component.page.WebStorage;

import java.util.List;
import java.util.stream.Collectors;

@Route("")
@PageTitle("My Tasks")
@Menu(order = 1, icon = "vaadin:tasks", title = "My Tasks")
public class Tasks extends VerticalLayout implements BeforeEnterObserver {
    private final TaskRepository taskRepo;

    public Tasks(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;

        List<Task> allTasksInDatabase = taskRepo.findAll();
        TaskCardList grid = new TaskCardList(allTasksInDatabase);

        Button addBtn = new Button("New Task");
        addBtn.getElement().setAttribute("style", "cursor: pointer;");
        addBtn.addClickListener(click -> {
            getUI().ifPresent(ui -> ui.navigate("tasks/new"));
        });

        ViewToolbar toolbar = new ViewToolbar("My Tasks", addBtn);

        // --- THE NEW FILTER BAR ---

        ComboBox<String> groupByFilter = new ComboBox<>();
        groupByFilter.setItems("Group by Status", "Group by Date", "Group by Subject");

        // --- 1. THE MAGIC: LOAD PREFERENCE FROM BROWSER MEMORY ---
        WebStorage.getItem("task-group-pref", savedPref -> {
            if (savedPref != null && !savedPref.isEmpty()) {
                groupByFilter.setValue(savedPref);
            } else {
                groupByFilter.setValue("Group by Status");
            }
        });
        // ---------------------------------------------------------

        TextField searchFilter = new TextField();
        searchFilter.setPlaceholder("Search tasks...");
        searchFilter.setClearButtonVisible(true);
        searchFilter.setValueChangeMode(ValueChangeMode.LAZY);

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.setItems("All Categories", "Work", "School", "Home", "Casual");
        categoryFilter.setValue("All Categories");

        List<String> existingSubjects = allTasksInDatabase.stream()
                .map(Task::getSubject)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        existingSubjects.add(0, "All Subjects");

        ComboBox<String> subjectFilter = new ComboBox<>();
        subjectFilter.setItems(existingSubjects);
        subjectFilter.setValue("All Subjects");

        HorizontalLayout filterLayout = new HorizontalLayout(groupByFilter, searchFilter, categoryFilter, subjectFilter);
        filterLayout.setWidthFull();
        filterLayout.setPadding(false);
        filterLayout.getStyle().set("padding-left", "16px");

        // --- WIRE UP THE LISTENERS ---

        // --- 2. THE MAGIC: SAVE PREFERENCE WHEN CHANGED ---
        groupByFilter.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                WebStorage.setItem("task-group-pref", e.getValue());
            }
            grid.filter(searchFilter.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue());
        });
        // --------------------------------------------------

        searchFilter.addValueChangeListener(e -> grid.filter(searchFilter.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        categoryFilter.addValueChangeListener(e -> grid.filter(searchFilter.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));
        subjectFilter.addValueChangeListener(e -> grid.filter(searchFilter.getValue(), categoryFilter.getValue(), subjectFilter.getValue(), groupByFilter.getValue()));

        add(toolbar, filterLayout, grid);
    }

    // --- THIS IS THE MISSING METHOD THAT FIXES YOUR ERROR! ---
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        var queryParams = beforeEnterEvent.getLocation().getQueryParameters().getParameters();
        if(queryParams.containsKey("message")) {
            String message = queryParams.get("message").getFirst();
            switch (message) {
                case "created":
                    Notification.show("Task created!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                case "deleted":
                    Notification.show("Task deleted!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    break;
                default:
                    break;
            }
        }
    }
}