package com.jry.base.ui.components;

import com.jry.backend.entities.Task;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TaskCardList extends VerticalLayout {
    private final List<Task> allTasks;
    private final Consumer<Task> onDelete;

    public TaskCardList(List<Task> allTasks, Consumer<Task> onDelete) {
        this.allTasks = allTasks;
        this.onDelete = onDelete; // Save it
        setWidthFull();
        setPadding(false);
        setSpacing(true);
        updateList("", "All Categories", "All Subjects", "Group by Status");
    }

    public void filter(String searchTerm, String category, String subject, String groupBy) {
        updateList(searchTerm, category, subject, groupBy);
    }

    private void updateList(String searchTerm, String category, String subject, String groupBy) {
        removeAll();
        String lowerTerm = (searchTerm == null) ? "" : searchTerm.toLowerCase();

        // 1. Filter the tasks exactly like before
        List<Task> filteredTasks = allTasks.stream()
                .filter(task -> {
                    boolean matchesText = true;
                    if (!lowerTerm.isEmpty()) {
                        boolean matchesTitle = task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerTerm);
                        boolean matchesDesc = task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerTerm);
                        matchesText = matchesTitle || matchesDesc;
                    }
                    boolean matchesCategory = true;
                    if (category != null && !category.equals("All Categories")) {
                        if (category.equals("Uncategorized")) {
                            // Catch new uncategorized tasks, old "Personal" tasks, and nulls
                            matchesCategory = task.getCategory() == null ||
                                    task.getCategory().isEmpty() ||
                                    task.getCategory().equals("Uncategorized") ||
                                    task.getCategory().equals("Personal");
                        } else {
                            // Standard exact match for Work, School, etc.
                            matchesCategory = task.getCategory() != null && task.getCategory().equals(category);
                        }
                    }
                    boolean matchesSubject = true;
                    if (subject != null && !subject.equals("All Subjects")) {
                        matchesSubject = task.getSubject() != null && task.getSubject().equals(subject);
                    }

                    return matchesText && matchesCategory && matchesSubject;
                })
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // 2. Route to the correct grouping view
        if ("Group by Date".equals(groupBy)) {
            renderGroupByDate(filteredTasks);
        } else if ("Group by Subject".equals(groupBy)) {
            renderGroupBySubject(filteredTasks);
        } else {
            renderGroupByStatus(filteredTasks);
        }
    }

    // ==========================================
    // VIEW 1: GROUP BY STATUS (Original View)
    // ==========================================
    private void renderGroupByStatus(List<Task> tasks) {
        VerticalLayout completedLayout = createInnerLayout();
        VerticalLayout urgentLayout = createInnerLayout();
        VerticalLayout ongoingLayout = createInnerLayout();

        int completedCount = 0, urgentCount = 0, ongoingCount = 0;

        for (Task task : tasks) {
            TaskCard card = createTaskCard(task);
            boolean isPastDue = task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now());

            if (task.isCompleted() || isPastDue) {
                completedLayout.add(card);
                completedCount++;
            } else if (task.isUrgent()) {
                urgentLayout.add(card);
                urgentCount++;
            } else {
                ongoingLayout.add(card);
                ongoingCount++;
            }
        }

        add(createDetailsBar("Completed", completedCount, "#166534", "#dcfce7", completedLayout, false));
        add(createDetailsBar("Urgent", urgentCount, "#991b1b", "#fee2e2", urgentLayout, true));
        add(createDetailsBar("Ongoing", ongoingCount, "#1e3a8a", "#dbeafe", ongoingLayout, true));
    }

    // ==========================================
    // VIEW 2: GROUP BY DATE
    // ==========================================
    private void renderGroupByDate(List<Task> tasks) {
        // Group tasks by their specific LocalDate (ignoring time)
        Map<LocalDate, List<Task>> tasksByDate = new HashMap<>();
        List<Task> noDateTasks = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getDueDate() != null) {
                LocalDate date = task.getDueDate().toLocalDate();
                tasksByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
            } else {
                noDateTasks.add(task);
            }
        }

        // --- 1. NEW: DRAW THE "NO DUE DATE" BUCKET FIRST ---
        if (!noDateTasks.isEmpty()) {
            VerticalLayout noDateLayout = createInnerLayout();
            for (Task task : noDateTasks) {
                noDateLayout.add(createTaskCard(task));
            }

            // set the last parameter to 'false' so this accordion starts CLOSED.
            // This acts like an Inbox and prevents your unscheduled tasks from hiding "Today's" tasks!
            add(createDetailsBar("No Due Date", noDateTasks.size(), "#4b5563", "#e5e7eb", noDateLayout, false));
        }

        // --- 2. THEN DRAW THE REST OF THE DATES CHRONOLOGICALLY ---
        // Sort dates chronologically (closest to today at top)
        List<LocalDate> sortedDates = new ArrayList<>(tasksByDate.keySet());
        Collections.sort(sortedDates);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");

        for (LocalDate date : sortedDates) {
            List<Task> tasksForDay = tasksByDate.get(date);
            VerticalLayout dayLayout = createInnerLayout();
            for (Task task : tasksForDay) {
                dayLayout.add(createTaskCard(task));
            }

            String dateLabel = date.format(formatter);
            if (date.equals(LocalDate.now())) dateLabel = "Today (" + dateLabel + ")";
            if (date.equals(LocalDate.now().plusDays(1))) dateLabel = "Tomorrow (" + dateLabel + ")";

            add(createDetailsBar(dateLabel, tasksForDay.size(), "#1f2937", "#f3f4f6", dayLayout, true));
        }
    }

    // ==========================================
    // VIEW 3: GROUP BY SUBJECT
    // ==========================================
    private void renderGroupBySubject(List<Task> filteredTasks) {
        // Get all unique subjects from the ENTIRE database (so empty ones still show up)
        List<String> allKnownSubjects = allTasks.stream()
                .map(Task::getSubject)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted() // Alphabetical order
                .collect(Collectors.toList());

        for (String subject : allKnownSubjects) {
            VerticalLayout subjectLayout = createInnerLayout();
            int taskCount = 0;

            for (Task task : filteredTasks) {
                if (subject.equals(task.getSubject())) {
                    subjectLayout.add(createTaskCard(task));
                    taskCount++;
                }
            }

            // Grab the color from the first matching task, or default to gray
            String bgColor = "#f3f4f6";
            Optional<Task> firstTask = allTasks.stream().filter(t -> subject.equals(t.getSubject()) && t.getSubjectColor() != null).findFirst();
            if (firstTask.isPresent()) bgColor = firstTask.get().getSubjectColor();

            add(createDetailsBar(subject, taskCount, "#111827", bgColor, subjectLayout, true));
        }

        // Catch-all for tasks that don't have a subject assigned
        List<Task> noSubjectTasks = filteredTasks.stream().filter(t -> t.getSubject() == null || t.getSubject().isEmpty()).collect(Collectors.toList());
        if(!noSubjectTasks.isEmpty()) {
            VerticalLayout noSubLayout = createInnerLayout();
            for(Task t : noSubjectTasks) noSubLayout.add(createTaskCard(t));
            add(createDetailsBar("No Subject", noSubjectTasks.size(), "#4b5563", "#e5e7eb", noSubLayout, true));
        }
    }

    // ==========================================
    // HELPER METHODS FOR CLEAN CODE
    // ==========================================
    private VerticalLayout createInnerLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.getStyle().set("margin-top", "16px");
        return layout;
    }

    private TaskCard createTaskCard(Task task) {
        TaskCard card = new TaskCard(task, this.onDelete);
        card.getStyle().set("cursor", "pointer");
        card.addClickListener(click -> card.getUI().ifPresent(ui -> ui.navigate("tasks/" + task.getId())));
        return card;
    }

    private Details createDetailsBar(String title, int count, String textColor, String bgColor, VerticalLayout content, boolean startOpen) {
        Span summary = new Span(title + " (" + count + ")");
        summary.getStyle().set("font-weight", "bold");
        summary.getStyle().set("font-size", "14px");
        summary.getStyle().set("color", textColor);
        summary.getStyle().set("background-color", bgColor);
        summary.getStyle().set("padding", "6px 12px");
        summary.getStyle().set("border-radius", "8px");
        summary.getStyle().set("border", "1px solid rgba(0, 0, 0, 0.15)");

        Details details = new Details(summary, content);
        details.addThemeVariants(DetailsVariant.REVERSE);
        details.setOpened(startOpen);
        details.setWidthFull();
        details.getStyle().set("border-bottom", "1px solid #e5e7eb");
        details.getStyle().set("padding-bottom", "16px");
        return details;
    }
}