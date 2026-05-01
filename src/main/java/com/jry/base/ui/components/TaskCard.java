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
        getStyle().set("border", "1px solid #dcdcdc");
        getStyle().set("border-radius", "12px");
        getStyle().set("padding", "16px");
        getStyle().set("background-color", "#ffffff");
        getStyle().set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");
        getStyle().set("transition", "transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out");

        getElement().addEventListener("mouseenter", e -> {
            getStyle().set("transform", "translateY(-5px)");
            getStyle().set("box-shadow", "0 12px 20px rgba(0, 0, 0, 0.15)");
        });

        getElement().addEventListener("mouseleave", e -> {
            getStyle().set("transform", "translateY(0)");
            getStyle().set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");
        });

        setWidthFull();

        // --- 1. NEW TOP RIGHT SUBJECT PILL ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.END); // Pushes pill to the far right

        if (task.getSubject() != null && !task.getSubject().isEmpty()) {
            Span subjectPill = new Span(task.getSubject());
            subjectPill.getStyle().set("padding", "4px 10px");
            subjectPill.getStyle().set("border-radius", "12px");
            subjectPill.getStyle().set("font-size", "12px");
            subjectPill.getStyle().set("font-weight", "bold");
            subjectPill.getStyle().set("color", "#111827"); // Dark text

            // Apply the color from the database! (Fallback to light gray if null)
            String bgColor = task.getSubjectColor() != null ? task.getSubjectColor() : "#f3f4f6";
            subjectPill.getStyle().set("background-color", bgColor);

            subjectPill.getStyle().set("color", getContrastTextColor(bgColor));

            header.add(subjectPill);
        }

        // --- 2. TEXT FIELDS ---
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

        // --- 3. STATUS BADGE ---
        Span statusBadge = new Span();
        statusBadge.getStyle().set("padding", "4px 10px");
        statusBadge.getStyle().set("border-radius", "12px");
        statusBadge.getStyle().set("font-size", "12px");
        statusBadge.getStyle().set("font-weight", "bold");

        if (task.isCompleted()) {
            statusBadge.setText("Completed");
            statusBadge.getStyle().set("color", "#166534");
            statusBadge.getStyle().set("background-color", "#dcfce7");
        } else {
            statusBadge.setText("Ongoing");
            statusBadge.getStyle().set("color", "#1e3a8a");
            statusBadge.getStyle().set("background-color", "#dbeafe");
        }

        // --- 4. FOOTER ---
        HorizontalLayout footer = new HorizontalLayout(date, statusBadge);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Add the header to the very top!
        add(header, title, description, footer);
    }

    private String getContrastTextColor(String hexColor) {
        // Safety check: ensure it's a valid 7-character hex string (e.g., "#ff0000")
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
            return "#111827"; // Default to dark text
        }

        try {
            // Extract the Red, Green, and Blue values from the hex string
            int r = Integer.valueOf(hexColor.substring(1, 3), 16);
            int g = Integer.valueOf(hexColor.substring(3, 5), 16);
            int b = Integer.valueOf(hexColor.substring(5, 7), 16);

            // Standard formula for perceived brightness
            double brightness = (r * 299 + g * 587 + b * 114) / 1000.0;

            // If brightness is less than 128 (out of 255), it's a dark color. Return white text.
            return brightness < 128 ? "#ffffff" : "#111827";

        } catch (Exception e) {
            // If the hex parsing fails for any reason, default to dark text
            return "#111827";
        }
    }
}