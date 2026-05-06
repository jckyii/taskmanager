package com.jry.base.ui.components;

import com.jry.backend.entities.Task;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    // --- NEW SEPARATED DATE & TIME PICKERS ---
    private final DatePicker dueDatePicker = new DatePicker("Due date");
    private final TimePicker dueTimePicker = new TimePicker("Time (optional)");
    // -----------------------------------------

    private Consumer<Task> onSave;
    private Runnable onCancel;

    private final Map<String, String> subjectColorMap = new HashMap<>();
    private final String ADD_NEW_OPTION = "+ Add New Subject...";

    public TaskForm() {
        subjectColorMap.put("Math", "#fecaca");
        subjectColorMap.put("Science", "#bbf7d0");
        subjectColorMap.put("History", "#fef08a");

        TextField title = new TextField("Title");

        Select<String> categorySelect = new Select<>();
        categorySelect.setLabel("Category");
        categorySelect.setItems("Work", "School", "Home", "Casual");

        Select<String> subjectSelect = new Select<>();
        subjectSelect.setLabel("Subject");

        updateSelectItems(subjectSelect);

        subjectSelect.addValueChangeListener(event -> {
            String selected = event.getValue();
            if (ADD_NEW_OPTION.equals(selected)) {
                openNewSubjectDialog(subjectSelect, event.getOldValue());
            } else if (selected != null && subjectColorMap.containsKey(selected) && this.task != null) {
                this.task.setSubjectColor(subjectColorMap.get(selected));
            }
        });

        TextArea description = new TextArea("Description");

        // Put the Date and Time pickers perfectly side-by-side
        HorizontalLayout dateLayout = new HorizontalLayout(dueDatePicker, dueTimePicker);
        dateLayout.setWidthFull();
        dueDatePicker.setWidth("50%");
        dueTimePicker.setWidth("50%");

        binder.forField(title).asRequired("Please enter a title").bind(Task::getTitle, Task::setTitle);
        binder.bind(description, Task::getDescription, Task::setDescription);
        binder.bind(subjectSelect, Task::getSubject, Task::setSubject);
        binder.bind(categorySelect, Task::getCategory, Task::setCategory);
        // Notice we do NOT bind the date/time here anymore! We handle it manually on save.

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Add the new grouped dateLayout to the form
        formLayout.add(title, categorySelect, subjectSelect, description, dateLayout);

        configureButtons();
        add(formLayout, new HorizontalLayout(saveBtn, cancelBtn));
    }

    private void updateSelectItems(Select<String> select) {
        List<String> currentItems = new ArrayList<>(subjectColorMap.keySet());
        currentItems.add(ADD_NEW_OPTION);
        select.setItems(currentItems);
    }

    private void openNewSubjectDialog(Select<String> subjectSelect, String previousValue) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create New Subject");

        TextField newSubjectField = new TextField("Subject Name");
        newSubjectField.setWidthFull();

        Input colorPicker = new Input();
        colorPicker.setType("color");
        colorPicker.setValue("#e5e7eb");

        Button saveButton = new Button("Save", e -> {
            String newSubject = newSubjectField.getValue().trim();
            if (!newSubject.isEmpty() && !subjectColorMap.containsKey(newSubject) && !newSubject.equals(ADD_NEW_OPTION)) {
                String chosenColor = colorPicker.getValue();
                subjectColorMap.put(newSubject, chosenColor);
                updateSelectItems(subjectSelect);
                subjectSelect.setValue(newSubject);
                if (this.task != null) {
                    this.task.setSubjectColor(chosenColor);
                }
                dialog.close();
            } else {
                newSubjectField.setInvalid(true);
                newSubjectField.setErrorMessage("Invalid or duplicate name");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> {
            subjectSelect.setValue(previousValue);
            dialog.close();
        });

        HorizontalLayout colorLayout = new HorizontalLayout(new Span("Select Color: "), colorPicker);
        colorLayout.setAlignItems(Alignment.CENTER);
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
            resetForm();
            if(onCancel != null) onCancel.run();
        });

        saveBtn.addClickListener(e -> {
            if (binder.writeBeanIfValid(task)) {

                // --- MANUAL DATE/TIME MERGING MAGIC ---
                if (dueDatePicker.getValue() != null) {
                    // If no time is chosen, default it to 11:59 PM (End of Day)
                    LocalTime time = dueTimePicker.getValue() != null ? dueTimePicker.getValue() : LocalTime.of(23, 59);
                    task.setDueDate(LocalDateTime.of(dueDatePicker.getValue(), time));
                } else {
                    task.setDueDate(null); // No date chosen at all
                }
                // --------------------------------------

                if(onSave != null) onSave.accept(task);
            } else {
                binder.validate();
            }
        });
    }

    public void setTask(Task task) {
        this.task = task;
        binder.readBean(task);
        updateDateAndTimeFields();
    }

    public void resetForm() {
        binder.readBean(task);
        updateDateAndTimeFields();
    }

    // Helper method to load existing date/time when you click an old task
    private void updateDateAndTimeFields() {
        if (this.task != null && this.task.getDueDate() != null) {
            dueDatePicker.setValue(this.task.getDueDate().toLocalDate());
            dueTimePicker.setValue(this.task.getDueDate().toLocalTime());
        } else {
            dueDatePicker.clear();
            dueTimePicker.clear();
        }
    }

    public void addSaveListener(Consumer<Task> onSave) { this.onSave = onSave; }
    public void addCancelListener(Runnable onCancel) { this.onCancel = onCancel; }

    public void setEditable(boolean isEditing) {
        binder.setReadOnly(!isEditing);
        dueDatePicker.setReadOnly(!isEditing); // Manually lock the new fields
        dueTimePicker.setReadOnly(!isEditing); // Manually lock the new fields
        saveBtn.setVisible(isEditing);
        cancelBtn.setVisible(isEditing);
    }
}