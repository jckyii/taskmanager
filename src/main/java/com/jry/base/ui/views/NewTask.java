package com.jry.base.ui.views;

import com.jry.backend.entities.Task;
import com.jry.backend.entities.TaskRepository;
import com.jry.base.ui.components.TaskForm;
import com.vaadin.flow.component.button.Button;

import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("tasks/new")
public class NewTask extends VerticalLayout {
    private final TaskRepository taskRepo;
    private final Task task = new Task();
    private final TaskForm taskForm = new TaskForm();
    private final Button backBtn = new Button("Back to All Tasks", VaadinIcon.ARROW_LEFT.create());

    public NewTask(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;

        taskForm.setTask(new Task());
        taskForm.addSaveListener(this::saveTask);
        taskForm.addCancelListener(() -> getUI().ifPresent(ui -> ui.navigate("")));

        add(new ViewToolbar("New Task"), taskForm);
    }

    private void saveTask(Task task) {
        taskRepo.save(task);
        getUI().ifPresent(ui -> ui.navigate(""));
    }
}
