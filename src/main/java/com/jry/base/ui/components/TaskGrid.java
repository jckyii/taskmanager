package com.jry.base.ui.components;
import com.jry.backend.Task; // Update to your actual Task location
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.apache.commons.lang3.StringUtils; // Assuming you are using Apache Commons, or keep your custom utils

import java.util.List;

public class TaskGrid extends Grid<Task> {
    private final ListDataProvider<Task> dataProvider;

    public TaskGrid(List<Task> allTasks) {
        // 1. Initialize the data provider with Tasks
        dataProvider = new ListDataProvider<>(allTasks);
        setDataProvider(dataProvider);

        // 2. Set up columns specific to a Task
        addColumn(Task::getTitle).setHeader("Task Name").setSortable(true);
        addColumn(Task::getDescription).setHeader("Description");
        addColumn(Task::getDueDate).setHeader("Due Date").setSortable(true);
    }

    public void filter(String searchTerm) {
        // 3. Update the filter to search through relevant Task fields
        dataProvider.setFilter(task ->
                searchTerm == null ||
                        searchTerm.isEmpty() ||
                        StringUtils.containsIgnoreCase(task.getTitle(), searchTerm) ||
                        StringUtils.containsIgnoreCase(task.getDescription(), searchTerm)
        );
    }
}
