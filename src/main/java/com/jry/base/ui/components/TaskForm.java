package com.jry.base.ui.components;

import com.jry.backend.Task;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class TaskForm extends VerticalLayout {//CHANGE TO DIALOGUE POPUP LATER
    private Task task;
    private final Binder<Task> binder = new Binder<>(Task.class);
    private final List<String> availableSubjects = new ArrayList<>(
            Arrays.asList("Math", "Science", "History")
    );
    private final String ADD_NEW_OPTION = "+ Add New Subject...";

    private final FormLayout formLayout = new FormLayout();
    private final Button saveBtn = new Button("Save");
    private final Button cancelBtn = new Button("Cancel");

    private Consumer<Task> onSave;
    private Runnable onCancel;

    public TaskForm() {
        TextField title = new TextField("Title");
        TextArea description = new TextArea("Description");
        DateTimePicker dueDate = new DateTimePicker("Due Date");
        dueDate.setLocale(Locale.US);
        dueDate.setStep(Duration.ofMinutes(30));


        binder.forField(title)
                .asRequired("Title is required")
                .bind(Task::getTitle, Task::setTitle);

        binder.forField(description)
                .bind(Task::getDescription, Task::setDescription);

        binder.forField(dueDate)
                .bind(Task::getDueDate, Task::setDueDate);

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.add(title, description, dueDate);

        Select<String> subjectSelect = new Select<>();
        subjectSelect.setLabel("Subject");
        updateSelectItems(subjectSelect); // Custom method to load items (see below)

        // 2. Listen for when they choose an item
        subjectSelect.addValueChangeListener(event -> {
            if (ADD_NEW_OPTION.equals(event.getValue())) {
                // If they picked "+ Add New...", open a popup!
                openNewSubjectDialog(subjectSelect, event.getOldValue());
            }
        });
        binder.bind(subjectSelect, Task::getSubject, Task::setSubject);


        configureButtons();
        add(formLayout, new HorizontalLayout(saveBtn, cancelBtn));
    }

    // Helper method to refresh the Select list so "+ Add New..." is always at the bottom
    private void updateSelectItems(Select<String> select) {
        List<String> currentItems = new ArrayList<>(availableSubjects);
        currentItems.add(ADD_NEW_OPTION); // Stick the action button at the very end
        select.setItems(currentItems);
    }

    // Opens a popup window to type the new subject
    private void openNewSubjectDialog(Select<String> subjectSelect, String previousValue) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create New Subject");

        TextField newSubjectField = new TextField("Subject Name");
        newSubjectField.setWidthFull();

        Button saveButton = new Button("Save", e -> {
            String newSubject = newSubjectField.getValue();
            if (!newSubject.trim().isEmpty() && !availableSubjects.contains(newSubject)) {
                availableSubjects.add(newSubject); // Save it to our list
                updateSelectItems(subjectSelect);  // Refresh the dropdown
                subjectSelect.setValue(newSubject); // Select the newly created subject!
            }
            dialog.close();
        });
        saveButton.getStyle().set("background-color", "#006af5");
        saveButton.getStyle().set("color", "white");

        Button cancelButton = new Button("Cancel", e -> {
            // Put the dropdown back to whatever it was before they clicked "+ Add New"
            subjectSelect.setValue(previousValue);
            dialog.close();
        });

        HorizontalLayout dialogButtons = new HorizontalLayout(cancelButton, saveButton);
        dialog.getFooter().add(dialogButtons);
        dialog.add(newSubjectField);

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
        binder.getFields().forEach(field -> field.setReadOnly(!isEditing));
        saveBtn.setEnabled(isEditing);
        saveBtn.setVisible(isEditing);
        cancelBtn.setEnabled(isEditing);
        cancelBtn.setVisible(isEditing);
    }
}