package com.jry.backend;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Task {
    public static final int DESCRIPTION_MAX_LENGTH = 300;
    public static final int TITLE_MAX_LENGTH = 100;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)


    @Column(name = "task_id")
    private Long id;

    @Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
    private String title = "";

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description = "";

    @Column(name = "creation_date", nullable = false)//nullable is "required fields"
    private Instant creationDate;

    @Column(name = "due_date")
    @Nullable
    private LocalDate dueDate;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    // Add the getter and setter
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    protected Task() { // To keep Hibernate happy
    }

    public Task(String title, Instant creationDate) {
        setTitle(title);
        this.creationDate = creationDate;
    }
    public Task(String title, String description, Instant creationDate) {
        setTitle(title);
        setDescription(description);
        this.creationDate = creationDate;
    }

    public Task(String title, String description, Instant creationDate, LocalDate dueDate) {
        setTitle(title);
        setDescription(description);
        this.creationDate = creationDate;
        this.dueDate = dueDate;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Title length exceeds " + DESCRIPTION_MAX_LENGTH);
        }
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Description length exceeds " + DESCRIPTION_MAX_LENGTH);
        }
        this.description = description;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@Nullable LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Task other = (Task) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return getClass().hashCode();
    }
}
