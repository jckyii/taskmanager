package com.jry.base.ui.views;

import com.jry.backend.Task;
import com.jry.backend.TaskRepository;
import com.jry.base.ui.components.TaskCardList;
import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

@Route("")
@PageTitle("My Tasks")
@Menu(order = 1, icon = "vaadin:tasks", title = "My Tasks")
public class Tasks extends VerticalLayout implements BeforeEnterObserver {
    private final TaskRepository taskRepo;

    public Tasks(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;

        // Initialize the card list (Click logic is now handled inside this class!)
        TaskCardList grid = new TaskCardList(taskRepo.findAll());

        Button addBtn = new Button("New Task");
        addBtn.getElement().setAttribute("style", "cursor: pointer;");
        addBtn.addClickListener(click -> {
            getUI().ifPresent(ui -> ui.navigate("tasks/new"));
        });

        // Updated title to match your app
        ViewToolbar toolbar = new ViewToolbar("My Tasks", addBtn);

        add(toolbar, grid);
    }

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