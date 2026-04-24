package com.jry.base.ui.components;

import com.jry.backend.Task;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.function.Consumer;

public class TaskForm extends VerticalLayout {
    private Task task;
    private Binder<Task> binder;

    private final FormLayout formLayout = new FormLayout();
    private final Button saveBtn = new Button("Save");
    private final Button cancelBtn = new Button("Cancel");

    private Consumer<Task> onSave;
    private Runnable onCancel;

    public TaskForm(Task task, Consumer<Task> onSave) {
        binder = new Binder<>(Task.class);

        TextField title = new TextField("Title");
        TextField description = new TextField("Description");

        binder.forField(title)
                .asRequired()
                .bind(Task::getTitle, Task::setTitle);
        binder.forField(description)
                .asRequired()
                .bind(Task::getDescription, Task::setDescription);

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0",1));

        formLayout.add(title, description);

        configureButtons();

        add(formLayout, new HorizontalLayout(saveBtn, cancelBtn));
    }

    private void configureButtons() {
        // Styling
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Logic
        cancelBtn.addClickListener(e -> {
            resetForm();
            if(onCancel != null) onCancel.run();
        });

        saveBtn.addClickListener(e -> {
            if (binder.writeBeanIfValid(task)) {
                if(onSave != null) onSave.accept(task); // pass book to save listener
            } else {
                binder.validate();
            }
        });
    }

    public void setTask(Task task) {
        this.task = task;
        binder.readBean(task);
    }

    public void resetForm() {
        binder.readBean(task);
    }

    public void addSaveListener(Consumer<Task> onSave) {
        this.onSave = onSave;
    }

    public void addCancelListener(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setEditable(boolean isEditing) {
        binder.getFields().forEach(field -> field.setReadOnly(!isEditing));
        saveBtn.setEnabled(isEditing);
        saveBtn.setVisible(isEditing);
        cancelBtn.setEnabled(isEditing);
        cancelBtn.setVisible(isEditing);
    }
}
