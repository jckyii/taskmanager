package com.jry.base.ui.components;

import com.jry.backend.entities.Task;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

public class TaskCard extends VerticalLayout {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy hh:mm a", Locale.US);

    public TaskCard(Task task, Consumer<Task> onComplete, Consumer<Task> onDelete) {
        this.getStyle().set("position", "relative");
        addClassName("task-card");

        // --- 1. STRICT STATUS LOGIC ---
        boolean isCompleted = task.isCompleted();
        boolean isPastDue = task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now()) && !isCompleted;
        boolean isUrgent = task.isUrgent() && !isCompleted;

        String cardBackground = "#ffffff";
        if (isCompleted) {
            cardBackground = "#f0fdf4"; // Green
        } else if (isPastDue) {
            cardBackground = "#fef2f2"; // Red
        } else if (isUrgent) {
            cardBackground = "#fff7ed"; // Orange
        }

        // --- 2. DYNAMIC BASE STYLING (Collapsed vs Expanded) ---
        getStyle().set("border", "1px solid #dcdcdc");
        getStyle().set("border-radius", "12px");
        getStyle().set("padding", isCompleted ? "8px 14px" : "14px");
        getStyle().set("background-color", cardBackground);
        getStyle().set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");
        getStyle().set("transition", "all 0.2s ease-in-out");

        getElement().addEventListener("mouseenter", e -> {
            getStyle().set("transform", "translateY(-4px)");
            getStyle().set("box-shadow", "0 10px 15px rgba(0, 0, 0, 0.1)");
        });

        getElement().addEventListener("mouseleave", e -> {
            getStyle().set("transform", "translateY(0)");
            getStyle().set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");
        });

        setWidthFull();
        setSpacing(false);

        if (!isCompleted) {
            this.setMinHeight("140px");
        }

        // --- 3. TOP RIGHT SUBJECT PILL ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.END);
        header.setMinHeight(isCompleted ? "20px" : "28px");

        if (task.getSubject() != null && !task.getSubject().isEmpty()) {
            Span subjectPill = new Span(task.getSubject());
            subjectPill.getStyle().set("padding", "4px 10px");
            subjectPill.getStyle().set("border-radius", "12px");
            subjectPill.getStyle().set("font-size", "12px");
            subjectPill.getStyle().set("font-weight", "bold");
            subjectPill.getStyle().set("border", "1px solid rgba(0, 0, 0, 0.15)");

            String bgColor = task.getSubjectColor() != null ? task.getSubjectColor() : "#f3f4f6";
            subjectPill.getStyle().set("background-color", bgColor);
            subjectPill.getStyle().set("color", getContrastTextColor(bgColor));

            header.add(subjectPill);
        }

        // --- 4. TEXT FIELDS ---
        VerticalLayout topContent = new VerticalLayout();
        topContent.setPadding(false);
        topContent.setSpacing(false);

