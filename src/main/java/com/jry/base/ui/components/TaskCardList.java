package com.jry.base.ui.components;

import com.jry.backend.Task;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.stream.Collectors;

public class TaskCardList extends VerticalLayout {
    private final List<Task> allTasks;

    public TaskCardList(List<Task> allTasks) {
        this.allTasks = allTasks;

        setWidthFull();
        setPadding(false); // Removes extra padding on the sides
        setSpacing(true);  // Puts space between each card

        // Draw the initial list
        updateList("");
    }

    // Your search bar will call this method
    public void filter(String searchTerm) {
        updateList(searchTerm);
    }

    private void updateList(String searchTerm) {
        removeAll(); // Clear the old cards from the screen

        if (allTasks.isEmpty()) {
            add(new com.vaadin.flow.component.html.H3("DATABASE IS EMPTY! 0 TASKS FOUND."));
            return; // Stop running the rest of the method
        }

        String lowerTerm = (searchTerm == null) ? "" : searchTerm.toLowerCase();

        // Find all tasks that match the search
        List<Task> filteredTasks = allTasks.stream()
                .filter(task -> {
                    if (lowerTerm.isEmpty()) return true;

                    boolean matchesTitle = task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerTerm);
                    boolean matchesDesc = task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerTerm);

                    return matchesTitle || matchesDesc;
                })
                .collect(Collectors.toList());

        // Create a new TaskCard for every matched task and add it to the screen
        for (Task task : filteredTasks) {
            TaskCard card = new TaskCard(task);

            // --- NEW CLICK LOGIC ---
            // Make the mouse look like a clicking hand when hovering over the card
            card.getStyle().set("cursor", "pointer");

            // When the card is clicked, navigate to the TaskDetails view
            card.addClickListener(click -> {
                card.getUI().ifPresent(ui -> ui.navigate("tasks/" + task.getId()));
            });
            // -----------------------

            add(card); // Add it to the screen
        }
    }
}