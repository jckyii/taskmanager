package com.jry.base.ui.views;

import com.jry.backend.entities.ApplicationUser;
import com.jry.backend.entities.Task;
import com.jry.backend.entities.TaskRepository;
import com.jry.backend.entities.UserRepository;
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
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Route("tasks")
@PermitAll
public class TaskDetails extends VerticalLayout implements HasUrlParameter<Long> {
    private final TaskRepository taskRepo;

    private Task task;
    private final TaskForm taskForm = new TaskForm();
    private Button editBtn = new Button("Edit");
    private Button deleteBtn = new Button("Delete");

    public TaskDetails(TaskRepository taskRepo, UserRepository userRepo, AuthenticationContext authContext) {

        String username = authContext.getAuthenticatedUser(UserDetails.class).get().getUsername();
        ApplicationUser currentUser = userRepo.findByUsername(username).get();
        this.taskRepo = taskRepo;

        taskForm.setEditable(false);
        taskForm.addSaveListener(this::saveTask);

        taskForm.addDeleteSubjectListener(subjectToDelete -> {
            List<Task> allUserTasks = taskRepo.findByUser(currentUser);

            boolean changesMade = false;
            for (Task t : allUserTasks) {
                if (subjectToDelete.equals(t.getSubject())) {
                    t.setSubject(null);
                    taskRepo.save(t);
                    changesMade = true;
                }
            }

            if (changesMade) {
                Notification success = Notification.show("Subject '" + subjectToDelete + "' deleted from all tasks.");
                success.addThemeVariants(NotificationVariant.LUMO_ERROR);
                success.setPosition(Notification.Position.TOP_CENTER);

                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        taskForm.addCancelListener(this::cancelEdit);

        configureLayout();
        configureButtons();
    }

    private void configureLayout() {
        Button backBtn = new Button("Back to All Tasks");
        backBtn.addClickListener(click -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        ViewToolbar toolbar = new ViewToolbar("Task Details", backBtn);
        HorizontalLayout actions = new HorizontalLayout(editBtn, deleteBtn);
        add(toolbar, taskForm, actions);
    }

    private void configureButtons() {
        editBtn.addThemeVariants(ButtonVariant.PRIMARY);
        deleteBtn.addThemeVariants(ButtonVariant.ERROR);

        editBtn.addClickListener(click -> {
            setEditable(true);
        });

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
                () -> beforeEvent.forwardTo("")
        );
    }

    private void setEditable(boolean isEditing) {
        taskForm.setEditable(isEditing);
        editBtn.setEnabled(!isEditing);
        editBtn.setVisible(!isEditing);
        deleteBtn.setEnabled(!isEditing);
        deleteBtn.setVisible(!isEditing);
    }

    private void cancelEdit() {
        setEditable(false);
        taskForm.resetForm();
    }

    // --- UPDATED SAVE WITH BANNERS ---
    private void saveTask(Task task) {
        taskRepo.save(task);

        if (task.isCompleted()) {
            Notification completeBanner = Notification.show("Great job! Task completed. 🎉");
            completeBanner.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            completeBanner.setPosition(Notification.Position.TOP_CENTER);
        } else {
            Notification updateBanner = Notification.show("Task updated successfully.");
            updateBanner.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            updateBanner.setPosition(Notification.Position.TOP_CENTER);
        }

        setEditable(false);
        getUI().ifPresent(ui -> ui.navigate(""));
    }

    // --- UPDATED DELETE WITH BANNERS ---
    private void deleteTask(Task task) {
        taskRepo.delete(task);

        Notification deletedBanner = Notification.show("Task permanently deleted.");
        deletedBanner.addThemeVariants(NotificationVariant.LUMO_ERROR);
        deletedBanner.setPosition(Notification.Position.TOP_CENTER);

        getUI().ifPresent(ui -> ui.navigate(""));
    }
}