        if (isCompleted) {
            H4 compactTitle = new H4(task.getTitle());
            compactTitle.getStyle().set("margin-top", "-20px");
            compactTitle.getStyle().set("margin-bottom", "8px");
            compactTitle.getStyle().set("padding-right", "50px");
            topContent.add(header, compactTitle);
        } else {
            H3 title = new H3(task.getTitle());
            title.getStyle().set("margin-top", "-8px");
            title.getStyle().set("margin-bottom", "4px");
            title.getStyle().set("padding-right", "50px");
            topContent.add(header, title);

            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                Paragraph description = new Paragraph(task.getDescription());
                description.getStyle().set("color", "#666666");
                description.getStyle().set("margin-top", "0");
                description.getStyle().set("margin-bottom", "12px");
                description.getStyle().set("padding-right", "50px");
                topContent.add(description);
            }
        }

        // --- 5. CATEGORY BADGE ---
        Span categoryBadge = new Span(task.getCategory() != null ? task.getCategory() : "Personal");
        categoryBadge.getStyle().set("padding", "4px 10px");
        categoryBadge.getStyle().set("border-radius", "12px");
        categoryBadge.getStyle().set("font-size", "12px");
        categoryBadge.getStyle().set("font-weight", "bold");
        categoryBadge.getStyle().set("border", "1px solid rgba(0, 0, 0, 0.15)");

        switch (categoryBadge.getText()) {
            case "Work":
                categoryBadge.getStyle().set("background-color", "#e2e8f0");
                categoryBadge.getStyle().set("color", "#1e293b");
                break;
            case "School":
                categoryBadge.getStyle().set("background-color", "#f3e8ff");
                categoryBadge.getStyle().set("color", "#6b21a8");
                break;
            case "Home":
                categoryBadge.getStyle().set("background-color", "#ccfbf1");
                categoryBadge.getStyle().set("color", "#115e59");
                break;
            case "Casual":
            default:
                categoryBadge.getStyle().set("background-color", "#fef3c7");
                categoryBadge.getStyle().set("color", "#92400e");
                break;
        }

        // --- 6. STATUS BADGE ---
        Span statusBadge = new Span();
        statusBadge.getStyle().set("padding", "4px 10px");
        statusBadge.getStyle().set("border-radius", "12px");
        statusBadge.getStyle().set("font-size", "12px");
        statusBadge.getStyle().set("font-weight", "bold");
        statusBadge.getStyle().set("border", "1px solid rgba(0, 0, 0, 0.15)");

        if (isCompleted) {
            statusBadge.setText("Completed");
            statusBadge.getStyle().set("color", "#166534");
            statusBadge.getStyle().set("background-color", "#dcfce7");
        } else if (isPastDue) {
            statusBadge.setText("Overdue");
            statusBadge.getStyle().set("color", "#991b1b");
            statusBadge.getStyle().set("background-color", "#fecaca");
        } else if (isUrgent) {
            statusBadge.setText("Urgent");
            statusBadge.getStyle().set("color", "#9a3412");
            statusBadge.getStyle().set("background-color", "#ffedd5");
        } else {
            statusBadge.setText("Ongoing");
            statusBadge.getStyle().set("color", "#1e3a8a");
            statusBadge.getStyle().set("background-color", "#dbeafe");
        }

        String dateString = task.getDueDate() != null ? task.getDueDate().format(DATE_TIME_FORMATTER) : "No due date";
        Span date = new Span("Due: " + dateString);
        date.getStyle().set("font-size", "14px");
        date.getStyle().set("color", "#888888");

        // --- 7. THE FLOATING BUTTONS ---
        Button actionBtn = new Button();
        Icon actionIcon;

        if (!isCompleted) {
            actionIcon = VaadinIcon.CHECK.create();
            actionIcon.setSize("24px");
            actionBtn.setIcon(actionIcon);
            actionBtn.getStyle().set("background-color", "#e5e7eb");
            actionIcon.setColor("#4b5563");
            actionBtn.getStyle().set("border", "1px solid #d1d5db");

            actionBtn.getElement().addEventListener("mouseenter", e -> {
                actionBtn.getStyle().set("background-color", "#22c55e");
                actionIcon.setColor("#ffffff");
                actionBtn.getStyle().set("border-color", "#22c55e");
            });
            actionBtn.getElement().addEventListener("mouseleave", e -> {
                actionBtn.getStyle().set("background-color", "#e5e7eb");
                actionIcon.setColor("#4b5563");
                actionBtn.getStyle().set("border-color", "#d1d5db");
            });

            actionBtn.addClickListener(e -> onComplete.accept(task));

        } else {
            actionIcon = VaadinIcon.TRASH.create();
            actionIcon.setSize("24px");
            actionBtn.setIcon(actionIcon);
            actionBtn.getStyle().set("background-color", "#e5e7eb");
            actionIcon.setColor("#4b5563");
            actionBtn.getStyle().set("border", "1px solid #d1d5db");

            actionBtn.getElement().addEventListener("mouseenter", e -> {
                actionBtn.getStyle().set("background-color", "#ef4444");
                actionIcon.setColor("#ffffff");
                actionBtn.getStyle().set("border-color", "#ef4444");
            });
            actionBtn.getElement().addEventListener("mouseleave", e -> {
                actionBtn.getStyle().set("background-color", "#e5e7eb");
                actionIcon.setColor("#4b5563");
                actionBtn.getStyle().set("border-color", "#d1d5db");
            });

            actionBtn.addClickListener(e -> onDelete.accept(task));
        }

        actionBtn.getStyle().set("cursor", "pointer");
        actionBtn.getStyle().set("padding", "12px");
        actionBtn.getStyle().set("border-radius", "8px");
        actionBtn.getStyle().set("position", "absolute");
        actionBtn.getStyle().set("right", "16px");
        actionBtn.getStyle().set("top", "47%");
        actionBtn.getStyle().set("transform", "translateY(-50%)");
        actionBtn.getStyle().set("z-index", "10");
        actionBtn.getStyle().set("transition", "all 0.2s ease");
        add(actionBtn);

        // --- 8. FOOTER LAYOUT ---
        HorizontalLayout rightBadges = new HorizontalLayout(categoryBadge, statusBadge);
        rightBadges.setAlignItems(Alignment.CENTER);
        rightBadges.getStyle().set("gap", "8px");

        HorizontalLayout footer = new HorizontalLayout(date, rightBadges);
        footer.setWidthFull();
        footer.setAlignItems(Alignment.CENTER);
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        add(topContent, footer);

        if (!isCompleted) {
            expand(topContent);
        }
    }

    private String getContrastTextColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) return "#111827";
        try {
            int r = Integer.valueOf(hexColor.substring(1, 3), 16);
            int g = Integer.valueOf(hexColor.substring(3, 5), 16);
            int b = Integer.valueOf(hexColor.substring(5, 7), 16);
            double brightness = (r * 299 + g * 587 + b * 114) / 1000.0;
            return brightness < 128 ? "#ffffff" : "#111827";
        } catch (Exception e) { return "#111827"; }
    }
}