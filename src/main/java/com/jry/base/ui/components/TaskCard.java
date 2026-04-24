package com.jry.base.ui.components;

import com.jry.backend.Task;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class TaskCard extends HorizontalLayout {

    public TaskCard(Task task, Runnable onComplete) {
        addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Margin.Bottom.SMALL,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.BETWEEN
        );
        setWidthFull();

        Checkbox completeCheckbox = new Checkbox();
        completeCheckbox.addValueChangeListener(event -> {
            if (event.getValue()) onComplete.run();
        });

        Span title = new Span(task.getTitle());
        title.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE);

        Span description = new Span(task.getDescription());
        description.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        VerticalLayout textLayout = new VerticalLayout(title, description);
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        Span dueDate = new Span(task.getDueDate() != null ? task.getDueDate().toString() : "No due date");
        dueDate.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.TERTIARY);

        HorizontalLayout leftSection = new HorizontalLayout(completeCheckbox, textLayout);
        leftSection.setAlignItems(Alignment.CENTER);

        add(leftSection, dueDate);
    }
}