//package com.jry.base.ui.components;
//
//import com.jry.backend.Task;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.data.provider.ListDataProvider;
//
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Locale;
//
//public class TaskGrid extends Grid<Task> {
//    private final ListDataProvider<Task> dataProvider;
//    public static final DateTimeFormatter DATE_TIME_FORMATTER  = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm a", Locale.US);
//
//    public TaskGrid(List<Task> allTasks) {
//        dataProvider = new ListDataProvider<>(allTasks);
//
//        setDataProvider(dataProvider);
//
//        // Map columns to your Task object properties
//        addColumn(Task::getTitle).setHeader("Title").setSortable(true);
//        addColumn(Task::getDescription).setHeader("Description");
//
//        // Handle the due date (checking if it is null so the app doesn't crash)
//        addColumn(task -> task.getDueDate() != null ? task.getDueDate().format(DATE_TIME_FORMATTER) : "No due date")
//                .setHeader("Due Date")
//                .setSortable(true);
//
//        // Convert the boolean into a readable string
//        addColumn(task -> task.isCompleted() ? "Done" : "Pending")
//                .setHeader("Status")
//                .setSortable(true);
//
//
//    }
//
//    // Standard Java filter that searches both Title and Description
//    public void filter(String searchTerm) {
//        dataProvider.setFilter(task -> {
//            if (searchTerm == null || searchTerm.isEmpty()) {
//                return true;
//            }
//            String lowerTerm = searchTerm.toLowerCase();
//
//            boolean matchesTitle = task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerTerm);
//            boolean matchesDesc = task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerTerm);
//
//            return matchesTitle || matchesDesc;
//        });
//    }
//}