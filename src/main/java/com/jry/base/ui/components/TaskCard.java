package com.jry.base.ui.components;

import com.jry.backend.Task;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

// Notice: We don't even need to import LumoUtility anymore!

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TaskCard extends VerticalLayout {
    // Changed HH to hh to fix the 24-hour clock issue!
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a", Locale.US);

    public TaskCard(Task task) {
        // Style the box (Rounded corners, shadow, border, background)
        getStyle().set("border", "1px solid #dcdcdc");
        getStyle().set("border-radius", "12px");
        getStyle().set("padding", "16px");
        getStyle().set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");
        getStyle().set("background-color", "#ffffff");
        setWidthFull(); // Stretches the box across the screen

        // Title
        H3 title = new H3(task.getTitle());
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "8px");

        // Description
        Paragraph description = new Paragraph(task.getDescription());
        description.getStyle().set("color", "#666666"); // Secondary gray
        description.getStyle().set("margin-top", "0");

        // Due Date
        String dateString = task.getDueDate() != null ? task.getDueDate().format(DATE_TIME_FORMATTER) : "No due date";
        Span date = new Span("Due: " + dateString);
        date.getStyle().set("font-size", "14px");
        date.getStyle().set("color", "#888888"); // Tertiary light gray

        // Status
        String statusString = task.isCompleted() ? "Done" : "Pending";
        Span status = new Span(statusString);
        status.getStyle().set("font-size", "14px");
        status.getStyle().set("font-weight", "bold");

        // Make "Done" green and "Pending" primary blue
        if(task.isCompleted()) {
            status.getStyle().set("color", "#16764b"); // Success Green
        } else {
            status.getStyle().set("color", "#006af5"); // Primary Blue
        }

        // Put Date and Status next to each other at the bottom
        HorizontalLayout footer = new HorizontalLayout(date, status);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN); // Pushes them to opposite sides

        // Add everything to the box
        add(title, description, footer);
    }
}