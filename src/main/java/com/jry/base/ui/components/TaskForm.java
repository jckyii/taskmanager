package com.jry.base.ui.components;

import com.jry.backend.Task;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TaskForm extends VerticalLayout {
    private Task task;
    private final Binder<Task> binder = new Binder<>(Task.class);

    private final FormLayout formLayout = new FormLayout();
    private final Button saveBtn = new Button("Save");
    private final Button cancelBtn = new Button("Cancel");

    private Consumer<Task> onSave;
    private Runnable onCancel;

    // Map to store our subjects AND their colors
    private final Map<String, String> subjectColorMap = new HashMap<>();
    private final String ADD_NEW_OPTION = "+ Add New Subject...";

    public TaskForm() {
        // Pre-load some default subjects and colors
        subjectColorMap.put("Math", "#fecaca"); // Light Red
        subjectColorMap.put("Science", "#bbf7d0"); // Light Green
        subjectColorMap.put("History", "#fef08a"); // Light Yellow

        TextField title = new TextField("Title");
        TextArea description = new TextArea("Description");
        DateTimePicker dueDate = new DateTimePicker("Due date");

        // --- THE SELECT COMPONENT ---
        Select<String> subjectSelect = new Select<>();
        subjectSelect.setLabel("Subject");

        // Load items and ensure "+ Add New..." is at the bottom
        updateSelectItems(subjectSelect);

        // Listen for when they choose an item
        subjectSelect.addValueChangeListener(event -> {
            String selected = event.getValue();

            if (ADD_NEW_OPTION.equals(selected)) {
                // Open the dialog asking for Name AND Color
                openNewSubjectDialog(subjectSelect, event.getOldValue());
            } else if (selected != null && subjectColorMap.containsKey(selected) && this.task != null) {
                // Update the task's color in the background when they pick a standard subject
                this.task.setSubjectColor(subjectColorMap.get(selected));
            }
        });
        // -----------------------------

        binder.forField(title).asRequired("Please enter a title").bind(Task::getTitle, Task::setTitle);
        binder.forField(description).asRequired("Please enter a description").bind(Task::getDescription, Task::setDescription);
        binder.bind(dueDate, Task::getDueDate, Task::setDueDate);
        binder.bind(subjectSelect, Task::getSubject, Task::setSubject);

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.add(title, subjectSelect, description, dueDate);

        configureButtons();
        add(formLayout, new HorizontalLayout(saveBtn, cancelBtn));
    }

    // Helper method to keep "+ Add New..." at the bottom of the list
    private void updateSelectItems(Select<String> select) {
        List<String> currentItems = new ArrayList<>(subjectColorMap.keySet());
        currentItems.add(ADD_NEW_OPTION);
        select.setItems(currentItems);
    }

    // --- THE COMBINED DIALOG POPUP ---
    private void openNewSubjectDialog(Select<String> subjectSelect, String previousValue) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create New Subject");

        TextField newSubjectField = new TextField("Subject Name");
        newSubjectField.setWidthFull();

        Input colorPicker = new Input();
        colorPicker.setType("color");
        colorPicker.setValue("#e5e7eb"); // Default gray

        Button saveButton = new Button("Save", e -> {
            String newSubject = newSubjectField.getValue().trim();

            // Validate that they actually typed something and it isn't a duplicate
            if (!newSubject.isEmpty() && !subjectColorMap.containsKey(newSubject) && !newSubject.equals(ADD_NEW_OPTION)) {
                String chosenColor = colorPicker.getValue();

                subjectColorMap.put(newSubject, chosenColor); // Save to our list
                updateSelectItems(subjectSelect);             // Refresh dropdown
                subjectSelect.setValue(newSubject);           // Select it

                if (this.task != null) {
                    this.task.setSubjectColor(chosenColor);   // Attach color to task
                }
                dialog.close();
            } else {
                newSubjectField.setInvalid(true);
                newSubjectField.setErrorMessage("Invalid or duplicate name");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> {
            // Revert the dropdown back to what they had selected previously
            subjectSelect.setValue(previousValue);
            dialog.close();
        });

        // Layout for the color picker row
        HorizontalLayout colorLayout = new HorizontalLayout(new Span("Select Color: "), colorPicker);
        colorLayout.setAlignItems(Alignment.CENTER);

        // Put the text field and color layout inside the dialog
        VerticalLayout dialogLayout = new VerticalLayout(newSubjectField, colorLayout);
        dialogLayout.setPadding(false);

        dialog.add(dialogLayout);
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void configureButtons() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        cancelBtn.addClickListener(e -> {
            binder.readBean(task);
            if(onCancel != null) onCancel.run();
        });

        saveBtn.addClickListener(e -> {
            if (binder.writeBeanIfValid(task)) {
                if(onSave != null) onSave.accept(task);
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

    public void addSaveListener(Consumer<Task> onSave) { this.onSave = onSave; }
    public void addCancelListener(Runnable onCancel) { this.onCancel = onCancel; }

    public void setEditable(boolean isEditing) {
        binder.setReadOnly(!isEditing);
        saveBtn.setVisible(isEditing);
        cancelBtn.setVisible(isEditing);
    }
}