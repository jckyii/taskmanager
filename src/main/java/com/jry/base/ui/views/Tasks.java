package com.jry.base.ui.views; // Update this if your package structure is different

import com.jry.backend.Task;
import com.jry.backend.TaskRepository;
import com.jry.base.ui.components.TaskGrid;
import com.jry.base.ui.components.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route("")
@PageTitle("My Tasks")
@Menu(order = 1, icon = "vaadin:tasks", title = "My Tasks")
public class Tasks extends VerticalLayout implements BeforeEnterObserver {
    private final TaskRepository taskRepo;

    public Tasks(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;

        TaskGrid grid = new TaskGrid(this.taskRepo.findAll());

        // Navigate to Task Details page when clicking on a row in the grid
        grid.addItemClickListener(click -> {
            Task targetTask = click.getItem();
            getUI().ifPresent(ui -> ui.navigate("tasks/" + targetTask.getId()));
        });


        Button addBtn = new Button("New Task");
        // set mouse to pointer
        addBtn.getElement().setAttribute("style", "cursor: pointer;");
        addBtn.addClickListener(click -> {
            getUI().ifPresent(ui -> ui.navigate("tasks/new")); // programmatically navigate
        });

        // this is the top bar for the page
        ViewToolbar toolbar = new ViewToolbar("Catalogue", addBtn);

        // Add the top bar and the grid to the overall vertical layout


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