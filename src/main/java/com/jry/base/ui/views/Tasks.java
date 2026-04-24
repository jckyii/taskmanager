package com.jry.base.ui.views;

import com.jry.backend.Task;
import com.jry.backend.TaskRepository;
import com.jry.base.ui.components.TaskCard;
import com.jry.base.ui.layouts.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "tasks", layout = MainLayout.class)
@PageTitle("My Tasks")
@Menu(order = 1, icon = "vaadin:tasks", title = "My Tasks")
public class Tasks extends VerticalLayout {

    private final TaskRepository taskRepo;
    private final VerticalLayout taskContainer = new VerticalLayout();

    public Tasks(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;
        setSizeFull();
        setPadding(true);

        H2 pageTitle = new H2("My Tasks");
        pageTitle.getStyle().set("margin-top", "0");

        Button addTaskBtn = new Button("New Task", VaadinIcon.PLUS.create());
        addTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTaskBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("tasks/new")));

        HorizontalLayout header = new HorizontalLayout(pageTitle, addTaskBtn);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        taskContainer.setWidthFull();
        taskContainer.setPadding(false);

        add(header, taskContainer);
        refreshTaskList();
    }

    private void refreshTaskList() {
        taskContainer.removeAll();
        List<Task> allTasks = taskRepo.findAll();

        for (Task task : allTasks) {
            if (!task.isCompleted()) {
                TaskCard card = new TaskCard(task, () -> {
                    task.setCompleted(true);
                    taskRepo.save(task);
                    refreshTaskList();
                });
                taskContainer.add(card);
            }
        }
    }
}