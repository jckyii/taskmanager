package com.jry.backend;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;

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
    private LocalDateTime dueDate;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(name = "subject")
    private String subject = "";

    @Column(name = "subject_color")
    private String subjectColor = "#e5e7eb"; // Default light gray

    @Column(name = "category")
    private String category = "Personal"; // Default category



    // Add the getter and setter
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Task() { // To keep Hibernate happy
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

    public Task(String title, String description, Instant creationDate, LocalDateTime dueDate) {
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

    public void setDescription(String description) {//add a character limit tracker
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Description length exceeds " + DESCRIPTION_MAX_LENGTH);
        }
        this.description = description;
    }

    public Instant getCreationDate() {
        return creationDate;
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.creationDate == null) {
            this.creationDate = Instant.now();
        }
    }

    public @Nullable LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(@Nullable LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectColor() {
        return subjectColor;
    }

    public void setSubjectColor(String subjectColor) {
        this.subjectColor = subjectColor;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    // Helper method to check if the task is due within 48 hours
    public boolean isUrgent() {
        // If it's already done, or has no due date, it's not urgent
        if (isCompleted || dueDate == null) {
            return false;
        }

        // Calculate the exact time 2 days from right now
        LocalDateTime twoDaysFromNow = LocalDateTime.now().plusDays(2);

        // Return true if the due date is BEFORE the 2-day mark
        return dueDate.isBefore(twoDaysFromNow);
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
