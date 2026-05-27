package com.jry.base.ui.components;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jry.backend.entities.Task;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class TaskCardList extends VerticalLayout {
    private final List<Task> allTasks;
    private Consumer<Task> onDelete;
    private Consumer<Task> onComplete;
    private Consumer<Task> onCardClick;
    private final int urgentThresholdHours;

    public TaskCardList(List<Task> allTasks, int urgentThresholdHours) {
        this.allTasks = allTasks;
        this.urgentThresholdHours = urgentThresholdHours;
        setWidthFull();
        setPadding(false);
        setSpacing(true);
        // Default initial load
        updateList("", "All Categories", "All Subjects", "Group by Status");
    }

    public void setOnDelete(Consumer<Task> onDelete) {
        this.onDelete = onDelete;
    }

    public void setOnComplete(Consumer<Task> onComplete) {
        this.onComplete = onComplete;
    }

    /**
     * Optional: when set, clicking a card fires this callback (e.g. to open an edit dialog)
     * instead of navigating to the task's detail page. When not set, cards fall back to
     * navigating to "tasks/{id}".
     */
    public void setOnCardClick(Consumer<Task> onCardClick) {
        this.onCardClick = onCardClick;
    }


    public void refresh(List<Task> updatedTasks) {
        this.allTasks.clear();
        this.allTasks.addAll(updatedTasks);
        updateList("", "All Categories", "All Subjects", "Group by Status");
    }

    public void filter(String searchTerm, String category, String subject, String groupBy) {
        updateList(searchTerm, category, subject, groupBy);
    }

    private void updateList(String searchTerm, String category, String subject, String groupBy) {
        removeAll(); // Clear the view
        String lowerTerm = (searchTerm == null) ? "" : searchTerm.toLowerCase();

        //get all tasks that match the search/filters
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
                            matchesCategory = task.getCategory() == null ||
                                    task.getCategory().isEmpty() ||
                                    task.getCategory().equals("Uncategorized") ||
                                    task.getCategory().equals("Personal");
                        } else {
                            matchesCategory = task.getCategory() != null && task.getCategory().equals(category);
                        }
                    }
                    boolean matchesSubject = true;
                    if (subject != null && !subject.equals("All Subjects")) {
                        matchesSubject = task.getSubject() != null && task.getSubject().equals(subject);
                    }
                    return matchesText && matchesCategory && matchesSubject;
                })
                .collect(Collectors.toList());//keep because otherwise tolist is immutable

        // 2. SPLIT COMPLETED VS ACTIVE
        List<Task> completedTasks = new ArrayList<>();
        List<Task> activeTasks = new ArrayList<>();

        for (Task t : filteredTasks) {
            if (t.isCompleted()) {
                completedTasks.add(t);
            } else {
                activeTasks.add(t);
            }
        }

        // 3. SORT ACTIVE TASKS CHRONOLOGICALLY
        activeTasks.sort((t1, t2) -> {
            if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
            if (t1.getDueDate() == null) return 1;
            if (t2.getDueDate() == null) return -1;
            return t1.getDueDate().compareTo(t2.getDueDate());
        });

        // 4. ALWAYS RENDER COMPLETED TASKS AT THE ABSOLUTE TOP (COLLAPSED)
        if (!completedTasks.isEmpty()) {
            VerticalLayout completedLayout = createInnerLayout();
            // Sort completed tasks so the most recently completed are near the top
            for (Task task : completedTasks) {
                completedLayout.add(createTaskCard(task));
            }
            // FALSE means it will start collapsed!
            add(createDetailsBar("Completed", completedTasks.size(), "#166534", "#dcfce7", completedLayout, false));
        }

        // 5. RENDER THE REST OF THE GROUPS BASED ON USER CHOICE
        if (activeTasks.isEmpty()) {
            return; // If there are no active tasks, stop drawing.
        }

        if ("Group by Date".equals(groupBy)) {
            renderGroupByDate(activeTasks);
        } else if ("Group by Subject".equals(groupBy)) {
            renderGroupBySubject(activeTasks);
        } else if ("Group by Category".equals(groupBy)) {
            renderGroupByCategory(activeTasks);
        } else {
            renderGroupByStatus(activeTasks);
        }
    }

    // ==========================================
    // VIEW 1: GROUP BY STATUS
    // ==========================================
    private void renderGroupByStatus(List<Task> activeTasks) {
        VerticalLayout urgentLayout = createInnerLayout();
        VerticalLayout ongoingLayout = createInnerLayout();

        int urgentCount = 0, ongoingCount = 0;

        for (Task task : activeTasks) {
            boolean isPastDue = task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now());

            if (task.isUrgent(urgentThresholdHours) || isPastDue) {
                urgentLayout.add(createTaskCard(task));
                urgentCount++;
            } else {
                ongoingLayout.add(createTaskCard(task));
                ongoingCount++;
            }
        }

        if (urgentCount > 0) {
            add(createDetailsBar("Urgent / Overdue", urgentCount, "#991b1b", "#fee2e2", urgentLayout, true));
        }
        if (ongoingCount > 0) {
            add(createDetailsBar("Ongoing", ongoingCount, "#1e3a8a", "#dbeafe", ongoingLayout, true));
        }
    }

    // ==========================================
    // VIEW 2: GROUP BY DATE (Strict Timeline Fix)
    // ==========================================
    private void renderGroupByDate(List<Task> activeTasks) {
        Map<LocalDate, List<Task>> tasksByDate = new HashMap<>();
        List<Task> noDateTasks = new ArrayList<>();

        for (Task task : activeTasks) {
            if (task.getDueDate() != null) {
                LocalDate date = task.getDueDate().toLocalDate();
                tasksByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
            } else {
                noDateTasks.add(task);
            }
        }

        // Draw "No Due Date" first so it doesn't get buried
        if (!noDateTasks.isEmpty()) {
            VerticalLayout noDateLayout = createInnerLayout();
            for (Task task : noDateTasks) {
                noDateLayout.add(createTaskCard(task));
            }
            add(createDetailsBar("No Due Date", noDateTasks.size(), "#4b5563", "#e5e7eb", noDateLayout, false));
        }

        // STRICT CHRONOLOGICAL SORTING FIX
        List<LocalDate> sortedDates = new ArrayList<>(tasksByDate.keySet());
        sortedDates.sort(Comparator.naturalOrder()); // Guarantees past -> present -> future

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");

        for (LocalDate date : sortedDates) {
            List<Task> tasksForDay = tasksByDate.get(date);

            // Secondary sort: Sort by exact time within that specific day
            tasksForDay.sort(Comparator.comparing(Task::getDueDate));

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
    private void renderGroupBySubject(List<Task> activeTasks) {
        List<String> allKnownSubjects = allTasks.stream()
                .map(Task::getSubject)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (String subject : allKnownSubjects) {
            VerticalLayout subjectLayout = createInnerLayout();
            int taskCount = 0;

            for (Task task : activeTasks) {
                if (subject.equals(task.getSubject())) {
                    subjectLayout.add(createTaskCard(task));
                    taskCount++;
                }
            }

            // Only draw the accordion if there are actually active tasks for it
            if (taskCount > 0) {
                String bgColor = "#f3f4f6";
                Optional<Task> firstTask = allTasks.stream().filter(t -> subject.equals(t.getSubject()) && t.getSubjectColor() != null).findFirst();
                if (firstTask.isPresent()) bgColor = firstTask.get().getSubjectColor();

                add(createDetailsBar(subject, taskCount, "#111827", bgColor, subjectLayout, true));
            }
        }

        List<Task> noSubjectTasks = activeTasks.stream().filter(t -> t.getSubject() == null || t.getSubject().isEmpty()).collect(Collectors.toList());
        if (!noSubjectTasks.isEmpty()) {
            VerticalLayout noSubLayout = createInnerLayout();
            for(Task t : noSubjectTasks) noSubLayout.add(createTaskCard(t));
            add(createDetailsBar("No Subject", noSubjectTasks.size(), "#4b5563", "#e5e7eb", noSubLayout, true));
        }
    }

    // ==========================================
    // VIEW 4: GROUP BY CATEGORY
    // ==========================================
    private void renderGroupByCategory(List<Task> activeTasks) {
        // Distinct, sorted list of categories actually present on the user's tasks
        // (treating null/empty as "Uncategorized"). Only categories that have tasks are
        // rendered as accordions — empty categories are intentionally omitted (less clutter).
        List<String> allKnownCategories = allTasks.stream()
                .map(Task::getCategory)
                .map(c -> (c == null || c.isEmpty()) ? "Uncategorized" : c)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (String category : allKnownCategories) {
            VerticalLayout categoryLayout = createInnerLayout();
            int taskCount = 0;

            for (Task task : activeTasks) {
                String taskCategory = (task.getCategory() == null || task.getCategory().isEmpty())
                        ? "Uncategorized"
                        : task.getCategory();
                if (category.equals(taskCategory)) {
                    categoryLayout.add(createTaskCard(task));
                    taskCount++;
                }
            }

            // Skip empty categories — only show ones with at least one active task.
            if (taskCount > 0) {
                String bgColor = "#fef3c7"; // default light yellow, matches Task's default category color
                Optional<Task> firstTask = allTasks.stream()
                        .filter(t -> {
                            String tc = (t.getCategory() == null || t.getCategory().isEmpty()) ? "Uncategorized" : t.getCategory();
                            return category.equals(tc) && t.getCategoryColor() != null;
                        })
                        .findFirst();
                if (firstTask.isPresent()) bgColor = firstTask.get().getCategoryColor();

                add(createDetailsBar(category, taskCount, "#111827", bgColor, categoryLayout, true));
            }
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    private VerticalLayout createInnerLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.getStyle().set("margin-top", "16px");
        return layout;
    }

    private TaskCard createTaskCard(Task task) {
        TaskCard card = new TaskCard(task, this.onComplete, this.onDelete, this.urgentThresholdHours);
        card.getStyle().set("cursor", "pointer");
        card.addClickListener(click -> {
            if (onCardClick != null) {
                onCardClick.accept(task);
            } else {
                card.getUI().ifPresent(ui -> ui.navigate("tasks/" + task.getId()));
            }
        });
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