package com.jry.base.ui.views;

import com.jry.backend.Task;
import com.jry.backend.TaskRepository;
import com.jry.base.ui.components.TaskForm;
import com.jry.base.ui.layouts.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.Instant;

@Route(value = "tasks/new", layout = MainLayout.class)
@PageTitle("Create New Task")
public class NewTask extends VerticalLayout {

    private final TaskRepository taskRepo;

    public NewTask(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;

        H2 title = new H2("Create a New Task");

        TaskForm taskForm = new TaskForm();

        // We create an empty task, but assign it the exact current time for creationDate
        Task newTask = new Task("", "", Instant.now());
        taskForm.setTask(newTask);

        taskForm.addSaveListener(task -> {
            taskRepo.save(task); // Save to Supabase
            getUI().ifPresent(ui -> ui.navigate("tasks")); // Go back to dashboard
        });

        taskForm.addCancelListener(() -> {
            getUI().ifPresent(ui -> ui.navigate("tasks"));
        });

        add(title, taskForm);
    }
}