package com.jry.base.ui.views;

import com.jry.backend.entities.Task;
import com.jry.backend.entities.TaskRepository;
import com.jry.base.ui.components.TaskForm;
import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("tasks") // With HasUrlParameter, this makes the URL "localhost:8080/tasks/1"
@PermitAll
public class TaskDetails extends VerticalLayout implements HasUrlParameter<Long> {
    private final TaskRepository taskRepo;

    private Task task;
    private final TaskForm taskForm = new TaskForm();
    private Button editBtn = new Button("Edit");
    private Button deleteBtn = new Button("Delete");

    public TaskDetails(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;

        taskForm.setEditable(false);
        taskForm.addSaveListener(this::saveTask);
        taskForm.addCancelListener(this::cancelEdit);

        configureLayout();
        configureButtons();
    }

    private void configureLayout() {
        Button backBtn = new Button("Back to All Tasks");
        backBtn.addClickListener(click -> {
            // Navigate back to the root dashboard
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        ViewToolbar toolbar = new ViewToolbar("Task Details", backBtn);
        HorizontalLayout actions = new HorizontalLayout(editBtn, deleteBtn);
        add(toolbar, taskForm, actions);
    }

    private void configureButtons() {
        // Style the buttons
        editBtn.addThemeVariants(ButtonVariant.PRIMARY);
        deleteBtn.addThemeVariants(ButtonVariant.ERROR);

        // Logic: Unlock the form when Edit is clicked
        editBtn.addClickListener(click -> {
            setEditable(true);
        });

        // Logic: Show the confirmation pop-up when Delete is clicked
        deleteBtn.addClickListener(click -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Delete Task?");
            confirmDialog.setText("Are you sure you want to delete this task?");
            confirmDialog.setCancelable(true);
            confirmDialog.setConfirmText("Delete");
            confirmDialog.setConfirmButtonTheme("error primary");
            confirmDialog.addConfirmListener(event -> { this.deleteTask(task); });
            confirmDialog.open();
        });
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long taskId) {
        taskRepo.findById(taskId).ifPresentOrElse(
                t -> {
                    task = t;
                    taskForm.setTask(task);
                },
                // Forward back to dashboard if someone types an invalid ID in the URL
                () -> beforeEvent.forwardTo("")
        );
    }

    private void setEditable(boolean isEditing) {
        taskForm.setEditable(isEditing);

        // turn off edit and delete buttons when I am editing
        editBtn.setEnabled(!isEditing);
        editBtn.setVisible(!isEditing);
        deleteBtn.setEnabled(!isEditing);
        deleteBtn.setVisible(!isEditing);
    }

    private void cancelEdit() {
        setEditable(false);
        taskForm.resetForm();
    }

    private void saveTask(Task task) {
        taskRepo.save(task);
        setEditable(false);
        getUI().ifPresent(ui -> ui.navigate(""));
    }

    private void deleteTask(Task task) {
        taskRepo.delete(task);
        getUI().ifPresent(ui -> ui.navigate(""));
    }
}