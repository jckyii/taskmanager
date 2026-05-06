package com.jry.base.ui.components;

import com.jry.backend.Task;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TaskCard extends VerticalLayout {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a", Locale.US);

    public TaskCard(Task task) {
        // --- 1. DETERMINE CARD TINT & STATUS ---
        // Treat it as completed if the checkbox is ticked OR if it's past the due date
        boolean isPastDue = task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now());
        boolean isEffectivelyCompleted = task.isCompleted() || isPastDue;

        String cardBackground = "#ffffff"; // Default White
        if (isEffectivelyCompleted) {
            cardBackground = "#f0fdf4"; // Very light green tint
        } else if (task.isUrgent()) {
            cardBackground = "#fef2f2"; // Very light red tint
        }

        // --- 2. BASE BOX STYLING ---
        getStyle().set("border", "1px solid #dcdcdc");
        getStyle().set("border-radius", "12px");
        getStyle().set("padding", "14px");
        getStyle().set("background-color", cardBackground); // Apply our dynamic tint!
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
        setSpacing(false);

        // --- 3. TOP RIGHT SUBJECT PILL ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.END);

        header.setMinHeight("28px");

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
        H3 title = new H3(task.getTitle());
        title.getStyle().set("margin-top", "-8px");
        title.getStyle().set("margin-bottom", "4px");

        // Add the header and title to the card first
        add(header, title);

        // Only create and add the description if it actually exists!
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            Paragraph description = new Paragraph(task.getDescription());
            description.getStyle().set("color", "#666666");
            description.getStyle().set("margin-top", "0");
            description.getStyle().set("margin-bottom", "12px");
            add(description); // Add it right under the title
        }

        // --- 5. CATEGORY BADGE ---
        Span categoryBadge = new Span(task.getCategory() != null ? task.getCategory() : "Personal");
        // ... (Keep your existing Category styling here) ...
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
        // ... (Keep your existing Status styling here) ...
        statusBadge.getStyle().set("padding", "4px 10px");
        statusBadge.getStyle().set("border-radius", "12px");
        statusBadge.getStyle().set("font-size", "12px");
        statusBadge.getStyle().set("font-weight", "bold");
        statusBadge.getStyle().set("border", "1px solid rgba(0, 0, 0, 0.15)");

        if (isEffectivelyCompleted) {
            statusBadge.setText("Completed");
            statusBadge.getStyle().set("color", "#166534");
            statusBadge.getStyle().set("background-color", "#dcfce7");
        } else if (task.isUrgent()) {
            statusBadge.setText("Urgent");
            statusBadge.getStyle().set("color", "#991b1b");
            statusBadge.getStyle().set("background-color", "#fee2e2");
        } else {
            statusBadge.setText("Ongoing");
            statusBadge.getStyle().set("color", "#1e3a8a");
            statusBadge.getStyle().set("background-color", "#dbeafe");
        }

        String dateString = task.getDueDate() != null ? task.getDueDate().format(DATE_TIME_FORMATTER) : "No due date";
        Span date = new Span("Due: " + dateString);
        date.getStyle().set("font-size", "14px");
        date.getStyle().set("color", "#888888");

        // --- 7. FOOTER LAYOUT ---
        HorizontalLayout rightBadges = new HorizontalLayout(categoryBadge, statusBadge);
        rightBadges.getStyle().set("gap", "8px");

        HorizontalLayout footer = new HorizontalLayout(date, rightBadges);
        footer.setWidthFull();
        footer.setAlignItems(Alignment.CENTER);
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Finally, add the footer to the very bottom!
        add(footer);
    }

    private String getContrastTextColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
            return "#111827";
        }
        try {
            int r = Integer.valueOf(hexColor.substring(1, 3), 16);
            int g = Integer.valueOf(hexColor.substring(3, 5), 16);
            int b = Integer.valueOf(hexColor.substring(5, 7), 16);
            double brightness = (r * 299 + g * 587 + b * 114) / 1000.0;
            return brightness < 128 ? "#ffffff" : "#111827";
        } catch (Exception e) {
            return "#111827";
        }
    }
}