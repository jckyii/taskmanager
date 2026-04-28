package com.jry.base.ui.components;

import com.jry.backend.Task;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TaskCard extends VerticalLayout {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a", Locale.US);

    public TaskCard(Task task) {
        // --- 1. BASE BOX STYLING ---
        getStyle().set("border", "1px solid #dcdcdc");
        getStyle().set("border-radius", "12px");
        getStyle().set("padding", "16px");
        getStyle().set("background-color", "#ffffff");

        // Base shadow and transition speed for the animation
        getStyle().set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");
        getStyle().set("transition", "transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out");

        // --- 2. HOVER ANIMATION LOGIC ---
        // When mouse enters: Lift the card up 5 pixels and make the shadow larger
        getElement().addEventListener("mouseenter", e -> {
            getStyle().set("transform", "translateY(-5px)");
            getStyle().set("box-shadow", "0 12px 20px rgba(0, 0, 0, 0.15)");
        });

        // When mouse leaves: Put the card back to normal
        getElement().addEventListener("mouseleave", e -> {
            getStyle().set("transform", "translateY(0)");
            getStyle().set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");
        });

        setWidthFull();

        // --- 3. TEXT FIELDS ---
        H3 title = new H3(task.getTitle());
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "8px");

        Paragraph description = new Paragraph(task.getDescription());
        description.getStyle().set("color", "#666666");
        description.getStyle().set("margin-top", "0");

        String dateString = task.getDueDate() != null ? task.getDueDate().format(DATE_TIME_FORMATTER) : "No due date";
        Span date = new Span("Due: " + dateString);
        date.getStyle().set("font-size", "14px");
        date.getStyle().set("color", "#888888");

        // --- 4. SOFT BADGES ---
        Span statusBadge = new Span();
        statusBadge.getStyle().set("padding", "4px 10px");
        statusBadge.getStyle().set("border-radius", "12px");
        statusBadge.getStyle().set("font-size", "12px");
        statusBadge.getStyle().set("font-weight", "bold");

        // Apply "Soft" Colors (Light background, dark text of the same color)
        if (task.isCompleted()) {
            statusBadge.setText("Completed");
            statusBadge.getStyle().set("color", "#166534"); // Dark Green Text
            statusBadge.getStyle().set("background-color", "#dcfce7"); // Light Green BG
        } else {
            statusBadge.setText("Ongoing");
            statusBadge.getStyle().set("color", "#1e3a8a"); // Dark Blue Text
            statusBadge.getStyle().set("background-color", "#dbeafe"); // Light Blue BG
        }

        if (task.getSubject() != null && !task.getSubject().isEmpty()) {
            Span subjectTag = new Span(task.getSubject());
            subjectTag.getStyle().set("font-size", "12px");
            subjectTag.getStyle().set("color", "#6b7280"); // Gray text
            add(subjectTag); // Add it wherever you want it to appear!
        }

        // --- 5. FOOTER ---
        HorizontalLayout footer = new HorizontalLayout(date, statusBadge);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        add(title, description, footer);
    }
}