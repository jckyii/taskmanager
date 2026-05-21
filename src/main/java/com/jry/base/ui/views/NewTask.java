package com.jry.base.ui.views;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;

import com.jry.backend.entities.ApplicationUser;
import com.jry.backend.entities.Task;
import com.jry.backend.entities.TaskRepository;
import com.jry.backend.entities.UserRepository;
import com.jry.base.ui.components.TaskForm;
import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.PermitAll;

@Route("tasks/new")
@PermitAll
public class NewTask extends VerticalLayout {
    private final TaskRepository taskRepo;
    private final ApplicationUser currentUser;
    private final Task task = new Task();
    private final TaskForm taskForm = new TaskForm();
    private final Button backBtn = new Button("Back to All Tasks", VaadinIcon.ARROW_LEFT.create());
    


    public NewTask(TaskRepository taskRepo, UserRepository userRepo, AuthenticationContext authContext) {
        this.taskRepo = taskRepo;
        
        //FIX: Grab email from the security context and use findByEmail
        String userEmail = authContext.getAuthenticatedUser(UserDetails.class).get().getUsername();
        this.currentUser = userRepo.findByEmail(userEmail).get();

        //package existing subjects into a map to pass to the form for dropdown population
        Map<String, String> existingSubjectsMap = taskRepo.findByUser(currentUser).stream()
                .filter(t -> t.getSubject() != null && !t.getSubject().isEmpty())
                .collect(Collectors.toMap(
                        Task::getSubject,
                        t -> t.getSubjectColor() != null ? t.getSubjectColor() : "#e5e7eb", // Default to grey if missing
                        (color1, color2) -> color1 // If duplicate subjects exist, keep the first color
                ));

        Map<String, String> existingCategoriesMap = taskRepo.findByUser(currentUser).stream()
                .filter(t -> t.getCategory() != null && !t.getCategory().isEmpty())
                .collect(Collectors.toMap(
                        Task::getCategory,
                        t -> t.getCategoryColor() != null ? t.getCategoryColor() : "#fef3c7", // Default yellow
                        (color1, color2) -> color1
                ));

        taskForm.setTask(new Task());
        //pass map into form to populate subject dropdown with existing subjects and their colors
        taskForm.setExistingSubjects(existingSubjectsMap);
        taskForm.setExistingCategories(existingCategoriesMap);
        taskForm.addSaveListener(this::saveTask);
        taskForm.addCancelListener(() -> getUI().ifPresent(ui -> ui.navigate("")));

        add(new ViewToolbar("New Task"), taskForm);
    }

    private void saveTask(Task task) {
        task.setUser(this.currentUser);
        taskRepo.save(task);
        getUI().ifPresent(ui -> ui.navigate(""));
    }
}